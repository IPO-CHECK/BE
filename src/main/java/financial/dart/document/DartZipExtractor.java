package financial.dart.document;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DartZipExtractor {

    private DartZipExtractor() {}

    public static String extractDocumentXmlPreferDocument(byte[] zipBytes) {
        if (zipBytes == null || zipBytes.length == 0) {
            throw new IllegalArgumentException("ZIP 바이트가 비어있습니다.");
        }

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;

            // 1차: document.xml 정확히 찾기
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (name != null && name.equalsIgnoreCase("document.xml")) {
                    return decodeSmart(readAllBytes(zis));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("ZIP 해제 실패", e);
        }

        // 2차: 다시 열어서 xml 아무거나(최후 수단)
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (name != null && name.toLowerCase().endsWith(".xml")) {
                    return decodeSmart(readAllBytes(zis));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("ZIP 해제 실패(2차)", e);
        }

        throw new IllegalStateException("ZIP 내부에서 XML 파일을 찾지 못했습니다.");
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) >= 0) bos.write(buf, 0, n);
        return bos.toByteArray();
    }

    private static String decodeSmart(byte[] data) {
        String text = new String(data, Charset.forName("UTF-8"));
        if (looksBroken(text)) {
            text = new String(data, Charset.forName("EUC-KR"));
        }
        return text;
    }

    private static boolean looksBroken(String s) {
        if (s == null) return true;
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\uFFFD') count++;
            if (count >= 10) return true;
        }
        return false;
    }
}