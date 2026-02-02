package financial.dart.vector.service;

import financial.dart.client.DartClient;
import financial.dart.dto.DartListResponseDto;
import financial.dart.section.service.CorpSectionMainXmlService;
import org.springframework.stereotype.Component;

@Component
public class DartMainXmlSectionProvider implements CorpSectionProvider {

    private final DartClient dartClient;
    private final CorpSectionMainXmlService mainXmlService;

    public DartMainXmlSectionProvider(DartClient dartClient,
                                      CorpSectionMainXmlService mainXmlService) {
        this.dartClient = dartClient;
        this.mainXmlService = mainXmlService;
    }

    @Override
    public ExtractedSections fetchLatestByCorpCode(String corpCode) {
        // 1) 공시 목록(list.json) 조회
        DartListResponseDto list = dartClient.fetchBusinessReportList(corpCode);

        if (list == null || list.list() == null || list.list().isEmpty()) {
            throw new IllegalStateException("사업보고서(A/A001) 공시가 없습니다. corpCode=" + corpCode);
        }

        // 2) 가장 최신 rcept_no (list.json은 보통 최신순으로 내려오지만, 안전하게 첫 번째 사용)
        String latestRcpNo = list.list().get(0).rceptNo();
        if (latestRcpNo == null || latestRcpNo.isBlank()) {
            throw new IllegalStateException("최신 rcpNo를 찾지 못했습니다. corpCode=" + corpCode);
        }

        // 3) ZIP(document.xml)에서 _안붙은 메인 XML 추출 → 섹션 추출
        var sections = mainXmlService.fetchSectionsByRcpNo(latestRcpNo);

        return new ExtractedSections(
                corpCode,
                latestRcpNo,
                sections.businessOverview(),
                sections.productService()
        );
    }
}