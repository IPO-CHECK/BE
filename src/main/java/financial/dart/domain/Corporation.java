package financial.dart.domain;

import jakarta.persistence.Column;
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

    // --- [ 기본 정보 ] ---
    private String corpCode;
    private String corpName;
    private String stockCode;
    private String modifyDate;
    private String market;
    private String industry;

    @Column(length = 1000)
    private String majorProducts;      // 주요 제품
    private String hopePriceBand;      // 희망공모가 (19,000~26,000)
    private String finalOfferPrice;    // 확정공모가 (미정 or 숫자)
    private Long totalOfferShares;     // 공모 주식수 (2,000,000)
    private String generalOfferRatio;  // 일반청약 비율 (범위 문자열)
    private String generalOfferShares; // 일반청약 배정물량 (범위 문자열)
    private String underwriter;        // 주관사 (한국투자증권 등)

    // --- [ IPO 일정 (문자열로 관리) ] ---
    private String preliminaryReviewDate; // 심사청구일
    private String approvalDate;          // 심사승인일
    private String subscriptionNoticeDate;// 청약공고일
    private String subscriptionDate;      // 청약기일 (기간)
    private String paymentDate;           // 납입기일
    private String allocationDate;        // 배정공고일
    private String listingDate;           // 상장일 (미정)

    // [유사 기업 종목 정보]
    private Long marketCap;    // 시가총액
    private Double per;        // 주가수익비율
    private Double pbr;        // 주가순자산비율

    // --- [ 상장 적격성 판단 기준 ] ---
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
