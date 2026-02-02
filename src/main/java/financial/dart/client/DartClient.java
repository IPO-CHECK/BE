package financial.dart.client;

import financial.dart.config.DartProperties;
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