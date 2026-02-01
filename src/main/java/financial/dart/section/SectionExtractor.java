//package financial.dart.section;
//
//import java.util.List;
//
//public class SectionExtractor {
//
//    // 사업 개요 헤딩 후보
//    private static final List<String> OVERVIEW_HEADERS = List.of(
//            "사업의 개요", "사업 개요", "I. 사업의 개요", "1. 사업의 개요", "1. 사업개요"
//    );
//
//    // 주요 제품/서비스 헤딩 후보
//    private static final List<String> PRODUCT_HEADERS = List.of(
//            "주요 제품 및 서비스", "주요제품 및 서비스", "주요 제품", "제품 및 서비스"
//    );
//
//    public static String extractBusinessOverview(String plain) {
//        return extractByHeaders(plain, OVERVIEW_HEADERS);
//    }
//
//    public static String extractMainProductsAndServices(String plain) {
//        return extractByHeaders(plain, PRODUCT_HEADERS);
//    }
//
//    /**
//     * 헤더 위치를 찾고, 그 다음 "상위 레벨로 보이는" 다음 헤더까지를 잘라냅니다.
//     * MVP용 휴리스틱입니다.
//     */
//    private static String extractByHeaders(String plain, List<String> headers) {
//        int start = -1;
//        String matchedHeader = null;
//
//        for (String h : headers) {
//            int idx = indexOfHeader(plain, h);
//            if (idx >= 0 && (start < 0 || idx < start)) {
//                start = idx;
//                matchedHeader = h;
//            }
//        }
//        if (start < 0) return "";
//
//        // 시작을 헤더 라인부터로 맞춤
//        int from = moveToLineStart(plain, start);
//
//        // 끝: 다음 큰 섹션처럼 보이는 헤더를 찾음
//        int end = findNextMajorHeader(plain, from + (matchedHeader == null ? 0 : matchedHeader.length()));
//
//        if (end < 0) end = Math.min(plain.length(), from + 20000); // 너무 길면 컷(안전장치)
//
//        String cut = plain.substring(from, end).trim();
//        // 너무 짧으면 실패로 취급
//        return cut.length() < 200 ? "" : cut;
//    }
//
//    private static int indexOfHeader(String text, String header) {
//        // 줄 단위로 헤더가 나오는 경우가 많아서, 주변에 개행이 있는 패턴을 우선 탐색
//        int idx = text.indexOf("\n" + header);
//        if (idx >= 0) return idx + 1;
//        idx = text.indexOf(header + "\n");
//        if (idx >= 0) return idx;
//
//        // fallback
//        return text.indexOf(header);
//    }
//
//    private static int moveToLineStart(String text, int pos) {
//        int p = Math.max(0, pos);
//        while (p > 0 && text.charAt(p - 1) != '\n') p--;
//        return p;
//    }
//
//    private static int findNextMajorHeader(String text, int from) {
//        // “다음 대제목” 후보: "II.", "2.", "3." 같은 패턴 + "사업" 등
//        // 너무 공격적으로 잡으면 잘릴 수 있으니 MVP에서는 완만하게.
//        String[] patterns = {
//                "\nII.", "\nIII.", "\nIV.", "\nV.",
//                "\n2.", "\n3.", "\n4.", "\n5.",
//                "\nⅡ.", "\nⅢ.", "\nⅣ.", "\nⅤ."
//        };
//
//        int min = -1;
//        for (String p : patterns) {
//            int idx = text.indexOf(p, from);
//            if (idx >= 0 && (min < 0 || idx < min)) min = idx + 1;
//        }
//        return min;
//    }
//}