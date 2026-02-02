package financial.dart.vector.client;

import financial.dart.vector.dto.*;
import financial.dart.vector.properties.QdrantProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

//@Component
public class QdrantClient {

    private final RestClient restClient;
    private final QdrantProperties props;

    public QdrantClient(RestClient qdrantRestClient, QdrantProperties props) {
        this.restClient = qdrantRestClient;
        this.props = props;
    }

    public void ensureCollection() {
        // 컬렉션 존재 확인
        HttpStatus status = restClient.get()
                .uri("/collections/{name}", props.collection())
                .exchange((req, res) -> HttpStatus.valueOf(res.getStatusCode().value()));

        if (status.is2xxSuccessful()) return;

        // 없으면 생성
        Map<String, Object> vectorConfig = new HashMap<>();
        vectorConfig.put("size", props.vectorSize());
        vectorConfig.put("distance", "Cosine");

        QdrantCreateCollectionRequest body = new QdrantCreateCollectionRequest(
                Map.of("default", vectorConfig)
        );

        restClient.put()
                .uri("/collections/{name}", props.collection())
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    public void upsert(List<QdrantUpsertRequest.Point> points) {
        if (points == null || points.isEmpty()) return;

        QdrantUpsertRequest req = new QdrantUpsertRequest(points);

        restClient.put()
                .uri("/collections/{name}/points?wait=true", props.collection())
                .body(req)
                .retrieve()
                .toBodilessEntity();
    }

    public List<QdrantSearchResponse.Result> search(float[] queryVector, int limit, Map<String, Object> filter) {
        if (queryVector == null || queryVector.length == 0) return List.of();

        List<Float> vec = new ArrayList<>(queryVector.length);
        for (float v : queryVector) vec.add(v);

        QdrantSearchRequest req = new QdrantSearchRequest(vec, limit, true, filter);

        QdrantSearchResponse res = restClient.post()
                .uri("/collections/{name}/points/search", props.collection())
                .body(req)
                .retrieve()
                .body(QdrantSearchResponse.class);

        if (res == null || res.result() == null) return List.of();
        return res.result();
    }

    public static List<Float> toFloatList(float[] v) {
        List<Float> list = new ArrayList<>(v.length);
        for (float f : v) {
            list.add(f);
        }
        return list;
    }
}