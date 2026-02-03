package financial.dart.domain;

import jakarta.persistence.*;

@Entity
@Table(
        name = "upcoming_ipo",
        uniqueConstraints = @UniqueConstraint(name = "uk_upcoming_ipo_no", columnNames = "ipo_no")
)
public class UpcomingIpo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corp_name", nullable = false, length = 200)
    private String corpName;

    @Column(name = "stock_name", nullable = false, length = 200)
    private String stockName;

    @Column(name = "normalized_name", length = 200)
    private String normalizedName;

    @Column(name = "corp_code", length = 20)
    private String corpCode;

    @Column(name = "ipo_no", nullable = false, length = 30)
    private String ipoNo;

    @Column(name = "detail_url", length = 500)
    private String detailUrl;

    @Column(name = "rcept_no", length = 20)
    private String rceptNo;

    @Column(name = "industry", length = 500)
    private String industry;

    @Column(name = "underwriter", length = 500)
    private String underwriter;

    @Column(name = "sub_date", length = 200)
    private String subDate;

    @Column(name = "list_date", length = 200)
    private String listDate;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "price", length = 100)
    private String price;

    @Column(name = "expected_price", length = 100)
    private String expectedPrice;

    protected UpcomingIpo() {}

    public UpcomingIpo(String corpName, String normalizedName, String corpCode, String ipoNo, String detailUrl) {
        this.corpName = corpName;
        this.stockName = corpName;
        this.normalizedName = normalizedName;
        this.corpCode = corpCode;
        this.ipoNo = ipoNo;
        this.detailUrl = detailUrl;
    }

    public Long getId() { return id; }
    public String getCorpName() { return corpName; }
    public String getStockName() { return stockName; }
    public String getNormalizedName() { return normalizedName; }
    public String getCorpCode() { return corpCode; }
    public String getIpoNo() { return ipoNo; }
    public String getDetailUrl() { return detailUrl; }
    public String getRceptNo() { return rceptNo; }
    public String getIndustry() { return industry; }
    public String getUnderwriter() { return underwriter; }
    public String getSubDate() { return subDate; }
    public String getListDate() { return listDate; }
    public String getStatus() { return status; }
    public String getPrice() { return price; }
    public String getExpectedPrice() { return expectedPrice; }

    public void updateBasic(String corpName, String normalizedName, String corpCode, String detailUrl) {
        this.corpName = corpName;
        this.stockName = corpName;
        this.normalizedName = normalizedName;
        this.corpCode = corpCode;
        this.detailUrl = detailUrl;
    }

    public void updateRceptNo(String rceptNo) {
        this.rceptNo = rceptNo;
    }

    public void updateIndustry(String industry) {
        this.industry = industry;
    }

    public void updateOfferingInfo(
            String underwriter,
            String subDate,
            String listDate,
            String status,
            String price,
            String expectedPrice
    ) {
        this.underwriter = underwriter;
        this.subDate = subDate;
        this.listDate = listDate;
        this.status = status;
        this.price = price;
        this.expectedPrice = expectedPrice;
    }
}
