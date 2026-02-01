package financial.dart.section;

import org.jsoup.Jsoup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DartSectionExtractor {

    // "1. 사업의 개요" ~ 다음 "2." 또는 다음 큰 목차 전까지
    private static final Pattern OVERVIEW_START =
            Pattern.compile("(?m)^\\s*1\\s*\\.\\s*사업의\\s*개요\\s*$");
    private static final Pattern PRODUCT_START =
            Pattern.compile("(?m)^\\s*2\\s*\\.\\s*주요\\s*제품\\s*및\\s*서비스\\s*$");

    private static final Pattern NEXT_SECTION =
            Pattern.compile("(?m)^\\s*(?:[1-9]\\s*\\.|[IVX]+\\s*\\.|\\d+\\s*\\))\\s+.*$");

    public ExtractedSections extract(String viewerHtml) {
        // HTML -> 텍스트 (Jsoup이 태그 제거 + 공백 정리에 좋음)
        String text = Jsoup.parse(viewerHtml).text();
        // 단, DART 문서는 줄바꿈이 중요할 수 있어 간단히 보강
        text = text.replace("\u00A0", " ").replaceAll("\\s+", " ").trim();

        String overview = sliceByHeaders(text, OVERVIEW_START, PRODUCT_START); // 1번은 2번 전까지
        String product = sliceByHeaders(text, PRODUCT_START, null);

        return new ExtractedSections(overview, product);
    }

    private String sliceByHeaders(String text, Pattern start, Pattern nextStart) {
        int s = findIndex(text, start);
        if (s < 0) return "";

        int from = s;
        int to;

        if (nextStart != null) {
            int n = findIndexFrom(text, nextStart, s + 1);
            to = (n > 0) ? n : text.length();
        } else {
            // 다음 섹션(예: 3. ~~)를 찾되, 없으면 끝까지
            Matcher m = NEXT_SECTION.matcher(text);
            m.region(s + 1, text.length());
            to = m.find() ? m.start() : text.length();
        }

        return text.substring(from, to).trim();
    }

    private int findIndex(String text, Pattern p) {
        Matcher m = p.matcher(text);
        return m.find() ? m.start() : -1;
    }

    private int findIndexFrom(String text, Pattern p, int from) {
        Matcher m = p.matcher(text);
        m.region(from, text.length());
        return m.find() ? m.start() : -1;
    }

    public record ExtractedSections(String businessOverview, String productService) {}
}