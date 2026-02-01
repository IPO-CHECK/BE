//package financial.dart.section;
//
//public class HtmlTextNormalizer {
//
//    private HtmlTextNormalizer() {}
//
//    public static String normalize(String s) {
//        if (s == null) return "";
//
//        String x = s;
//
//        // 1) script/style 제거
//        x = x.replaceAll("(?is)<script.*?>.*?</script>", " ");
//        x = x.replaceAll("(?is)<style.*?>.*?</style>", " ");
//
//        // 2) 줄바꿈 역할 태그를 먼저 개행으로 바꾸기 (★★★★★ 중요)
//        x = x.replaceAll("(?is)<br\\s*/?>", "\n");
//        x = x.replaceAll("(?is)</p\\s*>", "\n");
//        x = x.replaceAll("(?is)</div\\s*>", "\n");
//        x = x.replaceAll("(?is)</li\\s*>", "\n");
//        x = x.replaceAll("(?is)</tr\\s*>", "\n");
//        x = x.replaceAll("(?is)</h[1-6]\\s*>", "\n");
//        x = x.replaceAll("(?is)<p\\b[^>]*>", "");      // 시작태그는 제거
//        x = x.replaceAll("(?is)<div\\b[^>]*>", "");
//        x = x.replaceAll("(?is)<li\\b[^>]*>", "");
//        x = x.replaceAll("(?is)<tr\\b[^>]*>", "");
//        x = x.replaceAll("(?is)<h[1-6]\\b[^>]*>", "");
//
//        // 3) 나머지 태그 제거
//        x = x.replaceAll("(?is)<[^>]+>", " ");
//
//        // 4) HTML 엔티티 최소 처리
//        x = x.replace("&nbsp;", " ")
//                .replace("&amp;", "&")
//                .replace("&lt;", "<")
//                .replace("&gt;", ">")
//                .replace("&quot;", "\"")
//                .replace("&#39;", "'");
//
//        // 5) 공백/개행 정리
//        x = x.replaceAll("[\\t\\r]+", " ");
//        x = x.replaceAll("[ ]{2,}", " ");
//        x = x.replaceAll("\\n[ ]+", "\n");
//        x = x.replaceAll("\\n{3,}", "\n\n");
//
//        return x.trim();
//    }
//}