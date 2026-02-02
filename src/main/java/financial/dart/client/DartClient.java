package financial.dart.client;

import financial.dart.config.DartProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class DartClient {

    private final RestClient restClient;
    private final DartProperties props;

    public DartClient(RestClient dartRestClient, DartProperties props) {
        this.restClient = dartRestClient;
        this.props = props;
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

    // ✅ 증권신고서 정보(estkRs) 조회
    public Map<?, ?> fetchEstkRs(String corpCode, String bgnDe, String endDe) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/estkRs.json")
                        .queryParam("crtfc_key", props.apiKey())
                        .queryParam("corp_code", corpCode)
                        .queryParam("bgn_de", bgnDe)
                        .queryParam("end_de", endDe)
                        .build())
                .retrieve()
                .body(Map.class);
    }
}