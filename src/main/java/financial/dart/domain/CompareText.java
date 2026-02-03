package financial.dart.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "compare_text")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CompareText {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    private Long corpId;
    private Long compareCorpId;
    @Column(columnDefinition = "TEXT")
    private String revGrowth;
    @Column(columnDefinition = "TEXT")
    private String niGrowth;
    @Column(columnDefinition = "TEXT")
    private String assetGrowth;
    @Column(columnDefinition = "TEXT")
    private String gpm;
    @Column(columnDefinition = "TEXT")
    private String opm;
    @Column(columnDefinition = "TEXT")
    private String roe;
    @Column(columnDefinition = "TEXT")
    private String debtRatio;
    @Column(columnDefinition = "TEXT")
    private String intCov;
    @Column(columnDefinition = "TEXT")
    private String capRatio;
}
