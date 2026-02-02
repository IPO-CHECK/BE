package financial.dart.vector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import financial.dart.domain.ListedCorp;
import financial.dart.repository.ListedCorpVectorRepository;
import financial.dart.vector.client.EmbeddingClient;
import financial.dart.vector.domain.ListedCorpVector;
import financial.dart.vector.dto.SimilarListedCorpResult;
import financial.dart.vector.util.VectorCodec;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimilarListedCorpSearchService {

    private static final int DEFAULT_TOP_K = 10;

    private final EmbeddingClient embeddingClient;
    private final ListedCorpVectorRepository vectorRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SimilarListedCorpSearchService(
            EmbeddingClient embeddingClient,
            ListedCorpVectorRepository vectorRepository
    ) {
        this.embeddingClient = embeddingClient;
        this.vectorRepository = vectorRepository;
    }

    public List<SimilarListedCorpResult> search(String businessOverview, Integer topK) {
        if (businessOverview == null || businessOverview.isBlank()) {
            return List.of();
        }

        int limit = (topK == null || topK <= 0) ? DEFAULT_TOP_K : Math.min(topK, 20);

        float[] queryVector = embeddingClient.embedOne(businessOverview.trim());
        List<ListedCorpVector> vectors = vectorRepository.findAllWithListedCorp();

        Map<Long, CorpScore> bestByCorp = new HashMap<>();

        for (ListedCorpVector v : vectors) {
            float[] vec = parseVector(v.getVector());
            if (vec == null || vec.length == 0) continue;

            double score = VectorCodec.cosineSimilarity(queryVector, vec);
            ListedCorp corp = v.getListedCorp();
            if (corp == null) continue;

            CorpScore prev = bestByCorp.get(corp.getId());
            if (prev == null || score > prev.score) {
                bestByCorp.put(corp.getId(), new CorpScore(corp, score));
            }
        }

        List<SimilarListedCorpResult> results = new ArrayList<>();
        for (CorpScore cs : bestByCorp.values()) {
            ListedCorp c = cs.corp;
            results.add(new SimilarListedCorpResult(
                    c.getId(),
                    c.getCorpName(),
                    c.getStockCode(),
                    c.getMarket(),
                    c.getIndustry(),
                    cs.score
            ));
        }

        results.sort(Comparator.comparingDouble(SimilarListedCorpResult::score).reversed());

        if (results.size() > limit) {
            return results.subList(0, limit);
        }
        return results;
    }

    private float[] parseVector(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, float[].class);
        } catch (Exception e) {
            return null;
        }
    }

    private static class CorpScore {
        private final ListedCorp corp;
        private final double score;

        private CorpScore(ListedCorp corp, double score) {
            this.corp = corp;
            this.score = score;
        }
    }
}
