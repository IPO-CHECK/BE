package financial.dart.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "corp_filing",
        uniqueConstraints = @UniqueConstraint(name="uq_corp_rcept", columnNames = {"corp_code","rcept_no"}))
public class CorpFiling {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="corp_code", length=8, nullable=false)
    private String corpCode;

    @Column(name="rcept_no", length=20, nullable=false)
    private String rceptNo;

    @Column(name="report_nm", length=200)
    private String reportNm;

    @Column(name="rcept_dt", length=8)
    private String rceptDt;

    @Column(name="rm", length=10)
    private String rm;

    protected CorpFiling() {}

    public CorpFiling(String corpCode, String rceptNo) {
        this.corpCode = corpCode;
        this.rceptNo = rceptNo;
    }

    public Long getId() { return id; }
    public String getCorpCode() { return corpCode; }
    public String getRceptNo() { return rceptNo; }
    public String getReportNm() { return reportNm; }
    public String getRceptDt() { return rceptDt; }
    public String getRm() { return rm; }

    public void setReportNm(String reportNm) { this.reportNm = reportNm; }
    public void setRceptDt(String rceptDt) { this.rceptDt = rceptDt; }
    public void setRm(String rm) { this.rm = rm; }
}