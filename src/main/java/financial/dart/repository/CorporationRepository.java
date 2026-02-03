package financial.dart.repository;

import financial.dart.domain.CorpFinRatio;
import financial.dart.domain.Corporation;
import financial.dart.dto.UpcomingDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CorporationRepository extends JpaRepository<Corporation, Long> {
    boolean existsByCorpCode(String corpCode);

    @Query("SELECT distinct c " +
            "FROM Corporation c " +
            "join Financial f ON f.corporation.id = c.id " +
            "WHERE c.stockCode IS NOT NULL and c.stockCode <> '' ")
    List<Corporation> findCorps();

    @Query("SELECT c FROM Corporation c WHERE c.corpCode = :corpCode")
    Corporation findByCorpCode(@Param("corpCode") String corpCode);

    @Query("SELECT c.id FROM Corporation c WHERE c.corpCode = :corpCode")
    Optional<Long> findIdByCorpCode(@Param("corpCode") String corpCode);

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

    @Query("SELECT new financial.dart.dto.UpcomingDto(" +
            "null, c.id, c.corpCode, c.corpName, " +
            "c.market, c.industry, c.underwriter, " +
            "c.subscriptionDate, c.listingDate, " +
            "'청약 예정', '공모가 ' || c.finalOfferPrice) " +
            "FROM Corporation c " +
            "WHERE c.corpCode IN :corpCodes")
    List<UpcomingDto> findAllByCorpCodes(@Param("corpCodes") List<String> corpCodes);

    @Query("SELECT r " +
            "FROM CorpFinRatio r " +
            "WHERE r.corporation.id = :corporationId " +
            "ORDER BY r.bsnsYear ASC")
    List<CorpFinRatio> findAllByCorporationIds(@Param("corporationId") Long corporationId);

    @Query("SELECT sc.targetCorpId " +
            "FROM SimilarCorps sc " +
            "WHERE sc.corpId = :corpId")
    List<Long> findTargetCorpIdsByCorpId(@Param("corpId") Long corpId);

    // [추가] 여러 기업의 재무비율을 한 번에 조회 (사업연도 오름차순)
    @Query("SELECT r FROM CorpFinRatio r WHERE r.corporation.id IN :corpIds ORDER BY r.bsnsYear ASC")
    List<CorpFinRatio> findByCorporationIdInOrderByBsnsYearAsc(@Param("corpIds") List<Long> corpIds);
}