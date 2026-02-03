package financial.dart.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicDto {

    // --- 1. 기본 정보 ---
    private Long id;
    private String name;
    private String code;
    private String industry;
    private String market;
    private String products;
    private String expectedPrice;
    private String finalPrice;
    private String publicShares;
    private String generalShares;
    private String underwriter;

    // --- 2. 일정 (Schedule) ---
    private List<ScheduleDto> schedule;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleDto {
        private String step;
        private String date;
        private String status; // "done", "active", "future"
    }
}
