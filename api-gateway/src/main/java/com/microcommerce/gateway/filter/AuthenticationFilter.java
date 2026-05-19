package com.microcommerce.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();
            HttpMethod method = request.getMethod();

            boolean isAuthEndpoint = path.startsWith("/api/auth/");
            boolean isPublicProductRead = HttpMethod.GET.equals(method)
                    && path.startsWith("/api/products");
            boolean isPublic = isAuthEndpoint || isPublicProductRead;

            if (isPublic) {
                return chain.filter(exchange);
            }

            // Validate Authorization header
            if (!request.getHeaders().containsKey("Authorization")) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            String authHeader = request.getHeaders().getFirst("Authorization");
            
            // Validate Bearer token format
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            // Extract token (for future JWT validation)
            String token = authHeader.substring(7);
            
            // TODO: When User Service is implemented, validate JWT token here
            // For now, we just check the format
            
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}

