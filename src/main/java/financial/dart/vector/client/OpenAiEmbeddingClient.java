package financial.dart.vector.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiEmbeddingClient implements EmbeddingClient {

    private final RestClient restClient;

    public OpenAiEmbeddingClient(RestClient openAiEmbeddingRestClient) {
        this.restClient = openAiEmbeddingRestClient;
    }

    @Override
    public float[] embedOne(String text) {
        Map<String, Object> request = Map.of(
                "model", "text-embedding-3-small",
                "input", text
        );

        Map<?, ?> response = restClient.post()
                .uri("/embeddings")
                .body(request)
                .retrieve()
                .body(Map.class);

        // OpenAI 응답 구조: data[0].embedding[]
        List<?> embedding = (List<?>) ((Map<?, ?>)
                ((List<?>) response.get("data")).get(0))
                .get("embedding");

        float[] vector = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            vector[i] = ((Number) embedding.get(i)).floatValue();
        }

        return vector;
    }
}