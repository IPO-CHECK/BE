package financial.dart.Test;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ListedCorpRepository extends JpaRepository<ListedCorp, Long> {

    boolean existsByStockCode(String stockCode);

    @Query("select l.stockCode from ListedCorp l")
    List<String> findAllStockCodes();
}