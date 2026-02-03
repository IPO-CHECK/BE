package financial.dart.repository;

import financial.dart.domain.Financial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FinancialRepository extends JpaRepository<Financial, Long> {

    @Query("SELECT f " +
            "FROM Financial f " +
            "WHERE f.id = :financialId")
    Optional<Financial> findByFinancialId(@Param("financialId") Long financialId);

    @Query("SELECT f " +
            "FROM Financial f " +
            "WHERE f.corporation.id IN :corpIds AND" +
            " f.revenue BETWEEN :revenue * 0.2 AND :revenue * 5 AND" +
            " f.totalAssets BETWEEN :totalAssets * 0.2 AND :totalAssets * 5 AND" +
            " f.totalEquity BETWEEN :totalEquity * 0.2 AND :totalEquity * 5 AND " +
            " f.bsnsYear = :year AND " +
            " f.reprtCode = :reprtCode")
    List<Financial> findByCorporationId(@Param("corpIds") List<Long> corpIds,
                                        @Param("revenue") Long revenue,
                                        @Param("totalAssets") Long totalAssets,
                                        @Param("totalEquity") Long totalEquity,
                                        @Param("year") String year,
                                        @Param("reprtCode") String reprtCode);

    @Query("SELECT f " +
            "FROM Financial f " +
            "WHERE f.corporation.id = :corpId " +
            "ORDER BY f.bsnsYear DESC " +
            "LIMIT 1")
    Optional<Financial> findByCorporationId(@Param("corpId") Long corpId);

    @Query("SELECT f " +
            "FROM Financial f " +
            "WHERE f.corporation.id = :corpId " +
            "ORDER BY f.bsnsYear ASC")
    List<Financial> findByCorporationIdOrderByBsnsYearAsc(@Param("corpId") Long corpId);
}
