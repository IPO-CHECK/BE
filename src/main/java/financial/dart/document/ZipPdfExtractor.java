package financial.dart.document;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipPdfExtractor {

    private ZipPdfExtractor() {}

    public static byte[] extractPdf(byte[] zipBytes) {
        if (zipBytes == null || zipBytes.length == 0) {
            throw new IllegalArgumentException("ZIP 바이트가 비어있습니다.");
        }

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                String name = entry.getName();
                if (name != null && name.toLowerCase().endsWith(".pdf")) {
                    return readAllBytes(zis);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("ZIP에서 PDF 추출 실패", e);
        }

        throw new IllegalStateException("ZIP 내부에서 PDF 파일을 찾지 못했습니다.");
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) >= 0) bos.write(buf, 0, n);
        return bos.toByteArray();
    }
}