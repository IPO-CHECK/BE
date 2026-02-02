package financial.dart.vector.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "listed_corp_embedding_chunk",
        uniqueConstraints = @UniqueConstraint(name = "uk_corp_chunk", columnNames = {"listed_corp_id", "chunk_index"})
)
public class ListedCorpEmbeddingChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="listed_corp_id", nullable = false)
    private Long listedCorpId;

    @Column(name="chunk_index", nullable = false)
    private int chunkIndex;

    @Lob
    @Column(name="chunk_text", nullable = false, columnDefinition = "LONGTEXT")
    private String chunkText;

    @Lob
    @Column(name="embedding_blob", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] embeddingBlob;

    @Column(name="embedding_norm", nullable = false)
    private double embeddingNorm;

    @Column(name="model", nullable = false, length = 100)
    private String model;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected ListedCorpEmbeddingChunk() {}

    public ListedCorpEmbeddingChunk(Long listedCorpId, int chunkIndex, String chunkText,
                                    byte[] embeddingBlob, double embeddingNorm, String model) {
        this.listedCorpId = listedCorpId;
        this.chunkIndex = chunkIndex;
        this.chunkText = chunkText;
        this.embeddingBlob = embeddingBlob;
        this.embeddingNorm = embeddingNorm;
        this.model = model;
    }

    public Long getId() { return id; }
    public Long getListedCorpId() { return listedCorpId; }
    public int getChunkIndex() { return chunkIndex; }
    public String getChunkText() { return chunkText; }
    public byte[] getEmbeddingBlob() { return embeddingBlob; }
    public double getEmbeddingNorm() { return embeddingNorm; }
    public String getModel() { return model; }
}