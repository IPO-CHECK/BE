package financial.dart.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialsDto {

    private List<String> quarters;
    private List<Long> revenue;
    private List<Long> opProfit;
    private List<Long> netIncome;
}
