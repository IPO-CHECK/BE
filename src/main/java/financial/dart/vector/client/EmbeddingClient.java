package financial.dart.vector.client;

public interface EmbeddingClient {

    /**
     * 단일 텍스트 → 벡터
     */
    float[] embedOne(String text);
}