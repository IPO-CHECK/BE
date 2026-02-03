package financial.dart.service;

import financial.dart.domain.IpoBusinessAnalysis;
import financial.dart.repository.IpoBusinessAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IpoBusinessAnalysisService {

    private final IpoBusinessAnalysisRepository businessAnalysisRepository;

    public Optional<IpoBusinessAnalysis> findByUpcomingIpoId(Long upcomingIpoId) {
        return businessAnalysisRepository.findByUpcomingIpoId(upcomingIpoId);
    }
}
