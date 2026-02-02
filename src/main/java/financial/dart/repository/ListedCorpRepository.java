package financial.dart.repository;


import financial.dart.domain.ListedCorp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ListedCorpRepository extends JpaRepository<ListedCorp, Long> {
    Optional<ListedCorp> findByCorpCode(String corpCode);
    Optional<ListedCorp> findByStockCode(String stockCode);
}