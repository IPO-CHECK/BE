package financial.dart.service;

import financial.dart.ListedCorp.*;
import financial.dart.domain.ListedCorp;
import financial.dart.repository.ListedCorpRepository;
import financial.dart.util.CorpCodeXmlParser;
import financial.dart.util.ListedHtmlParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ListedUniverseImportService {

    private final ListedHtmlParser listedHtmlParser;
    private final CorpCodeXmlParser corpCodeXmlParser;
    private final ListedCorpRepository listedCorpRepository;

    public ListedUniverseImportService(ListedHtmlParser listedHtmlParser,
                                       CorpCodeXmlParser corpCodeXmlParser,
                                       ListedCorpRepository listedCorpRepository) {
        this.listedHtmlParser = listedHtmlParser;
        this.corpCodeXmlParser = corpCodeXmlParser;
        this.listedCorpRepository = listedCorpRepository;
    }

    @Transactional
    public ImportResult importFromClasspath() throws Exception {
        // 1) 리소스 로드
        var listedHtml = new ClassPathResource("상장법인목록.html");
        var corpCodeXml = new ClassPathResource("CORPCODE.xml");

        // 2) 파싱
        List<ListedRow> listedRows = listedHtmlParser.parse(listedHtml);
        Map<String, List<CorpCodeRow>> corpNameMap = corpCodeXmlParser.parseToNameMap(corpCodeXml);

        int total = listedRows.size();
        int matched = 0;
        int saved = 0;
        int ambiguous = 0;
        int notFound = 0;

        for (ListedRow lr : listedRows) {
            String key = NameNormalizer.norm(lr.corpName());
            List<CorpCodeRow> hits = corpNameMap.get(key);

            if (hits == null || hits.isEmpty()) {
                notFound++;
                continue;
            }

            // 보조 검증: CORPCODE 쪽 stock_code가 있을 때만 listed stock_code와 맞추기
            List<CorpCodeRow> refined = refineByStockCodeIfPossible(hits, lr.stockCode());

            if (refined.size() > 1) {
                ambiguous++;
            }

            // 정책: 여러개면 첫번째를 사용 (원하시면 전부 저장/로그로 남기도록 바꿔드릴 수 있음)
            CorpCodeRow picked = refined.get(0);
            matched++;

            // DB upsert
            ListedCorp entity = listedCorpRepository.findByCorpCode(picked.corpCode())
                    .orElseGet(() -> new ListedCorp(picked.corpCode(), lr.corpName()));

            entity.setCorpName(lr.corpName());
            entity.setStockCode(emptyToNull(lr.stockCode()));
            entity.setMarket(emptyToNull(lr.market()));
            entity.setIndustry(emptyToNull(lr.industry()));
            entity.setMainProducts(emptyToNull(lr.mainProducts()));
            entity.setListedDate(parseListedDate(lr.listedDateRaw()));
            entity.setFiscalMonth(emptyToNull(lr.fiscalMonth()));
            entity.setCeoName(emptyToNull(lr.ceoName()));
            entity.setHomepage(emptyToNull(lr.homepage()));
            entity.setRegion(emptyToNull(lr.region()));

            listedCorpRepository.save(entity);
            saved++;
        }

        return new ImportResult(total, matched, saved, ambiguous, notFound);
    }

    private static List<CorpCodeRow> refineByStockCodeIfPossible(List<CorpCodeRow> hits, String listedStockCode) {
        String listed = listedStockCode == null ? "" : listedStockCode.trim();
        if (listed.isEmpty()) return hits;

        // CORPCODE의 stock_code가 존재하는 row만 listed와 비교
        List<CorpCodeRow> withStock = hits.stream()
                .filter(h -> h.stockCode() != null && !h.stockCode().trim().isEmpty())
                .toList();

        if (withStock.isEmpty()) return hits;

        List<CorpCodeRow> matched = withStock.stream()
                .filter(h -> listed.equals(h.stockCode().trim()))
                .toList();

        return matched.isEmpty() ? hits : matched;
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static LocalDate parseListedDate(String raw) {
        // 파일엔 보통 YYYY-MM-DD 형태입니다. (예: 1996-06-04)
        if (raw == null) return null;
        String t = raw.trim();
        if (t.isEmpty() || "-".equals(t)) return null;

        try {
            return LocalDate.parse(t, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return null; // 형식이 다르면 일단 null 처리 (필요 시 로깅)
        }
    }

    public record ImportResult(int total, int matched, int saved, int ambiguous, int notFound) {}
}