package financial.dart.service;

import financial.dart.domain.Corporation;
import financial.dart.repository.CorporationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CorporationService {

    @Value("${dart.api-key}")
    private String apiKey;

    private final CorporationRepository corporationRepository;
    private final RestTemplate restTemplate;

    @Transactional
    public void saveCorporationData(List<Corporation> corporations) {
        try {
            corporationRepository.deleteAllInBatch();
            corporationRepository.saveAll(corporations);
        } catch (Exception e) {
            throw new RuntimeException("데이터 동기화 실패", e);
        }
    }

    public List<Corporation> getCorps() {
        return corporationRepository.findCorps();
    }

    public List<Long> findQualifiedCorpIds() {
        return corporationRepository.findQualifiedCorporationIds();
    }

    // [1번 기준 로직] 상장, 등록 후에 3개월이 경과할 것
    // TODO DART에서는 상장일을 구할 수가 없어서 대체 로직을 짠건데 문제 있는 듯
    @Transactional
    public void checkListingDate(String corpCode) {
        String url = UriComponentsBuilder.fromUriString("https://opendart.fss.or.kr/api/list.json")
                .queryParam("crtfc_key", apiKey)
                .queryParam("corp_code", corpCode)
                .queryParam("bgn_de", "19500101")
                .queryParam("pblntf_ty", "A")    // 핵심: 'A'는 사업/반기/분기보고서만 가져옵니다
                .queryParam("sort", "date")
                .queryParam("sort_mth", "asc")   // 옛날순
                .queryParam("page_count", "30")  // 넉넉하게 30건 정도 가져와서 검사
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, String>> list = (List<Map<String, String>>) response.get("list");
        Corporation corporation = corporationRepository.findByCorpCode(corpCode);

        // 1. 데이터가 아예 없으면 -> 판단 불가(탈락) 후 종료
        if (list == null || list.isEmpty()) {
            corporation.updateIsOver3Months(false);
            return; //
        }

        // 2. 데이터가 있으면 로직 수행
        String businessReportDt = null;
        for (Map<String, String> report : list) {
            if (report.get("report_nm").contains("사업보고서")) {
                businessReportDt = report.get("rcept_dt");
                break;
            }
        }

        if (businessReportDt == null) {
            businessReportDt = list.get(0).get("rcept_dt");
        }

        LocalDate firstDate = LocalDate.parse(businessReportDt, DateTimeFormatter.ofPattern("yyyyMMdd"));
        boolean result = firstDate.isBefore(LocalDate.now().minusMonths(3));

        // 결과 저장
        corporation.updateIsOver3Months(result);
    }

    // [Criterion 2] 최근 2년간 감사의견이 ‘적정’일 것
    @Transactional
    public void checkAuditOpinion(String corpCode) {
        // 최근 2년치
        int currentYear = LocalDate.now().getYear();
        String[] years = {String.valueOf(currentYear - 1), String.valueOf(currentYear - 2)};
        Corporation corporation = corporationRepository.findByCorpCode(corpCode);

        for (String year : years) {
            String url = UriComponentsBuilder.fromUriString("https://opendart.fss.or.kr/api/accnutAdtorNmNdAdtOpinion.json")
                    .queryParam("crtfc_key", apiKey)
                    .queryParam("corp_code", corpCode)
                    .queryParam("bsns_year", year)
                    .queryParam("reprt_code", "11011") // 사업보고서 고정
                    .toUriString();

            try {
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                // 데이터 없거나 리스트 비었으면 -> 뭔가 문제 있음 -> 탈락
                if (response == null || response.get("list") == null) {
                    corporation.updateHasUnqualifiedOpinion(false);
                    return; //
                }

                List<Map<String, String>> list = (List<Map<String, String>>) response.get("list");
                if (list.isEmpty()) {
                    corporation.updateHasUnqualifiedOpinion(false);
                    return; //
                }

                String opinion = list.get(0).get("adt_opinion");

                // "적정"이 아니면 -> 탈락
                if (opinion == null || !opinion.contains("적정")) {
                    corporation.updateHasUnqualifiedOpinion(false);
                    return; //
                }

            } catch (Exception e) {
                System.err.println("API 오류: " + e.getMessage());
            }
        }
        corporation.updateHasUnqualifiedOpinion(true); // 2년 모두 적정이면 통과
    }

    // [3번 기준 로직] 최근 2년간 경영에 중대한 영향을 미칠 수 있는 합병, 영업의 양수도, 분할이 없을 것
    @Transactional
    public void checkMnAHistory(String corpCode) {
        String twoYearsAgo = LocalDate.now().minusYears(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String[] targetDetails = {"B001", "B002", "B005", "C004"};
        Corporation corporation = corporationRepository.findByCorpCode(corpCode);

        for (String detail : targetDetails) {
            String url = UriComponentsBuilder.fromUriString("https://opendart.fss.or.kr/api/list.json")
                    .queryParam("crtfc_key", apiKey)
                    .queryParam("corp_code", corpCode)
                    .queryParam("bgn_de", twoYearsAgo)
                    .queryParam("pblntf_detail_ty", detail)
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            // 1. response 자체가 null인지 확인
            if (response != null && !"0".equals(String.valueOf(response.get("total_count")))) {
                List<Map<String, String>> reports = (List<Map<String, String>>) response.get("list");

                // 🌟 [핵심 수정] reports가 null이 아닌지 한 번 더 확인합니다!
                if (reports != null) {
                    for (Map<String, String> report : reports) {
                        String reportNm = report.get("report_nm");
                        if (reportNm != null && (reportNm.contains("합병") || reportNm.contains("분할") ||
                                reportNm.contains("양수") || reportNm.contains("양도"))) {
                            corporation.updateHasNoMajorChanges(false);
                            return;
                        }
                    }
                }
            }
        }
        corporation.updateHasNoMajorChanges(true);
    }
}
