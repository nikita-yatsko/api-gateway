package gateway.api_gateway.config;

import gateway.api_gateway.filter.JwtValidationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final JwtValidationFilter jwtValidationFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("order-service", r -> r.path("/api/item/**", "/api/order/**")
                        .filters(f -> f.filter(jwtValidationFilter))
                        .uri("http://order-service:8082"))
                .route("user-service", r -> r.path("/api/user/**", "/api/card/**")
                        .filters(f -> f.filter(jwtValidationFilter))
                        .uri("http://user-service:8083"))
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("http://auth-service:8081"))
                .route("payment-service", r -> r.path("/api/payment/**")
                        .filters(f -> f.filter(jwtValidationFilter))
                        .uri("http://payment-service:8084"))
                .build();
    }
}
