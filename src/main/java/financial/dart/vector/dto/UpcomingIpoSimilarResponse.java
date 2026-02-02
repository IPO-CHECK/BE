package financial.dart.vector.dto;

import java.util.List;

public record UpcomingIpoSimilarResponse(
        String businessOverview,
        List<SimilarListedCorpResult> similar
) {}
