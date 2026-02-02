package financial.dart.repository;

import financial.dart.vector.domain.ListedCorpVector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListedCorpVectorRepository extends JpaRepository<ListedCorpVector, Long> {
    void deleteByListedCorpId(Long listedCorpId);
}