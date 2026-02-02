package financial.dart.vector.dto;

import java.util.List;
import java.util.Map;

public record QdrantSearchRequest(
        List<Float> vector,
        int limit,
        boolean with_payload,
        Map<String, Object> filter
) {}