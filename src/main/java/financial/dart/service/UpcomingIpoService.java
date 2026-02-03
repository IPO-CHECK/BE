package financial.dart.service;

import financial.dart.ListedCorp.CorpCodeRow;
import financial.dart.ListedCorp.NameNormalizer;
import financial.dart.domain.ListedCorp;
import financial.dart.client.DartClient;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.*;
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
    private final DartClient dartClient;

    private Map<String, List<CorpCodeRow>> corpNameMap;

    public List<UpcomingDto> mainPageList() {
        List<UpcomingIpo> upcomingIpos = repository.findAll();

        Map<String, Long> ipoIdMap = upcomingIpos.stream()
                .collect(Collectors.toMap(UpcomingIpo::getCorpCode, UpcomingIpo::getId));

        List<String> corpCodes = new ArrayList<>(ipoIdMap.keySet());

        List<UpcomingDto> dtoList = corporationRepository.findAllByCorpCodes(corpCodes);

        dtoList.sort((o1, o2) -> {
            String date1 = extractDate(o1.getSubDate());
            String date2 = extractDate(o2.getSubDate());
            return date2.compareTo(date1);
        });

        // ID 주입
        for (UpcomingDto dto : dtoList) {
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
        Map<String, String> rceptCache = new HashMap<>();
        Set<String> seenIpoNos = new HashSet<>();

        int maxPage = detectMaxPage();
        for (int page = 1; page <= maxPage; page++) {
            String url = page == 1 ? LIST_URL : LIST_URL + "&page=" + page;
            Document doc = fetchPage(url).doc();
            List<Row> rows = parseUpcomingRows(doc);

            for (Row row : rows) {
                seenIpoNos.add(row.ipoNo);
                String normalized = NameNormalizer.norm(row.corpName);
                String corpCode = pickCorpCode(nameMap, normalized);

                UpcomingIpo entity = repository.findByIpoNo(row.ipoNo)
                        .orElseGet(() -> new UpcomingIpo(
                                row.corpName, normalized, corpCode, row.ipoNo, row.detailUrl
                        ));

                entity.updateBasic(row.corpName, normalized, corpCode, row.detailUrl);

                String rceptNo = "";
                if (corpCode != null && !corpCode.isBlank()) {
                    rceptNo = rceptCache.computeIfAbsent(corpCode, this::fetchRceptNoByCorpCode);
                }
                if (rceptNo == null || rceptNo.isBlank()) {
                    rceptNo = fetchRceptNoByDetailUrl(row.detailUrl);
                }
                if (rceptNo != null && !rceptNo.isBlank()) {
                    entity.updateRceptNo(rceptNo);
                }

                String industry = fetchIndustryByDetailUrl(row.detailUrl);
                updateIndustryIfEmpty(entity, industry);

                DetailInfo detailInfo = fetchDetailInfoByDetailUrl(row.detailUrl);
                if (detailInfo != null) {
                    entity.updateOfferingInfo(
                            detailInfo.underwriter(),
                            detailInfo.subDate(),
                            detailInfo.listDate(),
                            detailInfo.status(),
                            detailInfo.price(),
                            detailInfo.expectedPrice()
                    );
                }

                repository.save(entity);
                saved.add(entity);
            }
        }

        if (!seenIpoNos.isEmpty()) {
            repository.deleteByIpoNoNotIn(seenIpoNos);
        } else {
            // 목록을 찾지 못한 경우 삭제하지 않음
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

    public String fetchIndustryByDetailUrl(String detailUrl) {
        if (detailUrl == null || detailUrl.isBlank()) return "";
        Page page = fetchPage(detailUrl);
        String industry = extractIndustryFromDoc(page.doc());
        if ((industry == null || industry.isBlank()) && page.bytes() != null) {
            try {
                String eucHtml = new String(page.bytes(), Charset.forName("EUC-KR"));
                industry = extractIndustryFromDoc(Jsoup.parse(eucHtml));
            } catch (Exception ignored) {
                // ignore
            }
        }
        return industry == null ? "" : industry.trim();
    }

    public DetailInfo fetchDetailInfoByDetailUrl(String detailUrl) {
        if (detailUrl == null || detailUrl.isBlank()) return null;
        Page page = fetchPage(detailUrl);
        DetailInfo info = extractDetailInfoFromDoc(page.doc());
        if ((info == null || info.isBlank()) && page.bytes() != null) {
            try {
                String eucHtml = new String(page.bytes(), Charset.forName("EUC-KR"));
                info = extractDetailInfoFromDoc(Jsoup.parse(eucHtml));
            } catch (Exception ignored) {
                // ignore
            }
        }
        return info;
    }

    public String fetchRceptNoByCorpCode(String corpCode) {
        if (corpCode == null || corpCode.isBlank()) return "";
        try {
            Map<?, ?> response = dartClient.fetchEstkRs(corpCode, "19900101", "20991231");
            if (response == null) return "";
            Object statusObj = response.get("status");
            if (statusObj != null && !"000".equals(String.valueOf(statusObj))) return "";
            Object groupsObj = response.get("group");
            if (!(groupsObj instanceof List<?> groups)) return "";
            for (Object groupObj : groups) {
                if (!(groupObj instanceof Map<?, ?> group)) continue;
                Object listObj = group.get("list");
                if (!(listObj instanceof List<?> list) || list.isEmpty()) continue;
                for (Object itemObj : list) {
                    if (!(itemObj instanceof Map<?, ?> item)) continue;
                    Object rceptNo = item.get("rcept_no");
                    if (rceptNo != null && !String.valueOf(rceptNo).isBlank()) {
                        return String.valueOf(rceptNo).trim();
                    }
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
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
        LocalDate today = LocalDate.now();

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
                String scheduleText = findScheduleText(tds);
                LocalDate endDate = parseEndDate(scheduleText);
                if (corpName.isEmpty() || endDate == null || !endDate.isAfter(today)) continue;

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

    private String extractIndustryFromDoc(Document doc) {
        if (doc == null) return "";
        for (Element tr : doc.select("tr")) {
            Elements cells = tr.select("th,td");
            for (int i = 0; i < cells.size(); i++) {
                String text = normalizeCellText(cells.get(i).text());
                if (!"업종".equals(text)) continue;
                for (int j = i + 1; j < cells.size(); j++) {
                    String value = normalizeCellText(cells.get(j).text());
                    if (!value.isBlank()) return value;
                }
            }
        }
        return "";
    }

    private DetailInfo extractDetailInfoFromDoc(Document doc) {
        if (doc == null) return DetailInfo.empty();
        String underwriter = extractValueByLabels(doc, List.of("주관사", "주간사", "인수회사", "인수인"));
        String subDate = extractValueByLabels(doc, List.of("공모청약일"));
        String listDate = extractValueByLabels(doc, List.of("상장일"));
        String status = extractValueByLabels(doc, List.of("진행상황"));
        String price = extractValueByLabels(doc, List.of("공모가", "확정공모가"));
        String expectedPrice = extractValueByLabels(doc, List.of("희망공모가액"));

        if (listDate == null || listDate.isBlank()) {
            listDate = "미정";
        }
        if (status == null || status.isBlank()) {
            status = deriveStatus(subDate, listDate);
        }
        return new DetailInfo(
                blankToNull(underwriter),
                blankToNull(subDate),
                blankToNull(listDate),
                blankToNull(status),
                blankToNull(price),
                blankToNull(expectedPrice)
        );
    }

    private String extractValueByLabels(Document doc, List<String> labels) {
        for (Element tr : doc.select("tr")) {
            Elements cells = tr.select("th,td");
            for (int i = 0; i < cells.size(); i++) {
                String text = normalizeCellText(cells.get(i).text());
                if (text.isEmpty()) continue;
                if (!labels.contains(text)) continue;
                for (int j = i + 1; j < cells.size(); j++) {
                    String value = normalizeCellText(cells.get(j).text());
                    if (!value.isBlank()) return value;
                }
            }
        }
        return "";
    }

    private String deriveStatus(String subDate, String listDate) {
        LocalDate today = LocalDate.now();
        LocalDate subEnd = parseEndDate(subDate);
        LocalDate list = parseSingleDate(listDate);

        if (list != null && list.isBefore(today)) {
            return "상장완료";
        }
        if (subEnd != null && (subEnd.isAfter(today) || subEnd.isEqual(today))) {
            return "청약예정";
        }
        if (list != null && list.isAfter(today)) {
            return "상장예정";
        }
        return "준비중";
    }

    private LocalDate parseSingleDate(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher full = Pattern.compile("(\\d{4})\\.(\\d{2})\\.(\\d{2})").matcher(text);
        if (full.find()) {
            int y = Integer.parseInt(full.group(1));
            int m = Integer.parseInt(full.group(2));
            int d = Integer.parseInt(full.group(3));
            return LocalDate.of(y, m, d);
        }
        return null;
    }

    private String normalizeCellText(String text) {
        if (text == null) return "";
        return text.replace("\u00A0", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String blankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    private String findScheduleText(Elements tds) {
        for (Element td : tds) {
            String text = td.text().trim();
            if (text.matches(".*\\d{4}\\.\\d{2}\\.\\d{2}.*")) {
                return text;
            }
        }
        return "";
    }

    private LocalDate parseEndDate(String text) {
        if (text == null || text.isBlank()) return null;
        Matcher full = Pattern.compile("(\\d{4})\\.(\\d{2})\\.(\\d{2})").matcher(text);
        List<LocalDate> dates = new ArrayList<>();
        while (full.find()) {
            int y = Integer.parseInt(full.group(1));
            int m = Integer.parseInt(full.group(2));
            int d = Integer.parseInt(full.group(3));
            dates.add(LocalDate.of(y, m, d));
        }
        if (!dates.isEmpty()) {
            if (dates.size() >= 2) {
                return dates.get(dates.size() - 1);
            }
            LocalDate start = dates.get(0);
            Matcher shortEnd = Pattern.compile("~\\s*(\\d{2})\\.(\\d{2})").matcher(text);
            if (shortEnd.find()) {
                int m = Integer.parseInt(shortEnd.group(1));
                int d = Integer.parseInt(shortEnd.group(2));
                return LocalDate.of(start.getYear(), m, d);
            }
            return start;
        }
        return null;
    }

    private record Row(String corpName, String ipoNo, String detailUrl) {}

    private record DetailInfo(
            String underwriter,
            String subDate,
            String listDate,
            String status,
            String price,
            String expectedPrice
    ) {
        boolean isBlank() {
            return (underwriter == null || underwriter.isBlank())
                    && (subDate == null || subDate.isBlank())
                    && (listDate == null || listDate.isBlank())
                    && (status == null || status.isBlank())
                    && (price == null || price.isBlank())
                    && (expectedPrice == null || expectedPrice.isBlank());
        }

        static DetailInfo empty() {
            return new DetailInfo(null, null, null, null, null, null);
        }
    }

    private record Page(Document doc, String html, byte[] bytes) {
    }

    private String extractDate(String dateRange) {
        if (dateRange == null || dateRange.trim().isEmpty() || dateRange.equals("미정")) {
            // "미정"이거나 비어있으면 가장 과거 날짜로 취급해 맨 아래로 보냄 (내림차순 기준)
            return "0000.00.00";
            // 만약 "미정"을 맨 위로 올리고 싶다면 "9999.99.99"로 설정
        }
        // "2026.03.11 ~ 2026.03.12" -> "2026.03.11" 추출
        return dateRange.split("~")[0].trim();
    }
}
