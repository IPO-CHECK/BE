package financial.dart.repository;

import financial.dart.domain.UpcomingIpoRiskAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UpcomingIpoRiskAnalysisRepository extends JpaRepository<UpcomingIpoRiskAnalysis, Long> {
    Optional<UpcomingIpoRiskAnalysis> findByUpcomingIpoId(Long upcomingIpoId);
}
