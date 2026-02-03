package financial.dart.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailDto {

    // --- 1. 기본 정보 ---
    private BasicDto basic;

    // --- 3. 재무제표 (Financials) ---
    private FinancialsDto financials;

    // --- 4. 유사 기업 (Peers) ---
    private List<PeerDto> peers;

    // --- 5. 심층 지표 (Deep Metrics) ---
    private DeepMetricsDto deepMetrics;

    // --- 6. 가치 평가 (Valuation) ---
    private ValuationDto valuation;

    // --- 7. 리스크 리포트 (Risk Report) ---
    private RiskReportDto riskReport;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeerDto {
        private Long id;
        private String name;
        private String marketCap;
        private String per;
        private String pbr;
    }

    // --- Deep Metrics Structure ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeepMetricsDto {
        private MetricCategoryDto growth;
        private MetricCategoryDto profit;
        private MetricCategoryDto stability;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricCategoryDto {
        private String label;
        private List<MetricItemDto> items;
        // 키값(revGrowth 등)이 동적이므로 Map 사용
        private Map<String, MetricChartDataDto> data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricItemDto {
        private String key;
        private String name;
        private String unit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricChartDataDto {
        private List<Number> target; // int와 double 혼용 가능성 고려 Number
        private List<Number> avg;
        // Peer ID("101")가 키값이므로 Map 사용
        private Map<String, List<Number>> peers;
    }

    // --- Valuation Structure ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValuationDto {
        private ValuationScenarioDto conservative;
        private ValuationScenarioDto standard;
        private ValuationScenarioDto aggressive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValuationScenarioDto {
        private String label;
        private String modelName;
        private String price;
        private String gap;
        private String desc;
        private String formula;
        private List<ValuationItemDto> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValuationItemDto {
        private String name;
        private String value;
    }

    // --- Risk Report Structure ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskReportDto {
        private String grade;
        private Integer score;
        private List<String> aiSummary;
        private List<RiskFactorDto> factors;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskFactorDto {
        private String title;
        private String desc;
        private String severity; // "high", "medium", "low"
    }
}
