package financial.dart.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Corporation {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private String corpCode;
    private String corpName;
    private String stockCode;
    private String modifyDate;

    // 상장, 등록 후에 3개월이 경과했는지
    private boolean isOver3Months;
    // 최근 2년간 감사의견이 '적정'인지
    private boolean hasUnqualifiedOpinion;
    // 최근 2년간 합병, 영업의 양수, 분할이 없는지
    private boolean hasNoMajorChanges;

    public void updateIsOver3Months(boolean status) {
        this.isOver3Months = status;
    }

    public void updateHasUnqualifiedOpinion(boolean status) {
        this.hasUnqualifiedOpinion = status;
    }

    public void updateHasNoMajorChanges(boolean status) {
        this.hasNoMajorChanges = status;
    }
}
