package financial.dart.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "corp_fin_ratio")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CorpFinRatio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사업연도
    @Column(name = "bsns_year", nullable = false)
    private Integer bsnsYear;

    // 매출액 증가율 (%)
    @Column(name = "revGrowth", precision = 10, scale = 2)
    private BigDecimal revGrowth;

    // 순이익 증가율 (%)
    @Column(name = "niGrowth", precision = 10, scale = 2)
    private BigDecimal niGrowth;

    // 총자산 증가율 (%)
    @Column(name = "assetGrowth", precision = 10, scale = 2)
    private BigDecimal assetGrowth;

    // 매출총이익률
    @Column(nullable = false)
    private Float gpm;

    // 영업이익률
    @Column(nullable = false)
    private Float opm;

    // 자기자본이익률
    @Column
    private Float roe;

    // 부채비율 (%)
    @Column(name = "debtRatio", precision = 10, scale = 2)
    private BigDecimal debtRatio;

    // 이자보상비율 (배수)
    @Column(name = "intCov", precision = 10, scale = 2)
    private BigDecimal intCov;

    // 자기자본비율 (%)
    @Column(name = "capRatio", precision = 10, scale = 2)
    private BigDecimal capRatio;

    // 회사 (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporation_id", nullable = false)
    private Corporation corporation;

    // 생성일시
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 수정일시
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}