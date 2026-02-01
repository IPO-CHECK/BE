//package financial.dart.section;
//
//import java.util.Optional;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class PdfBusinessSectionExtractor {
//
//    // II/Ⅱ 모두 허용, 점 유무/공백 유연
//    private static final Pattern BIZ_START = Pattern.compile("(II|Ⅱ)\\s*\\.?\\s*사업\\s*의\\s*내용");
//    private static final Pattern BIZ_END   = Pattern.compile("(III|Ⅲ)\\s*\\.?\\s*재무\\s*에\\s*관한\\s*사항");
//
//    private static final Pattern SEC1 = Pattern.compile("1\\s*\\.?\\s*사업\\s*의\\s*개요");
//    private static final Pattern SEC2 = Pattern.compile("2\\s*\\.?\\s*주요\\s*제품\\s*및\\s*서비스");
//
//    // 3. ~ 또는 다음 큰 절 패턴
//    private static final Pattern NEXT_AFTER_2 = Pattern.compile("\\n\\s*3\\s*\\.?\\s*");
//
//    public Extracted extract(String normalizedText) {
//        String scope = extractBizBlock(normalizedText).orElse(normalizedText);
//
//        String overview = sliceBetween(scope, SEC1, SEC2).orElse("");
//        String products = sliceBetween(scope, SEC2, NEXT_AFTER_2).orElse("");
//
//        overview = overview.strip();
//        products = products.strip();
//
//        if (overview.length() < 300) overview = "";
//        if (products.length() < 300) products = "";
//
//        return new Extracted(overview, products);
//    }
//
//    private Optional<String> extractBizBlock(String text) {
//        Matcher s = BIZ_START.matcher(text);
//        if (!s.find()) return Optional.empty();
//        int start = s.start();
//
//        Matcher e = BIZ_END.matcher(text);
//        int end = text.length();
//        if (e.find(s.end())) end = e.start();
//
//        return Optional.of(text.substring(start, end));
//    }
//
//    private Optional<String> sliceBetween(String text, Pattern from, Pattern to) {
//        Matcher a = from.matcher(text);
//        if (!a.find()) return Optional.empty();
//        int start = a.start();
//
//        Matcher b = to.matcher(text);
//        int end = text.length();
//        if (b.find(a.end())) end = b.start();
//
//        return Optional.of(text.substring(start, end));
//    }
//
//    public record Extracted(String businessOverview, String mainProductsAndServices) {}
//}