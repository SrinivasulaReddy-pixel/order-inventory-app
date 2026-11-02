package com.orderinventory.gateway.filter;

import com.orderinventory.common.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Edge-level JWT check: rejects missing/invalid/expired tokens before a request is
 * even routed to a downstream service. Each downstream service (order/inventory/
 * notification) ALSO validates the token independently with Spring Security - this
 * is defense in depth, not a replacement for it.
 */
@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationGatewayFilter.class);

    // Routes that don't require a token (registration/login).
    private static final String[] PUBLIC_PATHS = { "/api/auth/register", "/api/auth/login" };

    private final JwtUtil jwtUtil;

    public JwtAuthenticationGatewayFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Browser CORS preflight requests never carry Authorization - let them through
        // so the CorsWebFilter can answer them; the actual request still gets checked below.
        if (exchange.getRequest().getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        for (String publicPath : PUBLIC_PATHS) {
            if (path.equals(publicPath)) {
                return chain.filter(exchange);
            }
        }

        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing bearer token");
        }

        try {
            jwtUtil.parseAndValidate(authHeader.substring(7));
        } catch (Exception ex) {
            log.warn("Rejected request to {}: invalid token ({})", path, ex.getMessage());
            return unauthorized(exchange, "Invalid or expired token");
        }

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // run before routing filters
    }
}
