package gateway.api_gateway.filter;

import gateway.api_gateway.dto.Token;
import gateway.api_gateway.dto.UserInfo;
import gateway.api_gateway.validator.RouterValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtValidationFilter implements GatewayFilter {

    private final RouterValidator routerValidator;
    private final WebClient webClient;

    public JwtValidationFilter(RouterValidator routerValidator) {
        this.routerValidator = routerValidator;
        this.webClient = WebClient.builder().build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (!routerValidator.isSecured.test(request)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        Token tokenForValidate = new Token(token);

        return webClient.post()
                .uri("http://auth-service:8081/api/auth/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tokenForValidate)
                .retrieve()
                .bodyToMono(UserInfo.class)
                .flatMap(userInfo -> {
                    log.info("UserInfo from auth: {}", userInfo);

                    ServerHttpRequest mutated = exchange.getRequest()
                            .mutate()
                            .header("X-Is-Valid", String.valueOf(userInfo.isValid()))
                            .header("X-User-Id", userInfo.getUserId().toString())
                            .header("X-Role", userInfo.getRole())
                            .build();

                    return chain.filter(exchange.mutate().request(mutated).build());
                })
                .onErrorResume(e -> {
                    log.error("JWT validation failed", e);
                    return unauthorized(exchange, "Invalid token");
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String json = "{\"error\": \"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
