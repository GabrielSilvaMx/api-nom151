package mx.com.nimbusti.api.nom151.integration.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements ClientHttpRequestInterceptor {

    private final PscProperties pscProperties;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String token = pscProperties.getJwtToken();
        if (token != null && !token.isBlank()) {
            String bearerAuth = token.startsWith("Bearer ") ? token : "Bearer " + token;
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, bearerAuth); // Requisito del PSC
        }
        return execution.execute(request, body);
    }
}