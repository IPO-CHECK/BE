package financial.dart.vector.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QdrantSearchResponse(
        List<Result> result
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            double score,
            Map<String, Object> payload
    ) {}
}