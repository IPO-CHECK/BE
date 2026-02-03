package financial.dart.controller;

import financial.dart.domain.Financial;
import financial.dart.domain.IpoBusinessAnalysis;
import financial.dart.domain.UpcomingIpo;
import financial.dart.domain.UpcomingIpoRiskAnalysis;
import financial.dart.dto.*;
import financial.dart.repository.ListedCorpRepository;
import financial.dart.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/upcoming-ipo")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowedHeaders = "*")
@Slf4j
@RequiredArgsConstructor
public class UpcomingIpoController {

    private final UpcomingIpoService upcomingIpoService;
    private final UpcomingIpoSimilarService upcomingIpoSimilarService;
    private final UpcomingIpoRiskAnalysisService riskAnalysisService;
    private final IpoBusinessAnalysisService businessAnalysisService;
    private final CorporationService corporationService;
    private final FinancialService financialService;
    private final SimilarityService similarityService;
    private final ListedCorpRepository listedCorpRepository;

    // 메인 화면에서 신규 상장 종목 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<List<UpcomingDto>> mainPageList() {
        return ResponseEntity.ok(upcomingIpoService.mainPageList());
    }

    // 상세 정보 -> 유사도 분석
    @GetMapping("/{id}/details")
    public ResponseEntity<Void> test(@PathVariable Long id) {
        String corpCode = upcomingIpoService.findCorpCodeById(id);
        log.info("🔍 신규 상장 종목 Corp Code: {}", corpCode);

        Long corpId = corporationService.findCorporationIdByCorpCode(corpCode);
        log.info("🔍 신규 상장 종목 Corporation ID: {}", corpId);

        // 신규 상장 종목의 재무제표 조회
        Financial targetFinancial = financialService.findByCorporationId(corpId);
        log.info("🎯 타겟 종목: {}, 매출액={}, 자산총계={}, 자본총계={}",
                targetFinancial.getCorporation().getCorpName(),
                targetFinancial.getRevenue(),
                targetFinancial.getTotalAssets(),
                targetFinancial.getTotalEquity());

        // 0. 분류 및 품목 필터링
        List<Long> listedCorpIds = upcomingIpoSimilarService.findSimilar(id);
        List<String> corpCodes = listedCorpRepository.findCorpCodesByIdIn(listedCorpIds);
        List<Long> corpIds = corpCodes.stream()
                .map(corporationService::findCorporationIdByCorpCode)
                .toList();
        log.info("🔍 후보군 Corp Codes: {}", corpCodes);

        // 1. 규모 0.2배 ~ 5배 이내 후보군 추출
        List<Financial> financials = financialService.findSimilarCorporations(corpIds, targetFinancial, "2025", 3);

        for (Financial f : financials) {
            log.info("후보 종목: {}, 매출액={}, 자산총계={}, 자본총계={}",
                    f.getCorporation().getCorpName(),
                    f.getRevenue(),
                    f.getTotalAssets(),
                    f.getTotalEquity());
        }

        // 2. 후보군 중 코사인 유사도 TOP 3개 선정
        List<SimilarityService.SimilarityResult> top3Results = similarityService.findTopSimilarCorp(targetFinancial, financials, 3);

        String[] labels = {"매출증가율", "영업이익증가율", "순익증가율", "영업이익률", "순이익률", "자산회전율"};

        log.info("🎯 [타겟] {} : {}",
                targetFinancial.getCorporation().getCorpName(),
                formatVector(targetFinancial.getAnalysisVector(), labels));

        log.info("{}", top3Results.size());

        int rank = 1;
        for (SimilarityService.SimilarityResult res : top3Results) {
            log.info("-------------------------");
            log.info("Analyzing TOP{}: {}", rank, res.getFinancial().getCorporation().getCorpName());
            double[] zScores = res.getVector(); // 정규화된 값
            double[] rawVector = res.getFinancial().getAnalysisVector(); // 원본 값

            String rawStr = formatVector(rawVector, labels);
            String zStr = formatVector(zScores, labels);

            log.info("🥈 TOP{} {} (점수: {})\n\t└─ 📊 Raw Data: {}\n\t└─ 📐 Z-Score : {}",
                    rank++,
                    res.getFinancial().getCorporation().getCorpName(),
                    String.format("%.4f", res.getScore()),
                    rawStr,
                    zStr);
        }

        return ResponseEntity.ok().build();
    }

    // 상세 정보 (기본 재무정보 조회)
    @GetMapping("/{id}/financials")
    public ResponseEntity<DetailDto> getFinancials(@PathVariable Long id) {
        String corpCode = upcomingIpoService.findCorpCodeById(id);

        // 1. 상단 기본 재무 정보 조회
        BasicDto basic = corporationService.getBasicDetail(corpCode);

        // 2. 실적추이 (매출액, 영업이익, 순이익)
        Long corpId = corporationService.findCorporationIdByCorpCode(corpCode);
        FinancialsDto financials = financialService.getFinancials(corpId);

        CompareDto compare = corporationService.getCompareDetail(corpId);

        DetailDto detailDto = DetailDto.builder()
                .basic(basic)
                .financials(financials)
                .compare(compare)
                .build();

        return ResponseEntity.ok(detailDto);
    }

    // 유사 기업 분석 텍스트 조회
    @GetMapping("/{id}/analysis/insights")
    public ResponseEntity<AnalysisDto> getSimilarityAnalysis(@PathVariable Long id) {
        String corpCode = upcomingIpoService.findCorpCodeById(id);
        return ResponseEntity.ok(corporationService.getAnalysisText(corpCode));
    }

    // 리스크 분석
    @GetMapping("/{id}/risk-analysis")
    public ResponseEntity<RiskAnalysisResponse> riskAnalysis(@PathVariable Long id) {
        UpcomingIpoRiskAnalysis analysis = riskAnalysisService.getOrCreate(id);
        return ResponseEntity.ok(new RiskAnalysisResponse(
                analysis.getUpcomingIpo().getId(),
                analysis.getRceptNo(),
                analysis.getKeyRiskText(),
                analysis.getAnalysisText(),
                analysis.getUpdatedAt().toString()
        ));
    }

    /**
     * @param id upcoming_ipo_id (FK). ipo_business_analysis.pk가 아님. 없으면 200 + 빈 본문.
     */
    @GetMapping("/{id}/business-analysis")
    public ResponseEntity<BusinessAnalysisResponse> businessAnalysis(@PathVariable Long id) {
        var opt = businessAnalysisService.findByUpcomingIpoId(id);
        if (opt.isEmpty()) {
            return ResponseEntity.ok(BusinessAnalysisResponse.empty());
        }
        return ResponseEntity.ok(BusinessAnalysisResponse.from(opt.get()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UpcomingIpo> get(@PathVariable Long id) {
        return ResponseEntity.ok(upcomingIpoService.getById(id));
    }

//    @GetMapping("/{id}/similar")
//    public ResponseEntity<UpcomingIpoSimilarResponse> similar(@PathVariable Long id) {
//        return ResponseEntity.ok(upcomingIpoSimilarService.findSimilar(id));
//    }

    @PostMapping("/refresh")
    public ResponseEntity<List<UpcomingIpo>> refresh() {
        return ResponseEntity.ok(upcomingIpoService.refreshFrom38());
    }

    @GetMapping
    public ResponseEntity<List<UpcomingIpo>> list() {
        return ResponseEntity.ok(upcomingIpoService.listAll());
    }

    public record RiskAnalysisResponse(
            Long upcomingIpoId,
            String rceptNo,
            String keyRiskText,
            String analysisText,
            String updatedAt
    ) {
    }

    public record BusinessAnalysisResponse(
            String overallSummary,
            java.util.List<CategoryItem> categories
    ) {
        public record CategoryItem(String title, String grade, String reason, String gradeColor) {
        }

        /**
         * 데이터 없을 때 200 OK로 내려줄 빈 응답 (프론트에서 목업으로 대체)
         */
        public static BusinessAnalysisResponse empty() {
            return new BusinessAnalysisResponse("", java.util.List.of());
        }

        public static BusinessAnalysisResponse from(IpoBusinessAnalysis a) {
            String monetizationGrade = withDefaultGrade(a.getMonetizationGrade());
            String scalabilityGrade = withDefaultGrade(a.getScalabilityGrade());
            String structuralRiskGrade = withDefaultGrade(a.getStructuralRiskGrade());
            String resourceCapabilityGrade = withDefaultGrade(a.getResourceCapabilityGrade());
            return new BusinessAnalysisResponse(
                    a.getSummaryFinal() != null ? a.getSummaryFinal() : "",
                    java.util.List.of(
                            new CategoryItem(
                                    "수익화 구조 (Revenue Structure)",
                                    monetizationGrade,
                                    nullToEmpty(a.getMonetizationStructure()),
                                    gradeColor(monetizationGrade)
                            ),
                            new CategoryItem(
                                    "확장성 (Scalability)",
                                    scalabilityGrade,
                                    nullToEmpty(a.getScalability()),
                                    gradeColor(scalabilityGrade)
                            ),
                            new CategoryItem(
                                    "구조적 리스크 (Structural Risk)",
                                    structuralRiskGrade,
                                    nullToEmpty(a.getStructuralRisk()),
                                    gradeColor(structuralRiskGrade)
                            ),
                            new CategoryItem(
                                    "자원 확보 (Resource Investment)",
                                    resourceCapabilityGrade,
                                    nullToEmpty(a.getResourceCapability()),
                                    gradeColor(resourceCapabilityGrade)
                            )
                    )
            );
        }

        private static String nullToEmpty(String s) {
            return s != null ? s : "";
        }

        private static String withDefaultGrade(String grade) {
            if (grade == null || grade.isBlank()) {
                return "중";
            }
            return grade;
        }

        private static String gradeColor(String grade) {
            if ("상".equals(grade)) {
                return "text-green-600 bg-green-50";
            }
            if ("하".equals(grade)) {
                return "text-red-600 bg-red-50";
            }
            // 기본값 및 그 외
            return "text-amber-600 bg-amber-50";
        }
    }

    private String formatVector(double[] vec, String[] labels) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vec.length && i < labels.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(labels[i]).append("=").append(String.format("%.4f", vec[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
