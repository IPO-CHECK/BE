package financial.dart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class DartClientConfig {

    @Bean
    public RestClient dartRestClient() {
        return RestClient.builder()
                .baseUrl("https://opendart.fss.or.kr/api")
                .build();
    }
}