package mx.com.nimbusti.api.nom151.integration.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final PscProperties pscProperties;
    private final JwtAuthInterceptor jwtAuthInterceptor;

    @Bean
    public RestClient pscRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(pscProperties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(pscProperties.getReadTimeoutMs());

        return RestClient.builder()
                .baseUrl(pscProperties.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE) // Exigido en especificación
                .requestInterceptor(jwtAuthInterceptor) // Inyección de JWT Bearer[cite: 1, 2]
                .build();
    }
}