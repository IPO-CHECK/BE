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

    private BasicDto basic;

    private FinancialsDto financials;

    private CompareDto compare;
}
