package financial.dart.vector.dto;

import java.util.List;

public record OpenAiEmbeddingRequest(
        String model,
        List<String> input
) {}