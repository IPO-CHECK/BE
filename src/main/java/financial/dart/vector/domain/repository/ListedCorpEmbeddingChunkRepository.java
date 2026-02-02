package financial.dart.vector.domain.repository;

import financial.dart.vector.domain.ListedCorpEmbeddingChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListedCorpEmbeddingChunkRepository extends JpaRepository<ListedCorpEmbeddingChunk, Long> {
    void deleteByListedCorpId(Long listedCorpId);
    List<ListedCorpEmbeddingChunk> findByListedCorpId(Long listedCorpId);
}