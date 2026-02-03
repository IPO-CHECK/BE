package financial.dart.document;

import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipMainXmlExtractor {

    /**
     * ZIP 내부에서 rceptNo로 시작하고 언더스코어가 없는 메인 XML만 찾아서 반환
     */
    public String extractMainXmlText(byte[] zipBytes, String rceptNo) {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                String name = e.getName();
                String baseName = name.substring(name.lastIndexOf('/') + 1);

                // ✅ rceptNo로 시작하고, 언더스코어가 없고, .xml 확장자인 파일만
                if (!baseName.startsWith(rceptNo)) continue;
                if (!baseName.endsWith(".xml")) continue;
                if (baseName.contains("_")) continue;

                byte[] bytes = zis.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }

            throw new IllegalStateException("ZIP 내부에서 메인 XML을 찾지 못했습니다. rceptNo=" + rceptNo);

        } catch (Exception ex) {
            throw new IllegalStateException("ZIP에서 메인 XML 추출 실패", ex);
        }
    }
}