package financial.dart.Test;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import org.jsoup.select.Elements;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Service
public class KindListedCorpImportService {

    private static final String KIND_URL = "https://kind.krx.co.kr/corpgeneral/corpList.do";
    private static final String KIND_REFERER = "https://kind.krx.co.kr/corpgeneral/corpList.do?method=loadInitPage";

    private final RestTemplate restTemplate;
    private final ListedCorpRepository repository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public KindListedCorpImportService(RestTemplate restTemplate, ListedCorpRepository repository) {
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    /**
     * 1) KIND에서 "엑셀 다운로드" (실제는 HTML 테이블이 내려오는 경우가 많음)
     * 2) Jsoup로 <table> 행 파싱
     * 3) DB 저장 (중복은 stockCode로 스킵)
     */
    @Transactional
    public ImportResultDto downloadParseAndSave(KindCorpDownloadRequestDto reqDto) {
        String html = downloadCorpListAsHtml(reqDto);

        // 기존 종목코드 미리 로드 (existsBy...를 매번 치는 것보다 빠름)
        Set<String> existing = new HashSet<>(repository.findAllStockCodes());

        Document doc = Jsoup.parse(html);

        // ✅ KIND/다운받은 "엑셀"은 table에 데이터가 있음
        // 보통 "table.bbs_tb" 안에 tr들이 쌓임 (헤더 1행 + 데이터 N행)
        Elements rows = doc.select("table.bbs_tb tr");

        int parsed = 0;
        int inserted = 0;
        int skipped = 0;

        for (int i = 1; i < rows.size(); i++) { // i=0 헤더
            Elements tds = rows.get(i).select("td");
            if (tds.size() < 10) {
                continue;
            }
            parsed++;

            String companyName = clean(tds.get(0).text());
            String market = clean(tds.get(1).text());
            String stockCode = clean(tds.get(2).text());
            String industry = clean(tds.get(3).text());
            String mainProducts = clean(tds.get(4).text());
            LocalDate listedDate = parseDate(clean(tds.get(5).text()));
            String fiscalMonth = clean(tds.get(6).text());
            String ceoName = clean(tds.get(7).text());
            String homepage = clean(tds.get(8).text());
            String region = clean(tds.get(9).text());

            if (stockCode.isEmpty() || existing.contains(stockCode)) {
                skipped++;
                continue;
            }

            ListedCorp entity = new ListedCorp(
                    companyName, market, stockCode, industry,
                    mainProducts, listedDate, fiscalMonth,
                    ceoName, homepage, region
            );

            repository.save(entity);
            existing.add(stockCode);
            inserted++;
        }

        return new ImportResultDto(parsed, inserted, skipped);
    }

    private String downloadCorpListAsHtml(KindCorpDownloadRequestDto reqDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Referer", KIND_REFERER);
        headers.set("User-Agent", "Mozilla/5.0");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        // ✅ KIND는 기본 페이지가 method=loadInitPage이고, 다운로드는 method=download 형태로 요청되는 경우가 많습니다.
        body.add("method", "download");

        // ↓ 여기부터는 필요시 확장 (Network에서 실제 파라미터명을 확인해 정확히 맞추는 게 가장 안전)
        if (reqDto != null) {
            if (reqDto.marketType != null) body.add("marketType", reqDto.marketType);
            if (reqDto.companyName != null) body.add("companyName", reqDto.companyName);
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                KIND_URL,
                HttpMethod.POST,
                requestEntity,
                byte[].class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("KIND 다운로드 실패: status=" + response.getStatusCode());
        }

        // KIND에서 내려오는 파일은 EUC-KR인 케이스가 흔합니다.
        // 한글 깨지면 EUC-KR로 우선 디코딩하세요.
        return new String(response.getBody(), Charset.forName("EUC-KR"));
    }

    private String clean(String s) {
        if (s == null) return "";
        return s.replace("\u00A0", " ").trim();
    }

    private LocalDate parseDate(String dateText) {
        if (dateText == null || dateText.isBlank()) return null;
        // "2026-01-30" 형태 가정
        return LocalDate.parse(dateText, dateFormatter);
    }
}