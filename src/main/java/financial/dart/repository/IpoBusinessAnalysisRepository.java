package financial.dart.repository;

import financial.dart.domain.IpoBusinessAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IpoBusinessAnalysisRepository extends JpaRepository<IpoBusinessAnalysis, Long> {
    Optional<IpoBusinessAnalysis> findByUpcomingIpoId(Long upcomingIpoId);
}
