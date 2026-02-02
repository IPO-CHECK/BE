package financial.dart.section.service;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DartMainXmlSectionExtractor {

    // <TITLE ...>1. 사업의 개요</TITLE>
    private static final Pattern TITLE_BIZ = Pattern.compile(
            "(?s)<TITLE\\b[^>]*>\\s*" +
                    "1\\s*\\.\\s*" +
                    "(?:\\([^)]*\\)\\s*)*" +   // ← (제조) 같은 선택 영역
                    "사업의\\s*개요\\s*" +
                    "</TITLE>"
    );

    // <TITLE ...>2. 주요 제품 및 서비스</TITLE>
    private static final Pattern TITLE_PROD = Pattern.compile(
            "(?s)<TITLE\\b[^>]*>\\s*" +
                    "2\\s*\\.\\s*" +
                    "(?:\\([^)]*\\)\\s*)*" +   // ← 동일
                    "주요\\s*제품\\s*및\\s*서비스\\s*" +
                    "</TITLE>"
    );

    // <TITLE ...>1. 핵심투자위험</TITLE>
    private static final Pattern TITLE_KEY_RISK = Pattern.compile(
            "(?s)<TITLE\\b[^>]*>\\s*" +
                    "1\\s*\\.\\s*" +
                    "핵심\\s*투자\\s*위험\\s*" +
                    "</TITLE>"
    );

    // 다음 섹션 시작(보통 3. 으로 시작). 없으면 문서 끝까지.
    private static final Pattern TITLE_NEXT_AFTER_PROD = Pattern.compile(
            "(?s)<TITLE\\b[^>]*>\\s*(?:3\\s*\\.|Ⅲ\\s*\\.|III\\s*\\.)"
    );

    private static final Pattern TITLE_ANY = Pattern.compile(
            "(?s)<TITLE\\b"
    );

    public SectionPair extractFromMainXml(String rawXml) {
        String bizRaw = sliceBetweenTitles(rawXml, TITLE_BIZ, TITLE_PROD);
        String prodRaw = sliceAfterTitle(rawXml, TITLE_PROD, TITLE_NEXT_AFTER_PROD);

        String bizText = "1. 사업의 개요\n" + toPlainText(bizRaw).trim();
        String prodText = "2. 주요 제품 및 서비스\n" + toPlainText(prodRaw).trim();

        // 혹시라도 실패하면 빈 문자열로
        if (bizRaw.isBlank()) bizText = "";
        if (prodRaw.isBlank()) prodText = "";

        return new SectionPair(bizText, prodText);
    }

    public String extractKeyInvestmentRisk(String rawXml) {
        String riskRaw = sliceAfterTitle(rawXml, TITLE_KEY_RISK, TITLE_ANY);
        String riskText = "1. 핵심투자위험\n" + toPlainText(riskRaw).trim();
        if (riskRaw.isBlank()) riskText = "";
        return riskText;
    }

    /** startTitle 끝 ~ endTitle 시작 전까지 자르기 */
    private String sliceBetweenTitles(String xml, Pattern startTitle, Pattern endTitle) {
        Matcher s = startTitle.matcher(xml);
        if (!s.find()) return "";

        Matcher e = endTitle.matcher(xml);
        if (!e.find(s.end())) return ""; // start 이후에서 endTitle 못 찾으면 실패

        return xml.substring(s.end(), e.start());
    }

    /** startTitle 끝 ~ nextTitle 시작 전(없으면 문서 끝) */
    private String sliceAfterTitle(String xml, Pattern startTitle, Pattern nextTitle) {
        Matcher s = startTitle.matcher(xml);
        if (!s.find()) return "";

        Matcher n = nextTitle.matcher(xml);
        if (n.find(s.end())) {
            return xml.substring(s.end(), n.start());
        }
        return xml.substring(s.end());
    }

    /** XML 태그 제거 + 줄바꿈 보존 */
    private String toPlainText(String xmlFragment) {
        if (xmlFragment == null || xmlFragment.isBlank()) return "";

        String t = xmlFragment;

        // 줄바꿈으로 취급할 태그들
        t = t.replaceAll("(?i)<PGBRK\\b[^>]*>", "\n");
        t = t.replaceAll("(?i)</P>", "\n");
        t = t.replaceAll("(?i)<BR\\b[^>]*>", "\n");

        // 나머지 태그 제거
        t = t.replaceAll("(?s)<[^>]+>", " ");

        // 엔티티 복원(필요 최소)
        t = t.replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"");

        // 공백 정리
        t = t.replaceAll("[\\t\\r\\f]+", " ");
        t = t.replaceAll(" +", " ");
        t = t.replaceAll("\\n\\s+", "\n");

        return t;
    }

    public record SectionPair(String businessOverview, String productService) {}
}