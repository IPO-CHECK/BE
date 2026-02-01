//package financial.dart.document;
//
//import financial.dart.client.DartViewerClient;
//import financial.dart.client.ViewDocParams;
//import org.springframework.stereotype.Component;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Component
//public class DartHtmlNavigator {
//
//    private final DartViewerClient client;
//
//    // viewDoc('rcpNo','dcmNo','eleId','offset','length','dtd')
//    private static final Pattern VIEW_DOC =
//            Pattern.compile("viewDoc\\('\\s*(\\d+)\\s*'\\s*,\\s*'\\s*(\\d+)\\s*'\\s*,\\s*'\\s*(\\d+)\\s*'\\s*,\\s*'\\s*(\\d+)\\s*'\\s*,\\s*'\\s*(\\d+)\\s*'\\s*,\\s*'\\s*([^']+)\\s*'\\s*\\)");
//
//    // /report/viewer.do?rcpNo=...&dcmNo=...&eleId=...&offset=...&length=...&dtd=...
//    private static final Pattern VIEWER_QS =
//            Pattern.compile("viewer\\.do\\?rcpNo=(\\d+)&dcmNo=(\\d+)&eleId=(\\d+)&offset=(\\d+)&length=(\\d+)&dtd=([^&\"']+)");
//
//    public DartHtmlNavigator(DartViewerClient client) {
//        this.client = client;
//    }
//
//    public ViewDocParams resolveViewDocParams(String rcpNo) {
//        // 1) main.do는 “진입점”일 뿐, 여기서 iframe/viewDoc이 없을 수 있음
//        String mainHtml = client.fetchMainHtml(rcpNo);
//
//        // (A) 혹시 inline viewDoc이 있으면 바로 사용
//        ViewDocParams p = findViewDoc(mainHtml);
//        if (p != null) return p.withFullDoc();
//
//        // 2) ✅ 핵심: report/main을 “강제로” 호출 (DART 화면 구조상 여기서 목차/문서정보가 나옴)
//        String reportMainPath = "/report/main.do?rcpNo=" + rcpNo;
//        String reportMainHtml = client.fetchByPath(reportMainPath); // 302 follow 필수
//
//        // (B) report/main에서 viewDoc 찾기
//        p = findViewDoc(reportMainHtml);
//        if (p != null) return p.withFullDoc();
//
//        // (C) report/main에 viewer.do 쿼리스트링이 박혀있으면 그걸로 복원
//        p = findViewerQuery(reportMainHtml);
//        if (p != null) return p.withFullDoc();
//
//        throw new IllegalStateException(
//                "report/main에서도 viewDoc/viewer 파라미터를 찾지 못했습니다. rcpNo=" + rcpNo
//                        + ", mainHead=" + head(mainHtml)
//                        + ", reportMainHead=" + head(reportMainHtml)
//        );
//    }
//
//    private ViewDocParams findViewDoc(String html) {
//        if (html == null || html.isBlank()) return null;
//        Matcher m = VIEW_DOC.matcher(html);
//        if (!m.find()) return null;
//
//        return new ViewDocParams(
//                m.group(1), m.group(2), m.group(3),
//                m.group(4), m.group(5), m.group(6)
//        );
//    }
//
//    private ViewDocParams findViewerQuery(String html) {
//        if (html == null || html.isBlank()) return null;
//        Matcher m = VIEWER_QS.matcher(html);
//        if (!m.find()) return null;
//
//        return new ViewDocParams(
//                m.group(1), m.group(2), m.group(3),
//                m.group(4), m.group(5), m.group(6)
//        );
//    }
//
//    private static String head(String s) {
//        if (s == null) return "";
//        String t = s.replaceAll("\\s+", " ").trim();
//        return t.substring(0, Math.min(250, t.length()));
//    }
//}