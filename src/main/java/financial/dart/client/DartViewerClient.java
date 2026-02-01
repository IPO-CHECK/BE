//package financial.dart.client;
//
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestClient;
//
//@Component
//public class DartViewerClient {
//
//    private final RestClient restClient;
//
//    public DartViewerClient() {
//        this.restClient = RestClient.builder()
//                .baseUrl("https://dart.fss.or.kr")
//                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0")
//                .build();
//    }
//
//    public String fetchMainHtml(String rcpNo) {
//        return fetchHtml("/dsaf001/main.do?rcpNo=" + rcpNo);
//    }
//
//    public String fetchByPath(String pathWithQuery) {
//        return fetchHtml(pathWithQuery);
//    }
//
//    public String fetchViewerHtml(ViewDocParams p) {
//        String url = "/report/viewer.do"
//                + "?rcpNo=" + p.rcpNo()
//                + "&dcmNo=" + p.dcmNo()
//                + "&eleId=" + p.eleId()
//                + "&offset=" + p.offset()
//                + "&length=" + p.length()
//                + "&dtd=" + p.dtd();
//        return fetchHtml(url);
//    }
//
//    private String fetchHtml(String pathWithQuery) {
//        return fetchHtml(pathWithQuery, 0);
//    }
//
//    private String fetchHtml(String pathWithQuery, int depth) {
//        if (depth > 5) {
//            throw new IllegalStateException("DART redirect too deep: " + pathWithQuery);
//        }
//
//        ResponseEntity<String> res = restClient.get()
//                .uri(pathWithQuery)
//                .exchange((req, resp) -> {
//                    int status = resp.getStatusCode().value();
//                    HttpHeaders headers = resp.getHeaders();
//                    String body = (resp.getBody() == null) ? null : new String(resp.getBody().readAllBytes());
//                    return ResponseEntity.status(status).headers(headers).body(body);
//                });
//
//        int status = res.getStatusCode().value();
//        String body = res.getBody();
//        int len = (body == null) ? -1 : body.length();
//
//        System.out.println("[DART-HTML] GET " + pathWithQuery + " -> " + status + ", len=" + len);
//
//        if (status >= 300 && status < 400) {
//            String location = res.getHeaders().getFirst(HttpHeaders.LOCATION);
//            if (location == null || location.isBlank()) {
//                throw new IllegalStateException("3xx without Location: " + pathWithQuery + " status=" + status);
//            }
//            String next = normalizeLocation(location);
//            System.out.println("[DART-HTML] redirect -> " + next);
//            return fetchHtml(next, depth + 1);
//        }
//
//        if (body == null || body.isBlank()) {
//            throw new IllegalStateException("Empty HTML: " + pathWithQuery + " (status=" + status + ")");
//        }
//
//        return body;
//    }
//
//    private String normalizeLocation(String location) {
//        if (location.startsWith("http://") || location.startsWith("https://")) {
//            int idx = location.indexOf("dart.fss.or.kr");
//            if (idx >= 0) {
//                int slash = location.indexOf("/", idx + "dart.fss.or.kr".length());
//                return (slash >= 0) ? location.substring(slash) : "/";
//            }
//            // 다른 도메인이면 그대로 두되(대부분은 dart 도메인임), 필요 시 별도 처리
//            return location;
//        }
//        return location.startsWith("/") ? location : "/" + location;
//    }
//}