package financial.dart.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompareDto {

    private List<PeerDto> peers;

    private DeepMetricsDto deepMetrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeerDto {
        private Long id;
        private String name;
        private String marketCap; // 시가총액
        private String per;       // PER
        private String pbr;       // PBR
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeepMetricsDto {
        private MetricCategoryDto growth;    // 성장성
        private MetricCategoryDto profit;    // 수익성
        private MetricCategoryDto stability; // 안정성
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricCategoryDto {
        private String label; // 예: "성장성"
        private List<MetricItemDto> items; // 선택 가능한 지표 목록 (예: 매출액증가율, 영업이익증가율)

        // 차트 데이터 (Key: 지표 키값, Value: 차트 데이터 객체)
        private Map<String, MetricChartDataDto> data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricItemDto {
        private String key;   // 예: "revenueGrowth"
        private String name;  // 예: "매출액 증가율"
        private String unit;  // 예: "%"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricChartDataDto {
        // 타겟 기업(내 기업)의 데이터 (예: [2023, 2024, 2025])
        private List<Number> target;

        // 업계 평균 데이터
        private List<Number> avg;

        // 경쟁사별 데이터 (Key: Peer ID, Value: 데이터 리스트)
        private Map<String, List<Number>> peers;
    }
}