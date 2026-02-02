package financial.dart.repository;

import financial.dart.vector.domain.ListedCorpVector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ListedCorpVectorRepository extends JpaRepository<ListedCorpVector, Long> {
    void deleteByListedCorpId(Long listedCorpId);

    @Query("SELECT v FROM ListedCorpVector v JOIN FETCH v.listedCorp")
    List<ListedCorpVector> findAllWithListedCorp();
}