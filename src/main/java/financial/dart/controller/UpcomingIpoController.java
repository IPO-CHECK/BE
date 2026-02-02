package financial.dart.controller;

import financial.dart.domain.Financial;
import financial.dart.domain.UpcomingIpo;
import financial.dart.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/upcoming-ipo")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowedHeaders = "*")
@RequiredArgsConstructor
@Slf4j
public class UpcomingIpoController {

    private final UpcomingIpoService upcomingIpoService;
    private final UpcomingIpoSimilarService upcomingIpoSimilarService;
    private final CorporationService corporationService;
    private final FinancialService financialService;
    private final SimilarityService similarityService;

    @PostMapping("/refresh")
    public ResponseEntity<List<UpcomingIpo>> refresh() {
        return ResponseEntity.ok(upcomingIpoService.refreshFrom38());
    }

    // 메인 화면에서 신규 상장 종목 리스트 조회
    @GetMapping
    public ResponseEntity<List<UpcomingIpo>> list() {
        return ResponseEntity.ok(upcomingIpoService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UpcomingIpo> get(@PathVariable Long id) {
        return ResponseEntity.ok(upcomingIpoService.getById(id));
    }

//    @GetMapping("/{id}/similar")
//    public ResponseEntity<UpcomingIpoSimilarResponse> similar(@PathVariable Long id) {
//        return ResponseEntity.ok(upcomingIpoSimilarService.findSimilar(id));
//    }

    @GetMapping("/{id}/test")
    public ResponseEntity<Void> test(@PathVariable Long id) {
        // id는 신규 상장 종목의 UpcomingIpo PK
        String corpCode = upcomingIpoService.findCorpCodeById(id);
        log.info("🔍 신규 상장 종목 CorpCode: {}", corpCode);

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
        List<Long> corpIds = upcomingIpoSimilarService.findSimilar(id);
//        List<Long> corpIds = corporationService.findQualifiedCorpIds();

        // 1. 규모 0.2배 ~ 5배 이내 후보군 추출
        List<Financial> financials = financialService.findSimilarCorporations(corpIds, targetFinancial, "2025", 3);

        for (Financial f : financials) {
            log.info("후보 종목: {}, 매출액={}, 자산총계={}, 자본총계={}",
                    f.getCorporation().getCorpName(),
                    f.getRevenue(),
                    f.getTotalAssets(),
                    f.getTotalEquity());
        }

        // 2. 후보군 중 코사인 유사도 TOP 3개 선정, 어떻게 비교할 지 더 고민해야 함
        List<SimilarityService.SimilarityResult> top3Results = similarityService.findTopSimilarCorp(targetFinancial, financials, 3);

        String[] labels = {"매출증가율", "영업이익증가율", "순익증가율", "영업이익률", "순이익률", "자산회전율"};

        log.info("🎯 [타겟] {} : {}",
                targetFinancial.getCorporation().getCorpName(),
                formatVector(targetFinancial.getAnalysisVector(), labels));

        int rank = 1;
        for (SimilarityService.SimilarityResult res : top3Results) {
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
