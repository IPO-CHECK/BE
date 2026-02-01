//package financial.dart.controller;
//
//import financial.dart.document.domain.CorpDocument;
//import financial.dart.document.service.DocumentService;
//import financial.dart.service.FilingService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/corps")
//public class DocumentController {
//
//    private final FilingService filingService;
//    private final DocumentService documentService;
//
//    public DocumentController(FilingService filingService, DocumentService documentService) {
//        this.filingService = filingService;
//        this.documentService = documentService;
//    }
//
//    /**
//     * 최신 사업보고서 rcept_no를 (2단계)에서 얻고,
//     * (3단계) document.xml(zip) 다운로드 → XML 저장
//     */
////    @PostMapping("/{corpCode}/document/latest")
////    public ResponseEntity<?> fetchLatestDocument(@PathVariable String corpCode) {
////        var latestFiling = filingService.fetchAndSaveLatestBusinessReport(corpCode);
////        CorpDocument doc = documentService.fetchAndSaveDocumentXml(corpCode, latestFiling.getRceptNo());
////        return ResponseEntity.ok(new Result(latestFiling.getRceptNo(), doc.getDocXml().length()));
////    }
//
//    public record Result(String rceptNo, int xmlLength) {}
//}