package financial.dart.repository;

import financial.dart.domain.Corporation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CorporationRepository extends JpaRepository<Corporation, Long> {
    boolean existsByCorpCode(String corpCode);

    @Query("SELECT distinct c " +
            "FROM Corporation c " +
            "join Financial f ON f.corporation.id = c.id " +
            "WHERE c.stockCode IS NOT NULL and c.stockCode <> '' ")
    List<Corporation> findCorps();

    @Query("SELECT c FROM Corporation c WHERE c.corpCode = :corpCode")
    Corporation findByCorpCode(@Param("corpCode") String corpCode);

    // TODO 원래 이 쿼리인데 boolean 3개 칼럼이 제대로 처리 안 되는 듯
//    @Query("SELECT c.id " +
//            "FROM Corporation c " +
//            "WHERE c.hasNoMajorChanges = true " +
//            "and c.hasUnqualifiedOpinion = true " +
//            "and c.isOver3Months = true " +
//            "and c.stockCode IS NOT NULL and " +
//            "c.stockCode <> '' ")
    @Query("SELECT c.id " +
            "FROM Corporation c " +
            "where c.stockCode IS NOT NULL and c.stockCode <> '' ")
    List<Long> findQualifiedCorporationIds();
}