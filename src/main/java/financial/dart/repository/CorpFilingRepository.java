package financial.dart.repository;

import financial.dart.domain.CorpFiling;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CorpFilingRepository extends JpaRepository<CorpFiling, Long> {

    Optional<CorpFiling> findTopByCorpCodeOrderByRceptDtDesc(String corpCode);
}