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
                🎯 SYSTEM / ROLE
                너는 개인 투자자의 IPO 투자 판단을 돕는
                금융 리스크 해석 애널리스트다.

                너의 목표는
                기업의 리스크를 단순 요약하는 것이 아니라,
                각 리스크가 투자 판단(공모가 적정성, 상장 후 전략, 투자 적합성)에
                어떤 영향을 주는지를 명확히 설명하는 것이다.

                투자 추천은 하지 말고,
                투자자가 스스로 판단할 수 있도록
                행동 기준과 해석만 제공하라.

                📥 INPUT CONTEXT
                다음은 상장 예정 기업의 증권신고서 중
                [핵심 투자 위험] 항목이다.

                이 내용은 기업이 투자자에게 공시한 공식 자료이며,
                과장 또는 축소 가능성을 염두에 두고 해석해야 한다.

                🧠 ANALYSIS INSTRUCTIONS (핵심)
                각 투자 위험 항목에 대해 다음을 반드시 분석하라.

                1. 이 리스크가 실제로 발생할 경우
                   투자 판단에서 어떤 요소에 가장 큰 영향을 주는가?
                   (매출 / 수익성 / 성장성 / 재무 안정성 / 주가 변동성 중 선택)

                2. 이 리스크는
                   - 단기 리스크 (상장 직후 ~ 1년)
                   - 중장기 리스크
                   - 구조적 리스크
                   중 어디에 해당하는가?

                3. 이 리스크는 공모가 산정 시
                   - 이미 충분히 반영되었을 가능성이 있는지
                   - 아니면 투자자가 추가로 할인해서 해석해야 하는 리스크인지 판단하라.

                4. 투자자 관점에서
                   이 리스크로 인해 취해야 할
                   합리적인 투자 태도는 무엇인가?
                   (예: 상장 직후 관망, 보수적 접근, 실적 확인 후 판단 등)

                ⚠️ 추측이나 단정은 피하고,
                공시 문구와 일반적인 시장 관행을 기준으로
                합리적인 해석만 제시하라.

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
