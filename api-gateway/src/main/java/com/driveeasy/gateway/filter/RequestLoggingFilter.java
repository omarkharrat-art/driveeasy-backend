package com.driveeasy.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Custom global filter, applied to EVERY request going through the gateway:
 *  1. adds a unique X-Request-Id header (sent to the service, and returned to the caller);
 *  2. measures the processing time and returns it in X-Response-Time;
 *  3. logs one line per request: method, path, target service, status, duration.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String RESPONSE_TIME = "X-Response-Time";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long debut = System.currentTimeMillis();

        // Reuse the caller's id if it sent one, otherwise create one
        String requestId = exchange.getRequest().getHeaders().getFirst(REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        final String id = requestId;

        // "PRE" part: before the request is forwarded to the microservice
        ServerHttpRequest request = exchange.getRequest().mutate().header(REQUEST_ID, id).build();
        ServerWebExchange mutated = exchange.mutate().request(request).build();

        // Headers must be added before the response is committed
        mutated.getResponse().beforeCommit(() -> {
            mutated.getResponse().getHeaders().set(REQUEST_ID, id);
            mutated.getResponse().getHeaders().set(RESPONSE_TIME, (System.currentTimeMillis() - debut) + "ms");
            return Mono.empty();
        });

        // "POST" part: once the microservice has answered
        return chain.filter(mutated).then(Mono.fromRunnable(() -> {
            Route route = mutated.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            log.info("[{}] {} {} -> {} : {} en {} ms",
                    id,
                    request.getMethod(),
                    request.getURI().getPath(),
                    route != null ? route.getUri() : "aucune route",
                    mutated.getResponse().getStatusCode(),
                    System.currentTimeMillis() - debut);
        }));
    }

    /** Runs first, so the measured time covers the whole processing. */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
