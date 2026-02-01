package financial.dart.section.domain;

import jakarta.persistence.*;

@Entity
@Table(name="corp_section",
        uniqueConstraints = @UniqueConstraint(name="uq_corp_rcept_type", columnNames={"corp_code","rcept_no","section_type"}))
public class CorpSection {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="corp_code", length=8, nullable=false)
    private String corpCode;

    @Column(name="rcept_no", length=20, nullable=false)
    private String rceptNo;

    @Column(name="section_type", length=30, nullable=false)
    private String sectionType;

    @Lob
    @Column(name="section_text", columnDefinition="LONGTEXT")
    private String sectionText;

    @Column(name="text_hash", length=64)
    private String textHash;

    protected CorpSection() {}

    public CorpSection(String corpCode, String rceptNo, String sectionType, String sectionText, String textHash) {
        this.corpCode = corpCode;
        this.rceptNo = rceptNo;
        this.sectionType = sectionType;
        this.sectionText = sectionText;
        this.textHash = textHash;
    }

    public String getSectionText() { return sectionText; }
}