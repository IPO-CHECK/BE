package financial.dart.service;

import financial.dart.ListedCorp.CorpCodeRow;
import financial.dart.ListedCorp.NameNormalizer;
import financial.dart.domain.ListedCorp;
import financial.dart.domain.UpcomingIpo;
import financial.dart.dto.UpcomingDto;
import financial.dart.repository.CorporationRepository;
import financial.dart.repository.ListedCorpRepository;
import financial.dart.repository.UpcomingIpoRepository;
import financial.dart.util.CorpCodeXmlParser;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpcomingIpoService {

    private static final String LIST_URL = "https://www.38.co.kr/html/fund/index.htm?o=k";
    private static final String DETAIL_URL_PREFIX = "https://www.38.co.kr/html/fund/?o=v&no=";

    private final ListedCorpRepository listedCorpRepository;
    private final CorporationRepository corporationRepository;
    private final UpcomingIpoRepository repository;
    private final CorpCodeXmlParser corpCodeXmlParser;
    private final ResourceLoader resourceLoader;

    private Map<String, List<CorpCodeRow>> corpNameMap;

    public List<UpcomingDto> mainPageList() {
        List<UpcomingIpo> upcomingIpos = repository.findAll();

        Map<String, Long> ipoIdMap = upcomingIpos.stream()
                .collect(Collectors.toMap(UpcomingIpo::getCorpCode, UpcomingIpo::getId));

        List<String> corpCodes = new ArrayList<>(ipoIdMap.keySet());

        List<UpcomingDto> dtoList = corporationRepository.findAllByCorpCodes(corpCodes);

        List<String> tempCodes = dtoList.stream()
                .map(UpcomingDto::getCorpCode)
                .toList();

        if (tempCodes.isEmpty()) {
            return dtoList;
        }

        for (UpcomingDto dto : dtoList) {
            // (1) UpcomingIpo ID 주입
            if (ipoIdMap.containsKey(dto.getCorpCode())) {
                dto.setUpcomingIpoId(ipoIdMap.get(dto.getCorpCode()));
            }
        }

        return dtoList;
    }

    public String findCorpCodeById(Long id) {
        return repository.findCorpCodeById(id);
    }

    @Transactional
    public List<UpcomingIpo> refreshFrom38() {
        Map<String, List<CorpCodeRow>> nameMap = getCorpNameMap();
        List<UpcomingIpo> saved = new ArrayList<>();

        int maxPage = detectMaxPage();
        for (int page = 1; page <= maxPage; page++) {
            String url = page == 1 ? LIST_URL : LIST_URL + "&page=" + page;
            Document doc = fetchPage(url).doc();
            List<Row> rows = parseUpcomingRows(doc);

            for (Row row : rows) {
                String normalized = NameNormalizer.norm(row.corpName);
                String corpCode = pickCorpCode(nameMap, normalized);

                UpcomingIpo entity = repository.findByIpoNo(row.ipoNo)
                        .orElseGet(() -> new UpcomingIpo(
                                row.corpName, normalized, corpCode, row.ipoNo, row.detailUrl
                        ));

                entity.updateBasic(row.corpName, normalized, corpCode, row.detailUrl);

                String rceptNo = fetchRceptNoByDetailUrl(row.detailUrl);
                if (rceptNo != null && !rceptNo.isBlank()) {
                    entity.updateRceptNo(rceptNo);
                }

                repository.save(entity);
                saved.add(entity);
            }
        }

        return saved;
    }

    // TODO 상장 예정 기업 리스트업
    public List<UpcomingIpo> listAll() {
        return repository.findAll();
    }

    public UpcomingIpo getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("upcoming_ipo not found: " + id));
    }

    public UpcomingIpo save(UpcomingIpo ipo) {
        return repository.save(ipo);
    }

    public void updateIndustryIfEmpty(UpcomingIpo ipo, String industry) {
        if (ipo == null) return;
        if (ipo.getIndustry() != null && !ipo.getIndustry().isBlank()) return;
        if (industry == null || industry.isBlank()) return;
        ipo.updateIndustry(industry.trim());
        repository.save(ipo);
    }

    public String fetchRceptNoByDetailUrl(String detailUrl) {
        if (detailUrl == null || detailUrl.isBlank()) return "";
        Page page = fetchPage(detailUrl);
        String rceptNo = extractRceptNo(page.html());
        if ((rceptNo == null || rceptNo.isBlank()) && page.bytes() != null) {
            try {
                String eucHtml = new String(page.bytes(), Charset.forName("EUC-KR"));
                rceptNo = extractRceptNo(eucHtml);
            } catch (Exception ignored) {
                // ignore
            }
        }
        return rceptNo == null ? "" : rceptNo.trim();
    }

    private Map<String, List<CorpCodeRow>> getCorpNameMap() {
        if (corpNameMap != null) return corpNameMap;
        try {
            Resource xml = resourceLoader.getResource("classpath:CORPCODE.xml");
            corpNameMap = corpCodeXmlParser.parseToNameMap(xml);
            return corpNameMap;
        } catch (Exception e) {
            throw new IllegalStateException("CORPCODE.xml 파싱 실패", e);
        }
    }

    private String pickCorpCode(Map<String, List<CorpCodeRow>> nameMap, String normalized) {
        if (normalized == null || normalized.isBlank()) return null;
        List<CorpCodeRow> list = nameMap.get(normalized);
        if (list == null || list.isEmpty()) return null;
        return list.get(0).corpCode();
    }

    private int detectMaxPage() {
        try {
            Document doc = fetchPage(LIST_URL).doc();
            int max = 1;
            for (Element a : doc.select("a[href*='page=']")) {
                String href = a.attr("href");
                int idx = href.indexOf("page=");
                if (idx < 0) continue;
                String s = href.substring(idx + 5).replaceAll("[^0-9].*$", "");
                if (s.isBlank()) continue;
                int n = Integer.parseInt(s);
                if (n > max) max = n;
            }
            return max;
        } catch (Exception e) {
            return 1;
        }
    }

    private Page fetchPage(String url) {
        try {
            var response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .referrer("https://www.38.co.kr/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8")
                    .timeout(10_000)
                    .ignoreContentType(true)
                    .execute();
            byte[] bytes = response.bodyAsBytes();
            String html = response.body();
            Document doc = response.parse();
            return new Page(doc, html, bytes);
        } catch (Exception e) {
            throw new IllegalStateException("38.co.kr 페이지 로딩 실패: " + url, e);
        }
    }

    private List<Row> parseUpcomingRows(Document doc) {
        List<Row> rows = new ArrayList<>();
        Elements tables = doc.select("table");

        for (Element table : tables) {
            Elements header = table.select("tr").first() == null
                    ? new Elements()
                    : table.select("tr").first().select("th");
            if (header.isEmpty()) continue;

            boolean hasHeader = header.stream().anyMatch(th -> th.text().contains("확정공모가"));
            if (!hasHeader) continue;

            for (Element tr : table.select("tr")) {
                Elements tds = tr.select("td");
                if (tds.size() < 3) continue;

                String corpName = tds.get(0).text().trim();
                String fixedPrice = tds.get(2).text().trim();
                if (corpName.isEmpty() || !"-".equals(fixedPrice)) continue;

                Element link = tds.get(0).selectFirst("a[href*='no=']");
                if (link == null) continue;

                String href = link.attr("href");
                String ipoNo = extractNo(href);
                if (ipoNo == null) continue;

                String detailUrl = DETAIL_URL_PREFIX + ipoNo + "&l=&page=1";
                rows.add(new Row(corpName, ipoNo, detailUrl));
            }
        }

        return rows;
    }

    private String extractNo(String href) {
        if (href == null) return null;
        int idx = href.indexOf("no=");
        if (idx < 0) return null;
        String s = href.substring(idx + 3);
        s = s.replaceAll("[^0-9].*$", "");
        return s.isBlank() ? null : s;
    }

    private String extractRceptNo(String html) {
        if (html == null || html.isBlank()) return "";
        Pattern p = Pattern.compile("viewDetail2\\s*\\(\\s*'?(\\d{14})'?");
        Matcher m = p.matcher(html);
        if (m.find()) return m.group(1);
        return "";
    }

    private record Row(String corpName, String ipoNo, String detailUrl) {
    }

    private record Page(Document doc, String html, byte[] bytes) {
    }
}
