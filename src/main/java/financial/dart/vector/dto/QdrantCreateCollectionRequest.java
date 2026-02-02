package financial.dart.vector.dto;

import java.util.Map;

public record QdrantCreateCollectionRequest(
        Map<String, Object> vectors
) {}