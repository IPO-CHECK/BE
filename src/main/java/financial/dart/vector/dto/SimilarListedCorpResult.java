package financial.dart.vector.dto;

public record SimilarListedCorpResult(
        Long corpId,
        String corpName,
        String stockCode,
        String market,
        String industry,
        double score
) {}
