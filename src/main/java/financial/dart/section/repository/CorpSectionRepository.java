package financial.dart.section.repository;

import financial.dart.section.domain.CorpSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CorpSectionRepository extends JpaRepository<CorpSection, Long> {
    Optional<CorpSection> findByCorpCodeAndRceptNoAndSectionType(String corpCode, String rceptNo, String sectionType);
}