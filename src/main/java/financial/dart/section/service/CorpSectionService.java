//package financial.dart.section.service;
//
//import financial.dart.document.service.DocumentService;
//import financial.dart.section.PdfBusinessSectionExtractor;
//import financial.dart.section.PdfTextNormalizer;
//import financial.dart.section.domain.CorpSection;
//import financial.dart.section.repository.CorpSectionRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.nio.charset.StandardCharsets;
//import java.security.MessageDigest;
//
//@Service
//public class CorpSectionService {
//
//    public static final String BUSINESS_OVERVIEW = "BUSINESS_OVERVIEW";
//    public static final String PRODUCT_SERVICE  = "PRODUCT_SERVICE";
//
//    private final DocumentService documentService;
//    private final CorpSectionRepository repo;
//
//    private final PdfBusinessSectionExtractor extractor = new PdfBusinessSectionExtractor();
//
//    public CorpSectionService(DocumentService documentService, CorpSectionRepository repo) {
//        this.documentService = documentService;
//        this.repo = repo;
//    }
//
//    @Transactional
//    public Result fetchAndExtractSections(String corpCode, String rceptNo) {
//        // ✅ PDF 텍스트로 전환
//        String pdfText = documentService.fetchPdfTextByRceptNo(rceptNo);
//        String plain = PdfTextNormalizer.normalize(pdfText);
//
//        var ex = extractor.extract(plain);
//
//        String overview = ex.businessOverview();
//        String product  = ex.mainProductsAndServices();
//
//        saveIfAbsent(corpCode, rceptNo, BUSINESS_OVERVIEW, overview);
//        saveIfAbsent(corpCode, rceptNo, PRODUCT_SERVICE, product);
//
//        return new Result(rceptNo, overview.length(), product.length(),
//                head(overview), head(product));
//    }
//
//    private void saveIfAbsent(String corpCode, String rceptNo, String type, String text) {
//        String hash = sha256(text);
//        repo.findByCorpCodeAndRceptNoAndSectionType(corpCode, rceptNo, type)
//                .orElseGet(() -> repo.save(new CorpSection(corpCode, rceptNo, type, text, hash)));
//    }
//
//    private static String head(String s) {
//        if (s == null) return "";
//        s = s.strip();
//        return s.substring(0, Math.min(250, s.length()));
//    }
//
//    private static String sha256(String s) {
//        try {
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            byte[] digest = md.digest((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
//            StringBuilder sb = new StringBuilder();
//            for (byte b : digest) sb.append(String.format("%02x", b));
//            return sb.toString();
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    public record Result(String rceptNo, int businessOverviewLength, int productServiceLength,
//                         String businessOverviewHead, String productServiceHead) {}
//}