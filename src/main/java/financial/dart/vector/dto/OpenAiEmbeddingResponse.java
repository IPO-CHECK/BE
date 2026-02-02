package financial.dart.vector.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiEmbeddingResponse(
        List<Item> data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            List<Double> embedding
    ) {}
}