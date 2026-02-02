package financial.dart.vector.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiClientConfig {

    @Bean
    public RestClient openAiRestClient(RestClient.Builder builder, OpenAiProperties props) {
        String baseUrl = props.baseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.openai.com/v1";
        }
        return builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + props.apiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}