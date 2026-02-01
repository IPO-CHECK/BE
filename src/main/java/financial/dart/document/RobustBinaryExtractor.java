//package financial.dart.document;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipInputStream;
//
//public class RobustBinaryExtractor {
//
//    private RobustBinaryExtractor() {}
//
//    public static ExtractResult extractPdfFromUnknown(byte[] bytes) {
//        if (bytes == null || bytes.length == 0) {
//            throw new IllegalArgumentException("응답 바이트가 비어있습니다.");
//        }
//
//        // 1) PDF 직접 응답인지 확인
//        if (startsWith(bytes, "%PDF")) {
//            return new ExtractResult(bytes, List.of("[direct] PDF response"), "DIRECT_PDF");
//        }
//
//        // 2) ZIP인지 확인 (PK..)
//        if (startsWithZip(bytes)) {
//            return extractPdfFromZip(bytes);
//        }
//
//        // 3) ZIP도 PDF도 아니면: 에러 XML/HTML 텍스트일 가능성 큼
//        String head = new String(bytes, 0, Math.min(bytes.length, 500), StandardCharsets.UTF_8);
//        throw new IllegalStateException(
//                "응답이 ZIP/PDF가 아닙니다. (에러 응답 가능)\n--- head(UTF-8, 500) ---\n" + head
//        );
//    }
//
//    private static ExtractResult extractPdfFromZip(byte[] zipBytes) {
//        List<String> entries = listEntries(zipBytes);
//
//        // 2-1) 1차: 같은 zip 안의 PDF 찾기
//        byte[] pdf = findPdfInZip(zipBytes);
//        if (pdf != null) {
//            return new ExtractResult(pdf, entries, "ZIP_PDF");
//        }
//
//        // 2-2) 2차: zip 안에 또 zip이 있는 경우(중첩 zip) 재귀 탐색
//        byte[] nestedPdf = findPdfInNestedZip(zipBytes);
//        if (nestedPdf != null) {
//            return new ExtractResult(nestedPdf, entries, "NESTED_ZIP_PDF");
//        }
//
//        throw new IllegalStateException(
//                "ZIP 내부에서 PDF 파일을 찾지 못했습니다.\nZIP entries=" + entries
//        );
//    }
//
//    private static byte[] findPdfInZip(byte[] zipBytes) {
//        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
//            ZipEntry entry;
//            while ((entry = zis.getNextEntry()) != null) {
//                if (entry.isDirectory()) continue;
//                String name = entry.getName();
//                if (name != null && name.toLowerCase().endsWith(".pdf")) {
//                    return readAllBytes(zis);
//                }
//            }
//            return null;
//        } catch (IOException e) {
//            throw new RuntimeException("ZIP 파싱 실패", e);
//        }
//    }
//
//    private static byte[] findPdfInNestedZip(byte[] zipBytes) {
//        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
//            ZipEntry entry;
//            while ((entry = zis.getNextEntry()) != null) {
//                if (entry.isDirectory()) continue;
//                String name = entry.getName();
//                byte[] data = readAllBytes(zis);
//
//                if (name != null && name.toLowerCase().endsWith(".zip") && startsWithZip(data)) {
//                    // 재귀적으로 중첩 zip 탐색
//                    byte[] pdf = findPdfInZip(data);
//                    if (pdf != null) return pdf;
//
//                    byte[] deeper = findPdfInNestedZip(data);
//                    if (deeper != null) return deeper;
//                }
//            }
//            return null;
//        } catch (IOException e) {
//            throw new RuntimeException("중첩 ZIP 파싱 실패", e);
//        }
//    }
//
//    public static List<String> listEntries(byte[] zipBytes) {
//        if (!startsWithZip(zipBytes)) return List.of("[not a zip]");
//        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
//            List<String> names = new ArrayList<>();
//            ZipEntry e;
//            while ((e = zis.getNextEntry()) != null) {
//                names.add(e.getName());
//            }
//            return names;
//        } catch (IOException e) {
//            return List.of("[zip read error: " + e.getMessage() + "]");
//        }
//    }
//
//    private static boolean startsWithZip(byte[] bytes) {
//        // ZIP: 'P' 'K' 0x03 0x04 (일반), 또는 PK.. 다른 변형도 존재해서 PK만 체크해도 충분
//        return bytes.length >= 2 && bytes[0] == 'P' && bytes[1] == 'K';
//    }
//
//    private static boolean startsWith(byte[] bytes, String magicAscii) {
//        byte[] m = magicAscii.getBytes(StandardCharsets.US_ASCII);
//        if (bytes.length < m.length) return false;
//        for (int i = 0; i < m.length; i++) {
//            if (bytes[i] != m[i]) return false;
//        }
//        return true;
//    }
//
//    private static byte[] readAllBytes(InputStream is) throws IOException {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        byte[] buf = new byte[8192];
//        int n;
//        while ((n = is.read(buf)) >= 0) bos.write(buf, 0, n);
//        return bos.toByteArray();
//    }
//
//    public record ExtractResult(byte[] pdfBytes, List<String> zipEntries, String mode) {}
//}