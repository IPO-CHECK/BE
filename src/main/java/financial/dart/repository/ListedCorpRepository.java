package financial.dart.repository;


import financial.dart.domain.ListedCorp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ListedCorpRepository extends JpaRepository<ListedCorp, Long> {
    Optional<ListedCorp> findByCorpCode(String corpCode);
    Optional<ListedCorp> findByStockCode(String stockCode);

    @Query("SELECT lc FROM FinancialListedCorp lc WHERE lc.corpCode IN :corpCodes")
    List<ListedCorp> findByCorpCodeIn(@Param("corpCodes") List<String> corpCodes);

    @Query("SELECT lc.corpCode FROM FinancialListedCorp lc WHERE lc.id IN :ids")
    List<String> findCorpCodesByIdIn(List<Long> ids);
}