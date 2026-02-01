//package financial.dart.section.service;
//
//import financial.dart.client.DartViewerClient;
//import financial.dart.document.DartHtmlNavigator;
//import financial.dart.section.DartSectionExtractor;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CorpSectionHtmlCrawlService {
//
//    private final DartHtmlNavigator navigator;
//    private final DartViewerClient client;
//    private final DartSectionExtractor extractor = new DartSectionExtractor();
//
//    public CorpSectionHtmlCrawlService(DartHtmlNavigator navigator, DartViewerClient client) {
//        this.navigator = navigator;
//        this.client = client;
//    }
//
//    public Result fetchSectionsByRcpNo(String rcpNo) {
//        var params = navigator.resolveViewDocParams(rcpNo);
//        String viewerHtml = client.fetchViewerHtml(params);
//
//        var sections = extractor.extract(viewerHtml);
//
//        return new Result(
//                rcpNo,
//                sections.businessOverview().length(),
//                sections.productService().length(),
//                head(sections.businessOverview()),
//                head(sections.productService())
//        );
//    }
//
//    private String head(String s) {
//        if (s == null) return "";
//        String t = s.strip();
//        return t.substring(0, Math.min(300, t.length()));
//    }
//
//    public record Result(String rcpNo, int businessOverviewLength, int productServiceLength,
//                         String businessOverviewHead, String productServiceHead) {}
//}