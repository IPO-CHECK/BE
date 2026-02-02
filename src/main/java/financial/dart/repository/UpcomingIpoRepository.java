package financial.dart.repository;

import financial.dart.domain.UpcomingIpo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UpcomingIpoRepository extends JpaRepository<UpcomingIpo, Long> {
    Optional<UpcomingIpo> findByIpoNo(String ipoNo);

    @Query("SELECT u.corpCode FROM UpcomingIpo u WHERE u.id = :id")
    String findCorpCodeById(@Param("id") Long id);
}
