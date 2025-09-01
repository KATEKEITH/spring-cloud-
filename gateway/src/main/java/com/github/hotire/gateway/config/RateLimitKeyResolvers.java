package com.github.hotire.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimitKeyResolvers {

    /**
     * 셀러별 레이트리밋: 요청 헤더 X-Seller-Id 값 사용
     * Redis Cluster 사용 시 슬롯 고정을 위해 {…} 해시태그 사용
     */
    @Bean
    public KeyResolver sellerKeyResolver() {
        return exchange ->
            Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("X-Seller-Id"))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .map(id -> "seller:{" + id + "}");
    }
    
}
