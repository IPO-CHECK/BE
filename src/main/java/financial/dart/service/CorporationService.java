package financial.dart.service;

import financial.dart.domain.CompareText;
import financial.dart.domain.CorpFinRatio;
import financial.dart.domain.Corporation;
import financial.dart.dto.AnalysisDto;
import financial.dart.dto.BasicDto;
import financial.dart.dto.CompareDto;
import financial.dart.repository.CorporationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorporationService {

    @Value("${dart.api-key}")
    private String apiKey;

    private final CorporationRepository corporationRepository;
    private final RestTemplate restTemplate;

    public AnalysisDto getAnalysisText(String corpCode) {
        Long corpId = corporationRepository.findIdByCorpCode(corpCode).orElse(null);
        List<CompareText> compareTexts = corporationRepository.findByCorpId(corpId);

        // 1. 매출액증가율
        Map<String, String> revGrowth = new HashMap<>();
        for (CompareText ct : compareTexts) {
            revGrowth.put(String.valueOf(ct.getCompareCorpId()), ct.getRevGrowth());
        }
        // 2. 순이익증가율
        Map<String, String> niGrowth = new HashMap<>();
        for (CompareText ct : compareTexts) {
            niGrowth.put(String.valueOf(ct.getCompareCorpId()), ct.getNiGrowth());
        }
        // 3. 총자산증가율
        Map<String, String> assetGrowth = new HashMap<>();
        for (CompareText ct : compareTexts) {
            assetGrowth.put(String.valueOf(ct.getCompareCorpId()), ct.getAssetGrowth());
        }
        // 4. 매출총이익률
        Map<String, String> gpm = new HashMap<>();
        for (CompareText ct : compareTexts) {
            gpm.put(String.valueOf(ct.getCompareCorpId()), ct.getGpm());
        }
        // 5. 영업이익률
        Map<String, String> opm = new HashMap<>();
        for (CompareText ct : compareTexts) {
            opm.put(String.valueOf(ct.getCompareCorpId()), ct.getOpm());
        }
        // 6. ROE
        Map<String, String> roe = new HashMap<>();
        for (CompareText ct : compareTexts) {
            roe.put(String.valueOf(ct.getCompareCorpId()), ct.getRoe());
        }
        // 7. 부채비율
        Map<String, String> debtRatio = new HashMap<>();
        for (CompareText ct : compareTexts) {
            debtRatio.put(String.valueOf(ct.getCompareCorpId()), ct.getDebtRatio());
        }
        // 8. 이자보상배율
        Map<String, String> intCov = new HashMap<>();
        for (CompareText ct : compareTexts) {
            intCov.put(String.valueOf(ct.getCompareCorpId()), ct.getIntCov());
        }
        // 9. 자기자본비율
        Map<String, String> capRatio = new HashMap<>();
        for (CompareText ct : compareTexts) {
            capRatio.put(String.valueOf(ct.getCompareCorpId()), ct.getCapRatio());
        }

        Map<String, Map<String, String>> analysisMap = new HashMap<>();
        analysisMap.put("revGrowth", revGrowth);
        analysisMap.put("niGrowth", niGrowth);
        analysisMap.put("assetGrowth", assetGrowth);
        analysisMap.put("gpm", gpm);
        analysisMap.put("opm", opm);
        analysisMap.put("roe", roe);
        analysisMap.put("debtRatio", debtRatio);
        analysisMap.put("intCov", intCov);
        analysisMap.put("capRatio", capRatio);
        return new AnalysisDto(analysisMap);
    }

    /**
     * [심층 비교 분석]
     * 내 기업(Target)과 유사 기업(Peers)의 재무 비율 및 성장성 지표를 비교하여 DTO로 반환합니다.
     */
    public CompareDto getCompareDetail(Long corpId) {
        // 1. Target(내 기업)과 Peers(유사 기업) ID 식별
        List<Long> peerIds = corporationRepository.findTargetCorpIdsByCorpId(corpId);

        // 모든 관련 ID 리스트 (Target + Peers)
        List<Long> allIds = new ArrayList<>();
        allIds.add(corpId);
        allIds.addAll(peerIds);

        // 2. 기업 정보 및 재무 비율 데이터 일괄 조회
        // Corporation 정보 조회 (시총, PER, PBR 등)
        List<Corporation> allCorps = corporationRepository.findAllById(allIds);
        // 재무 비율 데이터 조회 (연도별 오름차순 정렬됨)
        List<CorpFinRatio> allRatios = corporationRepository.findByCorporationIdInOrderByBsnsYearAsc(allIds);

        // 3. 데이터를 Map으로 그룹화 (Key: CorpId, Value: 비율 리스트 / Corporation 객체)
        Map<Long, List<CorpFinRatio>> ratioMap = allRatios.stream()
                .collect(Collectors.groupingBy(r -> r.getCorporation().getId()));

        Map<Long, Corporation> corpMap = allCorps.stream()
                .collect(Collectors.toMap(Corporation::getId, c -> c));

        // 4. PeerDto 리스트 생성 (경쟁사 기본 정보)
        List<CompareDto.PeerDto> peerDtos = peerIds.stream()
                .map(id -> {
                    Corporation c = corpMap.get(id);
                    // c가 null일 경우(DB 정합성 문제) 안전하게 처리
                    if (c == null) return null;

                    return new CompareDto.PeerDto(
                            c.getId(),
                            c.getCorpName(),
                            formatMarketCap(c.getMarketCap()), // 시가총액 포맷팅 (예: 1조 1500억)
                            formatRatio(c.getPer()),           // PER 포맷팅 (예: 16.50배)
                            formatRatio(c.getPbr())            // PBR 포맷팅 (예: 1.20배)
                    );
                })
                .filter(Objects::nonNull) // null 제외
                .collect(Collectors.toList());

        // 5. 심층 지표 (Deep Metrics) 구성
        // 각 카테고리별(성장성, 수익성, 안정성) 차트 데이터 생성

        // (1) 성장성 (BigDecimal 타입 필드)
        CompareDto.MetricCategoryDto growth = buildMetricCategory(
                "성장성", corpId, peerIds, ratioMap,
                List.of(
                        new MetricDef("revGrowth", "매출액증가율", "%", CorpFinRatio::getRevGrowth),
                        new MetricDef("niGrowth", "순이익증가율", "%", CorpFinRatio::getNiGrowth),
                        new MetricDef("assetGrowth", "총자산증가율", "%", CorpFinRatio::getAssetGrowth)
                )
        );

        // (2) 수익성 (Float 타입 필드)
        CompareDto.MetricCategoryDto profit = buildMetricCategory(
                "수익성", corpId, peerIds, ratioMap,
                List.of(
                        new MetricDef("gpm", "매출총이익률", "%", CorpFinRatio::getGpm),
                        new MetricDef("opm", "영업이익률", "%", CorpFinRatio::getOpm),
                        new MetricDef("roe", "ROE", "%", CorpFinRatio::getRoe)
                )
        );

        // (3) 안정성 (BigDecimal 타입 필드)
        CompareDto.MetricCategoryDto stability = buildMetricCategory(
                "안정성", corpId, peerIds, ratioMap,
                List.of(
                        new MetricDef("debtRatio", "부채비율", "%", CorpFinRatio::getDebtRatio),
                        new MetricDef("intCov", "이자보상배율", "배", CorpFinRatio::getIntCov),
                        new MetricDef("capRatio", "자기자본비율", "%", CorpFinRatio::getCapRatio)
                )
        );

        // 6. 최종 DTO 반환
        return CompareDto.builder()
                .peers(peerDtos)
                .deepMetrics(new CompareDto.DeepMetricsDto(growth, profit, stability))
                .build();
    }

    // --- [심층 지표 생성을 위한 내부 레코드 및 메서드] ---

    // 지표 정의 레코드: 키, 이름, 단위, 값 추출 함수(Number 타입 반환)
    private record MetricDef(String key, String name, String unit, Function<CorpFinRatio, Number> extractor) {
    }

    // 카테고리 빌더 (Items + Data 생성)
    private CompareDto.MetricCategoryDto buildMetricCategory(
            String label,
            Long targetId,
            List<Long> peerIds,
            Map<Long, List<CorpFinRatio>> ratioMap,
            List<MetricDef> metricDefs
    ) {
        // 1. Items 생성 (선택 가능한 지표 목록)
        List<CompareDto.MetricItemDto> items = metricDefs.stream()
                .map(def -> new CompareDto.MetricItemDto(def.key(), def.name(), def.unit()))
                .collect(Collectors.toList());

        // 2. Data Map 생성 (실제 차트 데이터)
        Map<String, CompareDto.MetricChartDataDto> dataMap = new HashMap<>();

        for (MetricDef def : metricDefs) {
            dataMap.put(def.key(), createChartData(targetId, peerIds, ratioMap, def.extractor()));
        }

        return CompareDto.MetricCategoryDto.builder()
                .label(label)
                .items(items)
                .data(dataMap)
                .build();
    }

    // 차트 데이터 생성 (Target, Peers, Avg 계산)
    private CompareDto.MetricChartDataDto createChartData(
            Long targetId,
            List<Long> peerIds,
            Map<Long, List<CorpFinRatio>> ratioMap,
            Function<CorpFinRatio, Number> valueExtractor
    ) {
        // 1. Target Data 추출
        List<Number> targetData = extractValues(ratioMap.get(targetId), valueExtractor);

        // 2. Peers Data 추출 및 Map 생성
        Map<String, List<Number>> peersDataMap = new HashMap<>();
        List<List<Number>> allPeersValues = new ArrayList<>(); // 평균 계산용

        for (Long peerId : peerIds) {
            List<Number> pValues = extractValues(ratioMap.get(peerId), valueExtractor);
            peersDataMap.put(String.valueOf(peerId), pValues);

            if (!pValues.isEmpty()) {
                allPeersValues.add(pValues);
            }
        }

        // 3. Avg Data (업계 평균) 계산
        List<Number> avgData = calculateAverage(allPeersValues);

        return CompareDto.MetricChartDataDto.builder()
                .target(targetData)
                .peers(peersDataMap)
                .avg(avgData)
                .build();
    }

    // 값 추출 (Null 처리 및 Number 타입 통일)
    private List<Number> extractValues(List<CorpFinRatio> ratios, Function<CorpFinRatio, Number> valueExtractor) {
        if (ratios == null || ratios.isEmpty()) {
            return Collections.emptyList();
        }
        return ratios.stream()
                .map(ratio -> {
                    Number val = valueExtractor.apply(ratio);
                    // 데이터가 null이면 0으로 처리 (차트 깨짐 방지)
                    return val != null ? val : 0;
                })
                .collect(Collectors.toList());
    }

    // 연도별 평균 계산
    private List<Number> calculateAverage(List<List<Number>> allPeersValues) {
        if (allPeersValues.isEmpty()) return Collections.emptyList();

        // 첫 번째 Peer의 데이터 길이를 기준으로 연도 개수 설정 (보통 3~4년치)
        int years = allPeersValues.get(0).size();
        List<Number> averages = new ArrayList<>();

        for (int i = 0; i < years; i++) {
            double sum = 0;
            int count = 0;
            for (List<Number> peerVals : allPeersValues) {
                // 데이터 길이가 다를 경우 안전하게 처리
                if (i < peerVals.size()) {
                    sum += peerVals.get(i).doubleValue();
                    count++;
                }
            }
            // 소수점 둘째 자리까지 반올림
            double avg = count > 0 ? sum / count : 0.0;
            averages.add(Math.round(avg * 100.0) / 100.0);
        }
        return averages;
    }

    // --- [기본 정보 조회 관련 메서드] ---

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

    public Long findCorporationIdByCorpCode(String corpCode) {
        return corporationRepository.findIdByCorpCode(corpCode).orElse(null);
    }

    public List<Corporation> getCorps() {
        return corporationRepository.findCorps();
    }

    public List<Long> findQualifiedCorpIds() {
        return corporationRepository.findQualifiedCorporationIds();
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

    // --- [ 상장 적격성 판단 로직 (DART API 연동) ] ---

    // 1. 상장 후 3개월 경과 여부 확인
    @Transactional
    public void checkListingDate(String corpCode) {
        String url = UriComponentsBuilder.fromUriString("https://opendart.fss.or.kr/api/list.json")
                .queryParam("crtfc_key", apiKey)
                .queryParam("corp_code", corpCode)
                .queryParam("bgn_de", "19500101")
                .queryParam("pblntf_ty", "A")
                .queryParam("sort", "date")
                .queryParam("sort_mth", "asc")
                .queryParam("page_count", "30")
                .toUriString();

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, String>> list = (List<Map<String, String>>) response.get("list");
            Corporation corporation = corporationRepository.findByCorpCode(corpCode);

            if (list == null || list.isEmpty()) {
                corporation.updateIsOver3Months(false);
                return;
            }

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
            corporation.updateIsOver3Months(result);

        } catch (Exception e) {
            log.error("Error checking listing date for {}: {}", corpCode, e.getMessage());
        }
    }

    // 2. 최근 2년간 감사의견 적정 여부 확인
    @Transactional
    public void checkAuditOpinion(String corpCode) {
        int currentYear = LocalDate.now().getYear();
        String[] years = {String.valueOf(currentYear - 1), String.valueOf(currentYear - 2)};
        Corporation corporation = corporationRepository.findByCorpCode(corpCode);

        for (String year : years) {
            String url = UriComponentsBuilder.fromUriString("https://opendart.fss.or.kr/api/accnutAdtorNmNdAdtOpinion.json")
                    .queryParam("crtfc_key", apiKey)
                    .queryParam("corp_code", corpCode)
                    .queryParam("bsns_year", year)
                    .queryParam("reprt_code", "11011")
                    .toUriString();

            try {
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response == null || response.get("list") == null) {
                    corporation.updateHasUnqualifiedOpinion(false);
                    return;
                }

                List<Map<String, String>> list = (List<Map<String, String>>) response.get("list");
                if (list.isEmpty()) {
                    corporation.updateHasUnqualifiedOpinion(false);
                    return;
                }

                String opinion = list.get(0).get("adt_opinion");
                if (opinion == null || !opinion.contains("적정")) {
                    corporation.updateHasUnqualifiedOpinion(false);
                    return;
                }

            } catch (Exception e) {
                log.error("Error checking audit opinion for {}: {}", corpCode, e.getMessage());
            }
        }
        corporation.updateHasUnqualifiedOpinion(true);
    }

    // 3. 최근 2년간 M&A 이슈 여부 확인
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

            try {
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                if (response != null && !"0".equals(String.valueOf(response.get("total_count")))) {
                    List<Map<String, String>> reports = (List<Map<String, String>>) response.get("list");
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
            } catch (Exception e) {
                log.error("Error checking M&A history for {}: {}", corpCode, e.getMessage());
            }
        }
        corporation.updateHasNoMajorChanges(true);
    }

    // --- [Helper Methods] ---

    // 일정 빌더
    private List<BasicDto.ScheduleDto> buildSchedule(Corporation corp) {
        List<BasicDto.ScheduleDto> schedules = new ArrayList<>();
        LocalDate today = LocalDate.now();

        addStep(schedules, "예비심사청구", corp.getPreliminaryReviewDate(), today);
        addStep(schedules, "심사승인", corp.getApprovalDate(), today);
        addStep(schedules, "청약공고", corp.getSubscriptionNoticeDate(), today);
        addStep(schedules, "청약기일", corp.getSubscriptionDate(), today);
        addStep(schedules, "납입기일", corp.getPaymentDate(), today);
        addStep(schedules, "배정공고", corp.getAllocationDate(), today);
        addStep(schedules, "상장일", corp.getListingDate(), today);

        boolean hasActive = schedules.stream().anyMatch(s -> "active".equals(s.getStatus()));

        if (!hasActive) {
            for (BasicDto.ScheduleDto schedule : schedules) {
                if ("future".equals(schedule.getStatus())) {
                    schedule.setStatus("active");
                    break;
                }
            }
        }
        return schedules;
    }

    private void addStep(List<BasicDto.ScheduleDto> list, String step, String dateStr, LocalDate today) {
        if (dateStr == null || dateStr.isEmpty()) return;

        String status = "future";
        try {
            String compareDate = dateStr.contains("~") ? dateStr.split("~")[0].trim() : dateStr;
            LocalDate targetDate = LocalDate.parse(compareDate.replace(".", "-"));

            if (targetDate.isBefore(today)) status = "done";
            else if (targetDate.isEqual(today)) status = "active";
        } catch (Exception e) {
            status = "future";
        }
        list.add(new BasicDto.ScheduleDto(step, dateStr, status));
    }

    // 주식수 범위 포맷팅
    private String formatSharesRange(String shares) {
        if (shares == null || shares.isEmpty() || shares.equals("미정")) return shares;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(shares.replace(",", ""));
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            try {
                long value = Long.parseLong(matcher.group());
                matcher.appendReplacement(sb, String.format("%,d주", value));
            } catch (NumberFormatException e) {
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    // 시가총액 포맷팅 (Long -> "1조 2000억" or "5000억")
    private String formatMarketCap(Long marketCap) {
        if (marketCap == null || marketCap == 0) return "-";
        long trillion = 1_000_000_000_000L;
        long hundredMillion = 100_000_000L;

        StringBuilder sb = new StringBuilder();
        long cho = marketCap / trillion;
        long uk = (marketCap % trillion) / hundredMillion;

        if (cho > 0) {
            sb.append(cho).append("조");
            if (uk > 0) sb.append(" ").append(uk).append("억");
        } else {
            sb.append(uk).append("억");
        }
        return sb.toString();
    }

    // 비율 포맷팅 (Double -> "12.34배")
    private String formatRatio(Double value) {
        if (value == null || value.isNaN()) return "-";
        return String.format("%.2f배", value);
    }
}