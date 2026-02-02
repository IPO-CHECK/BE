package financial.dart.section;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BusinessSectionExtractor {

    // "II. 사업의 내용" (II/Ⅱ 모두 허용, 점/공백 유연)
    private static final Pattern BIZ_START = Pattern.compile(
            "(II|Ⅱ)\\s*\\.?\\s*사업\\s*의\\s*내용"
    );

    // "III. 재무에 관한 사항" (끝 경계)
    private static final Pattern BIZ_END = Pattern.compile(
            "(III|Ⅲ)\\s*\\.?\\s*재무\\s*에\\s*관한\\s*사항"
    );

    private static final Pattern SEC1 = Pattern.compile(
            "1\\s*\\.?\\s*사업\\s*의\\s*개요"
    );

    private static final Pattern SEC2 = Pattern.compile(
            "2\\s*\\.?\\s*주요\\s*제품\\s*및\\s*서비스"
    );

    private static final Pattern SEC3 = Pattern.compile(
            "3\\s*\\.?\\s*" // 다음 절 (3. ...) 시작점까지만 자르기 위한 용도
    );

    public Extracted extractFromFullText(String text) {
        String scope = extractBizBlock(text).orElse(text);

        String overview = sliceBetween(scope, SEC1, SEC2).orElse("");
        String products = sliceBetween(scope, SEC2, SEC3).orElse("");

        overview = overview.strip();
        products = products.strip();

        // 너무 짧으면 실패로 간주
        if (overview.length() < 300) overview = "";
        if (products.length() < 300) products = "";

        return new Extracted(overview, products);
    }

    private Optional<String> extractBizBlock(String text) {
        Matcher s = BIZ_START.matcher(text);
        if (!s.find()) return Optional.empty();

        int start = s.start();

        Matcher e = BIZ_END.matcher(text);
        int end = text.length();
        if (e.find(start)) {
            end = e.start();
        }

        return Optional.of(text.substring(start, end));
    }

    private Optional<String> sliceBetween(String text, Pattern from, Pattern to) {
        Matcher mFrom = from.matcher(text);
        if (!mFrom.find()) return Optional.empty();
        int start = mFrom.start();

        Matcher mTo = to.matcher(text);
        int end = text.length();
        if (mTo.find(mFrom.end())) {
            end = mTo.start();
        }
        return Optional.of(text.substring(start, end));
    }

    public record Extracted(String businessOverview, String mainProductsAndServices) {}
}