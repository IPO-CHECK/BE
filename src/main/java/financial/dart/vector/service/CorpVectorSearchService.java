package financial.dart.vector.service;

import financial.dart.vector.client.EmbeddingClient;
import org.springframework.stereotype.Service;

@Service
public class CorpVectorSearchService {

    private final EmbeddingClient embeddingClient;

    public CorpVectorSearchService(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }
}