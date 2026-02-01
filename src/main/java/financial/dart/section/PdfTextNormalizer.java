//package financial.dart.section;
//
//public class PdfTextNormalizer {
//
//    private PdfTextNormalizer() {}
//
//    public static String normalize(String s) {
//        if (s == null) return "";
//        String x = s;
//
//        // 공백 정리
//        x = x.replace("\u00A0", " "); // nbsp
//        x = x.replaceAll("[ \\t]{2,}", " ");
//        x = x.replaceAll("\\r", "\n");
//        x = x.replaceAll("\\n{3,}", "\n\n");
//
//        return x.trim();
//    }
//}