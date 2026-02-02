package financial.dart.vector.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import financial.dart.domain.ListedCorp;
import financial.dart.repository.ListedCorpRepository;
import financial.dart.repository.ListedCorpVectorRepository;
import financial.dart.vector.client.EmbeddingClient;
import financial.dart.vector.domain.ListedCorpVector;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListedCorpVectorIndexService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ListedCorpRepository listedCorpRepository;
    private final ListedCorpVectorRepository vectorRepository;
    private final EmbeddingClient embeddingClient;

    @Data
    @AllArgsConstructor
    public static class IndexResult {
        private String target;
        private Long listedCorpId;
        private int saved;
        private int skippedEmpty;
    }

    @Transactional
    public IndexResult indexOneByCorpCodeOrStockCode(String corpCodeOrStockCode) {
        ListedCorp corp = listedCorpRepository.findByCorpCode(corpCodeOrStockCode)
                .or(() -> listedCorpRepository.findByStockCode(corpCodeOrStockCode))
                .orElseThrow(() -> new IllegalArgumentException(
                        "listed_corp에서 기업을 찾지 못했습니다. corp_code/stock_code=" + corpCodeOrStockCode));

        String text = buildIndexText(corp);
        if (text.isBlank()) {
            return new IndexResult(corpCodeOrStockCode, corp.getId(), 0, 1);
        }

        // 기존 벡터 삭제 후 재인덱싱
        vectorRepository.deleteByListedCorpId(corp.getId());

        List<String> chunks = chunk(text, 800, 120);

        int saved = 0;
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i).trim();
            if (chunk.isEmpty()) continue;

            float[] vector = embeddingClient.embedOne(chunk);

            String vectorJson;
            try {
                vectorJson = objectMapper.writeValueAsString(vector);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("벡터 JSON 변환 실패", e);
            }

            ListedCorpVector v = ListedCorpVector.builder()
                    .listedCorp(corp)
                    .chunkIndex(i)      // ✅ chunkIndex -> i
                    .text(chunk)        // ✅ text -> chunk
                    .vector(vectorJson)
                    .build();

            vectorRepository.save(v);
            saved++;
        }

        return new IndexResult(corpCodeOrStockCode, corp.getId(), saved, 0);
    }

    private String buildIndexText(ListedCorp corp) {
        String industry = nullToEmpty(corp.getIndustry());
        String products = nullToEmpty(corp.getMainProducts());

        String merged = """
                [산업]
                %s

                [주요제품/서비스]
                %s
                """.formatted(industry, products);

        return merged.trim();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * 간단 chunker (길이 기반)
     * - maxLen: 청크 최대 길이
     * - overlap: 다음 청크에 이전 텍스트 일부 겹치기
     */
    private List<String> chunk(String text, int maxLen, int overlap) {
        List<String> chunks = new ArrayList<>();
        int n = text.length();
        int start = 0;

        while (start < n) {
            int end = Math.min(start + maxLen, n);
            chunks.add(text.substring(start, end));

            if (end == n) break;
            start = Math.max(0, end - overlap);
        }

        return chunks;
    }

    @Transactional
    public void indexAll() {
        List<ListedCorp> corps = listedCorpRepository.findAll();

        int cnt = 0;
        int total = corps.size();
        log.info("상장기업 전체 인덱싱 시작: total={}", total);

        for (ListedCorp corp : corps) {

            String key = corp.getCorpCode() != null
                    ? corp.getCorpCode()
                    : corp.getStockCode();

            if (key == null || key.isBlank()) {
                log.info("인덱싱 스킵(키 없음): id={}, name={}", corp.getId(), corp.getCorpName());
                continue;
            }

            indexOneByCorpCodeOrStockCode(key);
            cnt++;
            log.info("인덱싱 진행: {}/{} (key={})", cnt, total, key);
        }

        log.info("상장기업 전체 인덱싱 완료: indexed={}/{}", cnt, total);
    }

}