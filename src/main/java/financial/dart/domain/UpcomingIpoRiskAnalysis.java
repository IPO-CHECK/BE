package financial.dart.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "upcoming_ipo_risk_analysis",
        uniqueConstraints = @UniqueConstraint(name = "uk_upcoming_ipo_risk", columnNames = "upcoming_ipo_id")
)
public class UpcomingIpoRiskAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upcoming_ipo_id", nullable = false)
    private UpcomingIpo upcomingIpo;

    @Column(name = "rcept_no", length = 20)
    private String rceptNo;

    @Lob
    @Column(name = "key_risk_text", columnDefinition = "LONGTEXT")
    private String keyRiskText;

    @Lob
    @Column(name = "analysis_text", columnDefinition = "LONGTEXT")
    private String analysisText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected UpcomingIpoRiskAnalysis() {}

    public UpcomingIpoRiskAnalysis(UpcomingIpo upcomingIpo, String rceptNo, String keyRiskText, String analysisText) {
        this.upcomingIpo = upcomingIpo;
        this.rceptNo = rceptNo;
        this.keyRiskText = keyRiskText;
        this.analysisText = analysisText;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public UpcomingIpo getUpcomingIpo() { return upcomingIpo; }
    public String getRceptNo() { return rceptNo; }
    public String getKeyRiskText() { return keyRiskText; }
    public String getAnalysisText() { return analysisText; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(String rceptNo, String keyRiskText, String analysisText) {
        this.rceptNo = rceptNo;
        this.keyRiskText = keyRiskText;
        this.analysisText = analysisText;
        this.updatedAt = LocalDateTime.now();
    }
}
