//package financial.dart.document;
//
//import org.apache.pdfbox.Loader;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.text.PDFTextStripper;
//
//import java.io.ByteArrayInputStream;
//
//public class PdfTextExtractor {
//
//    private PdfTextExtractor() {}
//
//    public static String extractText(byte[] pdfBytes) {
//        if (pdfBytes == null || pdfBytes.length == 0) {
//            throw new IllegalArgumentException("PDF 바이트가 비어있습니다.");
//        }
//
//        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
//            PDFTextStripper stripper = new PDFTextStripper();
//            // 필요 시: stripper.setSortByPosition(true);
//            return stripper.getText(doc);
//        } catch (Exception e) {
//            throw new RuntimeException("PDF 텍스트 추출 실패", e);
//        }
//    }
//}