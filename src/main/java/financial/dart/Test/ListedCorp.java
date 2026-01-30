package financial.dart.Test;


import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
        name = "listed_corp",
        uniqueConstraints = @UniqueConstraint(name = "uk_stock_code", columnNames = "stock_code")
)
public class ListedCorp {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(nullable = false, length = 50)
    private String market;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    @Column(length = 300)
    private String industry;

    @Lob
    @Column(name = "main_products", columnDefinition = "TEXT")
    private String mainProducts;

    @Column(name = "listed_date")
    private LocalDate listedDate;

    @Column(name = "fiscal_month", length = 10)
    private String fiscalMonth;

    @Column(name = "ceo_name", length = 200)
    private String ceoName;

    @Column(length = 500)
    private String homepage;

    @Column(length = 100)
    private String region;

    protected ListedCorp() {}

    public ListedCorp(String companyName, String market, String stockCode, String industry,
                      String mainProducts, LocalDate listedDate, String fiscalMonth,
                      String ceoName, String homepage, String region) {
        this.companyName = companyName;
        this.market = market;
        this.stockCode = stockCode;
        this.industry = industry;
        this.mainProducts = mainProducts;
        this.listedDate = listedDate;
        this.fiscalMonth = fiscalMonth;
        this.ceoName = ceoName;
        this.homepage = homepage;
        this.region = region;
    }

    public Long getId() { return id; }
    public String getCompanyName() { return companyName; }
    public String getMarket() { return market; }
    public String getStockCode() { return stockCode; }
    public String getIndustry() { return industry; }
    public String getMainProducts() { return mainProducts; }
    public LocalDate getListedDate() { return listedDate; }
    public String getFiscalMonth() { return fiscalMonth; }
    public String getCeoName() { return ceoName; }
    public String getHomepage() { return homepage; }
    public String getRegion() { return region; }
}