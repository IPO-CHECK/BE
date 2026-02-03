package financial.dart.service;

import financial.dart.domain.Corporation;
import financial.dart.dto.BasicDto;
import financial.dart.dto.DetailDto;
import financial.dart.repository.CorporationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CorporationService {

    @Value("${dart.api-key}")
    private String apiKey;

    private final CorporationRepository corporationRepository;
    private final RestTemplate restTemplate;

    public BasicDto getBasicDetail(String corpCode) {
        Corporation corp = corporationRepository.findByCorpCode(corpCode);
        return BasicDto.builder()
                .id(corp.getId())
                .name(corp.getCorpName())
                .code(corp.getCorpCode())
                .industry(corp.getIndustry())
                .market(corp.getMarket())
                .products(corp.getMajorProducts())
                .expectedPrice(corp.getHopePriceBand())
                .finalPrice(corp.getFinalOfferPrice())
                .publicShares(String.format("%,d주", corp.getTotalOfferShares()))
                .generalShares(formatSharesRange(corp.getGeneralOfferShares()))
                .underwriter(corp.getUnderwriter())
                .schedule(buildSchedule(corp)) // 일정 리스트 빌드
                .build();
    }

    @Transactional
    public void saveCorporationData(List<Corporation> corporations) {
        try {
            corporationRepository.deleteAllInBatch();
            corporationRepository.saveAll(corporations);
        } catch (Exception e) {
            throw new RuntimeException("데이터 동기화 실패", e);
        }
    }

    public Long findCorporationIdByCorpCode(String corpCode) {
        return corporationRepository.findIdByCorpCode(corpCode).orElse(null);
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

    private List<BasicDto.ScheduleDto> buildSchedule(Corporation corp) {
        List<BasicDto.ScheduleDto> schedules = new ArrayList<>();
        LocalDate today = LocalDate.now(); // 오늘 날짜: 2026-02-03

        // 1. 모든 단계를 리스트에 추가 (기본 addStep 로직 수행)
        addStep(schedules, "예비심사청구", corp.getPreliminaryReviewDate(), today);
        addStep(schedules, "심사승인", corp.getApprovalDate(), today);
        addStep(schedules, "청약공고", corp.getSubscriptionNoticeDate(), today);
        addStep(schedules, "청약기일", corp.getSubscriptionDate(), today);
        addStep(schedules, "납입기일", corp.getPaymentDate(), today);
        addStep(schedules, "배정공고", corp.getAllocationDate(), today);
        addStep(schedules, "상장일", corp.getListingDate(), today);

        // 2. 리스트에 "active" 상태인 일정이 있는지 확인
        boolean hasActive = schedules.stream()
                .anyMatch(s -> "active".equals(s.getStatus()));

        // 3. 오늘 날짜와 딱 맞는 일정이 없어 active가 비어있다면,
        //    future 중 가장 빠른 것(리스트 상의 첫 번째 future)을 active로 변경
        if (!hasActive) {
            for (BasicDto.ScheduleDto schedule : schedules) {
                if ("future".equals(schedule.getStatus())) {
                    schedule.setStatus("active");
                    break; // 가장 빠른 하나만 찾으면 루프 종료
                }
            }
        }

        return schedules;
    }

    private void addStep(List<BasicDto.ScheduleDto> list, String step, String dateStr, LocalDate today) {
        if (dateStr == null || dateStr.isEmpty()) return;

        String status = "future";
        try {
            // "2026.03.11" 또는 "2026.03.11 ~ 2026.03.12" 형식 처리
            String compareDate = dateStr.contains("~") ? dateStr.split("~")[0].trim() : dateStr;
            LocalDate targetDate = LocalDate.parse(compareDate.replace(".", "-"));

            if (targetDate.isBefore(today)) status = "done";
            else if (targetDate.isEqual(today)) status = "active";
        } catch (Exception e) {
            // "미정" 등의 문자열 처리
            status = "future";
        }

        list.add(new BasicDto.ScheduleDto(step, dateStr, status));
    }

    // 범위 문자열을 위한 포맷팅 메서드
    private String formatSharesRange(String shares) {
        if (shares == null || shares.isEmpty() || shares.equals("미정")) {
            return shares;
        }

        // 1. 기존에 혹시 있을지 모를 콤마(,)를 제거하고 숫자를 찾습니다.
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(shares.replace(",", ""));
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            try {
                // 2. 찾은 숫자를 파싱하여 3자리 콤마 + '주' 포맷으로 변경
                long value = Long.parseLong(matcher.group());
                matcher.appendReplacement(sb, String.format("%,d주", value));
            } catch (NumberFormatException e) {
                // 파싱 실패 시 해당 부분은 원본 유지 (안전 장치)
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
