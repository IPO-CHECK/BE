package financial.dart.document.repository;

import financial.dart.document.domain.CorpDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CorpDocumentRepository extends JpaRepository<CorpDocument, Long> {
    Optional<CorpDocument> findByCorpCodeAndRceptNo(String corpCode, String rceptNo);
}