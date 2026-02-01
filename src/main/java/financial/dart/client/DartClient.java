package financial.dart.client;

import financial.dart.config.DartProperties;
import financial.dart.dto.DartListResponseDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class DartClient {

    private final RestClient restClient;
    private final DartProperties props;

    public DartClient(RestClient dartRestClient, DartProperties props) {
        this.restClient = dartRestClient;
        this.props = props;
    }

    /**
     * 요구사항 고정:
     * - bgn_de=19000101
     * - end_de=20260101
     * - pblntf_ty=A
     * - pblntf_detail_ty=A001
     * - page_count=10
     */
    public DartListResponseDto fetchBusinessReportList(String corpCode) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/list.json")
                        .queryParam("crtfc_key", props.apiKey())
                        .queryParam("corp_code", corpCode)
                        .queryParam("bgn_de", "19000101")
                        .queryParam("end_de", "20260101")
                        .queryParam("pblntf_ty", "A")
                        .queryParam("pblntf_detail_ty", "A001")
                        .queryParam("page_no", 1)
                        .queryParam("page_count", 10)
                        .build())
                .retrieve()
                .body(DartListResponseDto.class);
    }

    // ✅ document.xml ZIP 다운로드
    public byte[] downloadDocumentZip(String rceptNo) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/document.xml")
                        .queryParam("crtfc_key", props.apiKey())
                        .queryParam("rcept_no", rceptNo)
                        .build())
                .retrieve()
                .body(byte[].class);
    }
}