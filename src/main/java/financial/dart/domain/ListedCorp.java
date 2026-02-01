package financial.dart.domain;


import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "listed_corp")
public class ListedCorp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="corp_code", length = 8, nullable = false, unique = true)
    private String corpCode;

    @Column(name="corp_name", length = 200, nullable = false)
    private String corpName;

    @Column(name="stock_code", length = 20)
    private String stockCode;

    @Column(name="market", length = 30)
    private String market;

    @Column(name="industry", length = 200)
    private String industry;

    @Lob
    @Column(name="main_products", columnDefinition = "LONGTEXT")
    private String mainProducts;

    @Column(name="listed_date")
    private LocalDate listedDate;

    @Column(name="fiscal_month", length = 20)
    private String fiscalMonth;

    @Column(name="ceo_name", length = 200)
    private String ceoName;

    @Column(name="homepage", length = 500)
    private String homepage;

    @Column(name="region", length = 200)
    private String region;

    protected ListedCorp() {}

    public ListedCorp(String corpCode, String corpName) {
        this.corpCode = corpCode;
        this.corpName = corpName;
    }

    // --- getters ---
    public Long getId() { return id; }
    public String getCorpCode() { return corpCode; }
    public String getCorpName() { return corpName; }
    public String getStockCode() { return stockCode; }
    public String getMarket() { return market; }
    public String getIndustry() { return industry; }
    public String getMainProducts() { return mainProducts; }
    public LocalDate getListedDate() { return listedDate; }
    public String getFiscalMonth() { return fiscalMonth; }
    public String getCeoName() { return ceoName; }
    public String getHomepage() { return homepage; }
    public String getRegion() { return region; }

    // --- setters (import용) ---
    public void setCorpName(String corpName) { this.corpName = corpName; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }
    public void setMarket(String market) { this.market = market; }
    public void setIndustry(String industry) { this.industry = industry; }
    public void setMainProducts(String mainProducts) { this.mainProducts = mainProducts; }
    public void setListedDate(LocalDate listedDate) { this.listedDate = listedDate; }
    public void setFiscalMonth(String fiscalMonth) { this.fiscalMonth = fiscalMonth; }
    public void setCeoName(String ceoName) { this.ceoName = ceoName; }
    public void setHomepage(String homepage) { this.homepage = homepage; }
    public void setRegion(String region) { this.region = region; }
}