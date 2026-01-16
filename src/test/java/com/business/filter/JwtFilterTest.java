package com.business.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import static org.assertj.core.api.Assertions.assertThat;

class JwtFilterTest {

    private JwtFilter jwtFilter;
    private WebFilterChain filterChain;

    private static final String SECRET = "w9pZ4m7Q1z2G5BzYFzL3KQ5XrC9a1N8ZrKJtYv9WlqE=";

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter();
        filterChain = exchange -> {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        };
    }

    @Test
    void shouldBypassActuatorEndpoint() {
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/actuator/health").build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        Mono<Void> result = jwtFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnUnauthorized_whenAuthorizationHeaderMissing() {
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/data").build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        Mono<Void> result = jwtFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnUnauthorized_whenUserIsNotAllowed() {
        String token = generateToken("guest");

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/data")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        Mono<Void> result = jwtFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAllowRequest_whenTokenIsValidAndUserIsAllowed() {
        String token = generateToken("admin");

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/data")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        Mono<Void> result = jwtFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnUnauthorized_whenTokenIsInvalid() {
        MockServerHttpRequest request =
                MockServerHttpRequest.get("/api/data")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        Mono<Void> result = jwtFilter.filter(exchange, filterChain);

        StepVerifier.create(result)
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String generateToken(String username) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(username)
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();
    }
    public static void main(String args[]) {
        System.out.println(new JwtFilterTest().generateToken("admin"));
    }
}
