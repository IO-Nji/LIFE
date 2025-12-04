package io.life.samplefactory.gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

/**
 * Simple gateway-level JWT validation. Requests to public paths are forwarded without checks,
 * while every other route must include a valid {@code Authorization: Bearer <token>} header.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtParser jwtParser;
    private final List<String> publicPaths = List.of(
        "/api/auth/login",
        "/actuator/health",
        "/actuator/info",
        "/error"
    );

    public JwtAuthenticationFilter(GatewayJwtProperties properties) {
        if (!StringUtils.hasText(properties.getSecret())) {
            throw new IllegalStateException("JWT secret must be configured for the gateway");
        }
        this.jwtParser = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8)))
            .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicPath(path) || HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtParser.parseClaimsJws(token).getBody();
            ServerHttpRequest mutated = request.mutate()
                .header("X-Authenticated-User", claims.getSubject())
                .headers(headers -> {
                    Object role = claims.get("role");
                    if (role != null) {
                        headers.set("X-Authenticated-Role", role.toString());
                    }
                })
                .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (JwtException ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
