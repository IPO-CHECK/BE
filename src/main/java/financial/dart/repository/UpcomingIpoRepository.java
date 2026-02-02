package financial.dart.repository;

import financial.dart.domain.UpcomingIpo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UpcomingIpoRepository extends JpaRepository<UpcomingIpo, Long> {
    Optional<UpcomingIpo> findByIpoNo(String ipoNo);
}
