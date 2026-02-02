package financial.dart.service;

import financial.dart.domain.UpcomingIpo;
import financial.dart.domain.UpcomingIpoRiskAnalysis;
import financial.dart.repository.UpcomingIpoRepository;
import financial.dart.repository.UpcomingIpoRiskAnalysisRepository;
import financial.dart.section.service.CorpSectionMainXmlService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

@Service
public class UpcomingIpoRiskAnalysisService {

    private static final String MODEL = "gpt-4o";
    private static final int MAX_INPUT_CHARS = 12000;

    private final UpcomingIpoRepository upcomingIpoRepository;
    private final UpcomingIpoRiskAnalysisRepository riskAnalysisRepository;
    private final CorpSectionMainXmlService corpSectionMainXmlService;
    private final RestClient openAiRestClient;

    public UpcomingIpoRiskAnalysisService(
            UpcomingIpoRepository upcomingIpoRepository,
            UpcomingIpoRiskAnalysisRepository riskAnalysisRepository,
            CorpSectionMainXmlService corpSectionMainXmlService,
            @Qualifier("openAiRestClient") RestClient openAiRestClient
    ) {
        this.upcomingIpoRepository = upcomingIpoRepository;
        this.riskAnalysisRepository = riskAnalysisRepository;
        this.corpSectionMainXmlService = corpSectionMainXmlService;
        this.openAiRestClient = openAiRestClient;
    }

    public UpcomingIpoRiskAnalysis getOrCreate(Long upcomingIpoId) {
        UpcomingIpo ipo = upcomingIpoRepository.findById(upcomingIpoId)
                .orElseThrow(() -> new IllegalArgumentException("upcoming_ipo not found: " + upcomingIpoId));

        String rceptNo = ipo.getRceptNo();
        if (rceptNo == null || rceptNo.isBlank()) {
            throw new IllegalStateException("rcept_no가 없습니다. 먼저 상장예정기업 정보를 갱신하세요.");
        }

        UpcomingIpoRiskAnalysis existing = riskAnalysisRepository.findByUpcomingIpoId(upcomingIpoId)
                .orElse(null);
        if (existing != null && rceptNo.equals(existing.getRceptNo()) && existing.getAnalysisText() != null
                && !existing.getAnalysisText().isBlank() && !isRefusal(existing.getAnalysisText())) {
            return existing;
        }

        String keyRiskText = corpSectionMainXmlService.fetchKeyInvestmentRiskByRcpNo(rceptNo);
        if (keyRiskText == null || keyRiskText.isBlank()) {
            throw new IllegalStateException("핵심투자위험 섹션을 찾지 못했습니다. rcept_no=" + rceptNo);
        }

        String analysis = analyzeKeyRisks(keyRiskText);

        if (existing == null) {
            UpcomingIpoRiskAnalysis created = new UpcomingIpoRiskAnalysis(ipo, rceptNo, keyRiskText, analysis);
            return riskAnalysisRepository.save(created);
        }

        existing.update(rceptNo, keyRiskText, analysis);
        return riskAnalysisRepository.save(existing);
    }

    private String analyzeKeyRisks(String keyRiskText) {
        String input = normalizeInput(keyRiskText);
        if (input.length() > MAX_INPUT_CHARS) {
            input = input.substring(0, MAX_INPUT_CHARS);
        }

        Map<String, Object> request = Map.of(
                "model", MODEL,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                        너는 증권신고서 내부 내용만을 근거로 핵심투자위험을 분석하는 전문가다.
                                        외부 뉴스, 보고서, 웹자료는 절대 사용하지 말아라.
                                        """
                        ),
                        Map.of(
                                "role", "user",
                                "content", buildPrompt(input)
                        )
                )
        );

        Map<?, ?> response = openAiRestClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(Map.class);

        String content = extractContent(response);
        if (isRefusal(content)) {
            return analyzeKeyRisksFallback(input);
        }
        return content;
    }

    private String buildPrompt(String input) {
        return """
                다음은 증권신고서의 '핵심투자위험' 섹션입니다.
                내부 내용만 근거로, 투자자가 이해하기 쉽게 항목별로 정리해 주세요.

                출력 형식:
                [핵심 요약]
                - 3~5줄로 가장 중요한 위험을 간결하게 요약

                [항목별 정리]
                1) 항목명
                - 요약: 1~2문장
                - 내부 근거: 본문에서 근거가 되는 문장이나 키워드(짧게)
                - 확인 필요: 추가 확인이 필요한 부분이 있으면 한 줄로 표시(없으면 '없음')

                규칙:
                - 외부 자료를 사용하지 말 것
                - 내부 근거가 부족하면 '확인 필요'에 그 이유를 간단히 적기
                - 과도한 수식 없이 명확하고 간결하게

                본문:
                %s
                """.formatted(input);
    }

    private String analyzeKeyRisksFallback(String input) {
        Map<String, Object> request = Map.of(
                "model", MODEL,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                        너는 증권신고서 내부 내용만 간결하게 정리하는 어시스턴트다.
                                        외부 자료는 사용하지 않는다.
                                        """
                        ),
                        Map.of(
                                "role", "user",
                                "content", """
                                        다음 본문을 투자자가 이해하기 쉽게 요약해 주세요.
                                        - 5~8줄
                                        - 핵심 위험 키워드를 괄호로 표시

                                        본문:
                                        %s
                                        """.formatted(input)
                        )
                )
        );

        Map<?, ?> response = openAiRestClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(Map.class);

        return extractContent(response);
    }

    private String normalizeInput(String text) {
        return text.replace("\u00A0", " ").replaceAll("\\s+", " ").trim();
    }

    private String extractContent(Map<?, ?> response) {
        if (response == null) return "";
        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) return "";
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) return "";
        Object messageObj = firstMap.get("message");
        if (!(messageObj instanceof Map<?, ?> message)) return "";
        Object contentObj = message.get("content");
        if (!(contentObj instanceof String content)) return "";
        return content.strip();
    }

    private boolean isRefusal(String text) {
        if (text == null) return false;
        String t = text.toLowerCase();
        return t.contains("i can't assist")
                || t.contains("i cannot assist")
                || t.contains("i'm sorry")
                || t.contains("cannot comply")
                || t.contains("죄송")
                || t.contains("도와드릴 수");
    }
}
