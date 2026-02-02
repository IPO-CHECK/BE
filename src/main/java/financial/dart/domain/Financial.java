package financial.dart.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Financial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4)
    private String bsnsYear;    // 사업연도 (예: 2025)

    @Column(nullable = false, length = 5)
    private String reprtCode;   // 보고서 코드 (11013, 11012, 11014, 11011)

    // --- [ 손익계산서 (IS) 항목 ] ---
    private Long revenue;          // 매출액
    private Long op;               // 영업이익
    private Long ni;               // 당기순이익
    private Long grossProfit;      // 매출총이익
    private Long financeCosts;       // 금융비용(이자)

    // --- [ 재무상태표 (BS) 항목 ] ---
    private Long totalAssets;      // 자산총계
    private Long totalLiabilities; // 부채총계
    private Long totalEquity;      // 자본총계
    private Long capStock;         // 자본금
    private Long curAssets;        // 유동자산
    private Long curLiabilities;   // 유동부채

    // --- [ 성장성(YoY) 계산용 전년 동기 데이터 ] ---
    private Long prevRevenue;      // 전기 매출액
    private Long prevOp;           // 전기 영업이익
    private Long prevNi;           // 전기 당기순이익
    private Long prevTotalAssets;  // 전기 자산총계

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporation_id")
    private Corporation corporation; // 회사 정보 (외래키)

//    public double[] getAnalysisVector() {
//        return new double[]{
//                getRevenueGrowth(),  // 1. 매출액 증가율 (성장)
//                getOpGrowth(),       // 2. 영업이익 증가율 (성장)
//                getNiGrowth(),       // 3. 순이익 증가율 (성장)
//                getOpMargin(),       // 4. 영업이익률 (수익)
//                getNiMargin(),       // 5. 순이익률 (수익)
//                getAssetTurnover()   // 6. 자산회전율 (효율/구조)
//        };
//    }

    public double[] getAnalysisVector() {
        // 1. 숫자가 너무 커서 다른 지표를 잡아먹는 '증가율' 형님들은 로그 처리
        double v1 = applyLog(getRevenueGrowth());
        double v2 = applyLog(getOpGrowth());
        double v3 = applyLog(getNiGrowth());

        // 2. 얌전한 '비율' 친구들은 그대로 (혹은 자산회전율만 x100 등 스케일 보정)
        double v4 = getOpMargin();
        double v5 = getNiMargin();
        double v6 = getAssetTurnover() * 10; // 회전율이 너무 작으면(0.15) x10 정도 해줘도 됨

        return new double[] { v1, v2, v3, v4, v5, v6 };
    }

    // ==========================================
    // ① 수익성 지표 (Profitability)
    // ==========================================

    public double getGrossProfitMargin() { // 매출총이익률
        return calculateRatio(grossProfit, revenue);
    }

    public double getOpMargin() { // 매출액 영업이익률
        return calculateRatio(op, revenue);
    }

    public double getRoe() { // 자기자본이익률 (ROE)
        return calculateRatio(ni, totalEquity);
    }

    public double getNiMargin() { // 순이익률
        return calculateRatio(ni, revenue);
    }

    public double getNiGrowth() { // 순이익 성장률 (순이익 증가율)
        return calculateGrowth(ni, prevNi);
    }

    // ==========================================
    // ② 성장성 지표 (Growth)
    // ==========================================

    public double getRevenueGrowth() { // 매출액 증가율
        return calculateGrowth(revenue, prevRevenue);
    }

    public double getOpGrowth() { // 영업이익 성장률 (영업이익 증가율)
        return calculateGrowth(op, prevOp);
    }

    public double getAssetGrowth() { // 총자산 증가율
        return calculateGrowth(totalAssets, prevTotalAssets);
    }

    // ==========================================
    // ③ 안정성 지표 (Stability)
    // ==========================================

    public double getDebtRatio() { // 부채비율
        return calculateRatio(totalLiabilities, totalEquity);
    }

    public double getInterestCoverageRatio() { // 이자보상비율
        if (financeCosts == null || financeCosts == 0) return 0.0;
        return (double) op / financeCosts; // 배수이므로 * 100 안 함
    }

    public double getCurrentRatio() { // 유동비율
        return calculateRatio(curAssets, curLiabilities);
    }

    /**
     * 자산회전율 (Asset Turnover)
     * 의미: 자산을 얼마나 효율적으로 굴려서 매출을 만들었는가?
     * 공식: 매출액 / 자산총계
     * (주의: 이건 %가 아니라 '배수'입니다. 1.5배, 0.8배 등)
     */
    public double getAssetTurnover() {
        if (revenue == null || totalAssets == null || totalAssets == 0) return 0.0;
        return (double) revenue / totalAssets; // * 100 안 함!
    }

    // ==========================================
    // 유틸리티 메서드 (NPE 및 0 나누기 방지)
    // ==========================================

    private double calculateRatio(Long numerator, Long denominator) {
        if (numerator == null || denominator == null || denominator == 0) return 0.0;
        return (double) numerator / denominator * 100;
    }

    private double calculateGrowth(Long current, Long previous) {
        if (current == null || previous == null || previous == 0) return 0.0;
        return (double) (current - previous) / previous * 100;
    }

    // 로그 변환 헬퍼 (음수 처리 포함)
    private double applyLog(Double val) {
        if (val == null || val == 0) return 0.0;
        // 부호는 살리고 절대값에만 로그 적용 (900 -> 6.8, -20 -> -3.0)
        return Math.signum(val) * Math.log10(Math.abs(val) + 1);
    }
}
