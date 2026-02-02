package financial.dart.vector.dto;

public record IndexResultDto(
        String corpCode,
        String rcpNo,
        int chunkCount,
        int upsertedCount
) {}