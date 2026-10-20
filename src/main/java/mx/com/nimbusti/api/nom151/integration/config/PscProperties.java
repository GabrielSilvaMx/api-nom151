package mx.com.nimbusti.api.nom151.integration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "psc.codex")
public class PscProperties {
    private String baseUrl;
    private String jwtToken;
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 10000;
}