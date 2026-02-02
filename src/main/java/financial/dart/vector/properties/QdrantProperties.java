package financial.dart.vector.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "qdrant")
public record QdrantProperties(
        String baseUrl,
        String collection,
        int vectorSize
) {}