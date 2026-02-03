package financial.dart.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ipo_business_analysis",
        uniqueConstraints = @UniqueConstraint(name = "uk_ipo_business_analysis_upcoming_ipo", columnNames = "upcoming_ipo_id")
)
public class IpoBusinessAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "upcoming_ipo_id", nullable = false)
    private UpcomingIpo upcomingIpo;

    @Lob
    @Column(name = "monetization_structure", columnDefinition = "TEXT")
    private String monetizationStructure;

    @Lob
    @Column(name = "scalability", columnDefinition = "TEXT")
    private String scalability;

    @Lob
    @Column(name = "structural_risk", columnDefinition = "TEXT")
    private String structuralRisk;

    @Lob
    @Column(name = "resource_capability", columnDefinition = "TEXT")
    private String resourceCapability;

    @Lob
    @Column(name = "summary_final", columnDefinition = "TEXT")
    private String summaryFinal;

    @Column(name = "monetization_grade", length = 10)
    private String monetizationGrade;

    @Column(name = "scalability_grade", length = 10)
    private String scalabilityGrade;

    @Column(name = "structural_risk_grade", length = 10)
    private String structuralRiskGrade;

    @Column(name = "resource_capability_grade", length = 10)
    private String resourceCapabilityGrade;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected IpoBusinessAnalysis() {}

    public IpoBusinessAnalysis(
            UpcomingIpo upcomingIpo,
            String monetizationStructure,
            String scalability,
            String structuralRisk,
            String resourceCapability,
            String summaryFinal,
            String monetizationGrade,
            String scalabilityGrade,
            String structuralRiskGrade,
            String resourceCapabilityGrade
    ) {
        this.upcomingIpo = upcomingIpo;
        this.monetizationStructure = monetizationStructure;
        this.scalability = scalability;
        this.structuralRisk = structuralRisk;
        this.resourceCapability = resourceCapability;
        this.summaryFinal = summaryFinal;
        this.monetizationGrade = monetizationGrade;
        this.scalabilityGrade = scalabilityGrade;
        this.structuralRiskGrade = structuralRiskGrade;
        this.resourceCapabilityGrade = resourceCapabilityGrade;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public UpcomingIpo getUpcomingIpo() { return upcomingIpo; }
    public String getMonetizationStructure() { return monetizationStructure; }
    public String getScalability() { return scalability; }
    public String getStructuralRisk() { return structuralRisk; }
    public String getResourceCapability() { return resourceCapability; }
    public String getSummaryFinal() { return summaryFinal; }
    public String getMonetizationGrade() { return monetizationGrade; }
    public String getScalabilityGrade() { return scalabilityGrade; }
    public String getStructuralRiskGrade() { return structuralRiskGrade; }
    public String getResourceCapabilityGrade() { return resourceCapabilityGrade; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(
            String monetizationStructure,
            String scalability,
            String structuralRisk,
            String resourceCapability,
            String summaryFinal,
            String monetizationGrade,
            String scalabilityGrade,
            String structuralRiskGrade,
            String resourceCapabilityGrade
    ) {
        this.monetizationStructure = monetizationStructure;
        this.scalability = scalability;
        this.structuralRisk = structuralRisk;
        this.resourceCapability = resourceCapability;
        this.summaryFinal = summaryFinal;
        this.monetizationGrade = monetizationGrade;
        this.scalabilityGrade = scalabilityGrade;
        this.structuralRiskGrade = structuralRiskGrade;
        this.resourceCapabilityGrade = resourceCapabilityGrade;
        this.updatedAt = LocalDateTime.now();
    }
}
