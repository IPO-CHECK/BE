//package financial.dart.document.service;
//
//import financial.dart.client.DartClient;
//import financial.dart.document.PdfTextExtractor;
//import financial.dart.document.RobustBinaryExtractor;
//import org.springframework.stereotype.Service;
//
//@Service
//public class DocumentService {
//
//    private final DartClient dartClient;
//
//    public DocumentService(DartClient dartClient) {
//        this.dartClient = dartClient;
//    }
//
//    public String fetchPdfTextByRceptNo(String rceptNo) {
//        byte[] bytes = dartClient.downloadDocumentZip(rceptNo);
//
//        // ✅ ZIP/PDF/에러응답까지 모두 처리
//        var res = RobustBinaryExtractor.extractPdfFromUnknown(bytes);
//
//        // 디버그: 어떤 모드로 추출됐는지 + zip 엔트리 확인
//        System.out.println("[DART] extract mode=" + res.mode());
//        System.out.println("[DART] zip entries=" + res.zipEntries());
//
//        return PdfTextExtractor.extractText(res.pdfBytes());
//    }
//}