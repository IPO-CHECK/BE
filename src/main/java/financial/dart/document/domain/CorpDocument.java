package financial.dart.document.domain;

import jakarta.persistence.*;

@Entity
@Table(name="corp_document",
        uniqueConstraints = @UniqueConstraint(name="uq_corp_rcept", columnNames={"corp_code","rcept_no"}))
public class CorpDocument {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="corp_code", length=8, nullable=false)
    private String corpCode;

    @Column(name="rcept_no", length=20, nullable=false)
    private String rceptNo;

    @Lob
    @Column(name="doc_xml", columnDefinition = "LONGTEXT")
    private String docXml;

    protected CorpDocument() {}

    public CorpDocument(String corpCode, String rceptNo, String docXml) {
        this.corpCode = corpCode;
        this.rceptNo = rceptNo;
        this.docXml = docXml;
    }

    public String getCorpCode() { return corpCode; }
    public String getRceptNo() { return rceptNo; }
    public String getDocXml() { return docXml; }
}