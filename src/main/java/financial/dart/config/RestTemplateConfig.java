package financial.dart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // ⏳ 중요: 무한 대기 방지 (10초 지나면 끊어버림)
        factory.setConnectTimeout(10000); // 연결 5초
        factory.setReadTimeout(10000);    // 데이터 수신 5초

        return new RestTemplate(factory);
    }
}