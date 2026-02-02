package financial.dart.section.service;

import financial.dart.client.DartClient;
import financial.dart.document.ZipMainXmlExtractor;
import org.springframework.stereotype.Service;

@Service
public class CorpSectionMainXmlService {

    private final DartClient dartClient;
    private final ZipMainXmlExtractor zipMainXmlExtractor;
    private final DartMainXmlSectionExtractor extractor;

    public CorpSectionMainXmlService(
            DartClient dartClient,
            ZipMainXmlExtractor zipMainXmlExtractor,
            DartMainXmlSectionExtractor extractor
    ) {
        this.dartClient = dartClient;
        this.zipMainXmlExtractor = zipMainXmlExtractor;
        this.extractor = extractor;
    }

    public DartMainXmlSectionExtractor.SectionPair fetchSectionsByRcpNo(String rcpNo) {
        byte[] zip = dartClient.downloadDocumentZip(rcpNo);

        // ✅ 언더스코어 없는 메인 XML만 사용
        String mainXml = zipMainXmlExtractor.extractMainXmlText(zip, rcpNo);

        return extractor.extractFromMainXml(mainXml);
    }
}