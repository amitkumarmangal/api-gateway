package com.business.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Component
public class JwtFilter implements WebFilter {
    private static final String SECRET = "w9pZ4m7Q1z2G5BzYFzL3KQ5XrC9a1N8ZrKJtYv9WlqE=";
    private static final List<String> BYPASS_URL= List.of("/actuator","/h2-console");
    private static final List<String> ALLOWED_USERS = List.of("admin", "bankuser");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if(BYPASS_URL.stream().noneMatch(url -> exchange.getRequest().getURI().getPath().startsWith(url))) {
            try {
                Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
                String auth = exchange.getRequest()
                        .getHeaders()
                        .getFirst(HttpHeaders.AUTHORIZATION);
                if (auth == null || !auth.startsWith("Bearer ")) {
                    return unauthorized(exchange);
                }
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(auth.replace("Bearer ", ""))
                        .getBody();
                String username = claims.getSubject();
                if (!ALLOWED_USERS.contains(username)) {
                    return unauthorized(exchange);
                }
            } catch (Exception e) {
                return unauthorized(exchange);
            }
        }
        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
