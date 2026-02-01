package financial.dart.document;

import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipMainXmlExtractor {

    /**
     * ZIP 내부에서 "{rceptNo}.xml" (언더스코어 없는 메인 XML)만 찾아서 문자열로 반환
     */
    public String extractMainXmlText(byte[] zipBytes, String rceptNo) {
        String target = rceptNo + ".xml";

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                String name = e.getName();

                // ✅ 딱 "20250311001085.xml"만
                if (!name.equals(target)) continue;

                byte[] bytes = zis.readAllBytes();
                return new String(bytes, StandardCharsets.UTF_8);
            }

            throw new IllegalStateException("ZIP 내부에서 메인 XML을 찾지 못했습니다. target=" + target);

        } catch (Exception ex) {
            throw new IllegalStateException("ZIP에서 메인 XML 추출 실패", ex);
        }
    }
}