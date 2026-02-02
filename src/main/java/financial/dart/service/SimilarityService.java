package financial.dart.service;

import financial.dart.domain.Financial;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimilarityService {

    // 결과 담을 DTO (내부 클래스로 정의)
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class SimilarityResult {
        private Financial financial; // 후보 기업 재무정보
        private double score;        // 유사도 점수 (1.0에 가까울수록 똑같음)
        private double[] vector;     // 표준화된 벡터
    }

    /**
     * 핵심 로직: 후보군 중에서 Target과 가장 비슷한 Top N개를 뽑는다.
     */
    public List<SimilarityResult> findTopSimilarCorp(Financial target, List<Financial> candidates, int topN) {

        // 1. 전체 데이터(Target + 후보들) 벡터 수집
        // (표준화를 위해 전체 집단의 평균과 표준편차가 필요함)
        List<Financial> allList = new ArrayList<>(candidates);
        allList.add(target);

        // 2. [중요] 표준화 (Z-Score Normalization) 수행
        // 각 기업의 6개 지표를 '평균 0, 표준편차 1'로 변환한 맵을 받음
        Map<Financial, double[]> normalizedMap = normalizeVectors(allList);

        // 3. Target의 표준화된 벡터 가져오기
        double[] targetVector = normalizedMap.get(target);

        // 4. 코사인 유사도 계산 및 정렬
        return candidates.stream()
                .map(candidate -> {
                    double[] candidateVector = normalizedMap.get(candidate);
                    double score = calculateCosineSimilarity(targetVector, candidateVector);
                    return new SimilarityResult(candidate, score, candidateVector);
                })
                .sorted(Comparator.comparingDouble(SimilarityResult::getScore).reversed()) // 점수 높은 순 정렬
                .limit(topN) // Top N 자르기
                .collect(Collectors.toList());
    }

    // --- [내부 로직 1] 데이터 표준화 (Z-Score) ---
    private Map<Financial, double[]> normalizeVectors(List<Financial> list) {
        int dim = 6; // 6차원 벡터
        int size = list.size();

        // 결과 담을 맵
        Map<Financial, double[]> resultMap = new HashMap<>();
        for (Financial f : list) {
            resultMap.put(f, new double[dim]);
        }

        // 각 차원(열)별로 평균과 표준편차 계산
        for (int i = 0; i < dim; i++) {
            double sum = 0;
            double sumSq = 0;

            // 1. 해당 차원의 값들 수집
            for (Financial f : list) {
                double val = f.getAnalysisVector()[i];
                sum += val;
            }
            double mean = sum / size;

            for (Financial f : list) {
                double val = f.getAnalysisVector()[i];
                sumSq += Math.pow(val - mean, 2);
            }
            double stdDev = Math.sqrt(sumSq / size);

            // 분모가 0이면(모든 기업의 값이 같으면) 0으로 나눌 수 없으니 1로 처리
            if (stdDev == 0) stdDev = 1.0;

            // 2. 표준화 적용 ( (X - 평균) / 표준편차 )
            for (Financial f : list) {
                double rawValue = f.getAnalysisVector()[i];
                resultMap.get(f)[i] = (rawValue - mean) / stdDev;
            }
        }
        return resultMap;
    }

    // --- [내부 로직 2] 코사인 유사도 공식 ---
    private double calculateCosineSimilarity(double[] v1, double[] v2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += Math.pow(v1[i], 2);
            normB += Math.pow(v2[i], 2);
        }

        if (normA == 0 || normB == 0) return 0.0; // 0벡터 예외처리
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}