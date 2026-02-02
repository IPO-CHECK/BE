package financial.dart.vector.service;

import financial.dart.vector.dto.IndexResultDto;
import org.springframework.stereotype.Service;

@Service
public class CorpVectorIndexService {

    public IndexResultDto indexLatest(String corpCode) {
        // TODO: 최신 rcpNo 조회 + 섹션 추출 + 청킹 + 임베딩
        String rcpNo = "TODO";
        int chunkCount = 0;
        int upsertedCount = 0;

        return new IndexResultDto(corpCode, rcpNo, chunkCount, upsertedCount);
    }
}