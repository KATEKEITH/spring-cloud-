# SCG (Spring Cloud Gateway)


**작성자**: Haytham Mohamed
**발행일**: 2021년 4월 5일

## 1. Rate Limiting의 필요성

* API와 서비스 엔드포인트를 **서비스 거부 공격(DoS)**, **자원 남용**, **연쇄 실패**로부터 보호하는 것은 중요한 아키텍처적 과제다.
* **Rate Limiting**은 클라이언트가 API를 호출할 수 있는 비율을 제어하는 기법이다.
* 분산 시스템에서는 **중앙집중식으로 API 소비 속도를 제어·관리**하는 방식이 가장 효과적이다.
* 설정된 요청 비율을 초과하면 요청은 **HTTP 429 (Too Many Requests)** 오류를 반환한다.

---

## 2. Spring Cloud Gateway(SCG) 소개

* SCG는 **경량화된 API Gateway**이지만 API 소비 속도 제어에 효과적이다.
* SCG를 사용하려면 `org.springframework.cloud:spring-cloud-starter-gateway` 의존성을 추가하고, **YAML 설정**을 통해 라우팅 및 필터를 지정하면 된다.
* 라우트(Route) 정의를 통해 **특정 경로/헤더 조건에 맞는 요청을 어떤 백엔드 서비스로 보낼지** 설정할 수 있다.

예시:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: route1
          uri: http://localhost:8081
          predicates:
            - Path=/backend
```

---

## 3. RequestRateLimiter 필터

* SCG가 제공하는 여러 **Gateway Filter** 중 하나가 `RequestRateLimiter`.
* 요청이 허용 가능한 한도를 초과했는지 확인하고, 초과 시 요청을 차단한다.
* **KeyResolver 인터페이스**를 통해 “누가 요청하는가”를 정의할 수 있다. (예: 사용자 Principal, API 키, IP 등)
* 기본적으로는 **사용자의 Principal name**을 키로 사용한다.
* `KeyResolver` 인터페이스 예시:

```java
public interface KeyResolver {
    Mono<String> resolve(ServerWebExchange exchange);
}
```

* 키가 없을 경우 요청을 거부하지만, `spring.cloud.gateway.filter.request-rate-limiter.deny-empty-key=false` 설정을 하면 허용 가능하다.
* 빈 키일 때 반환할 **HTTP 상태 코드**도 설정할 수 있다.

---

## 4. Redis 기반 Token Bucket 알고리즘

* **Redis RateLimiter 구현체**를 통해 Token Bucket 알고리즘을 적용 가능하다.
* `spring-boot-starter-data-redis` 의존성이 필요하다.
* Token Bucket 방식:

  * 일정 비율로 토큰이 버킷에 추가된다.
  * API 호출은 토큰을 소모하며, 토큰이 없으면 요청이 거부된다.
  * 한 요청이 여러 자원을 사용하는 경우, **requestedTokens** 값을 조정해 처리 가능.

예시 설정:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: route1
          uri: http://localhost:8081
          predicates:
            - Path=/backend
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 500
                redis-rate-limiter.burstCapacity: 1000
                redis-rate-limiter.requestedTokens: 1
```

* 위 설정은 초당 **500개의 요청**을 허용하며, **최대 1000개의 버스트 요청**까지 수용한다.
* 버스트가 발생하면 해당 초 동안은 허용하지만, 그 다음 초에는 요청이 제한될 수 있다.

---

## 5. 커스텀 RateLimiter와 KeyResolver

* SCG는 **사용자 정의 RateLimiter**와 **KeyResolver**를 지원한다.
* `RateLimiter` 인터페이스를 직접 구현하고, `@Bean`으로 등록 가능하다.
* 설정 시 SpEL을 이용해 지정 가능:

```java
@Bean
public KeyResolver customKeyResolver() {
    return exchange -> ...; // Mono<String> 반환
}
```

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: route1
          uri: http://localhost:8081
          predicates:
            - Path=/backend
          filters:
            - name: RequestRateLimiter
              args:
                rate-limiter: "#{customRateLimiter}"
                key-resolver: "#{customKeyResolver}"
```

---

✅ **정리**

* SCG는 간단한 설정만으로 API Rate Limiting을 손쉽게 구현할 수 있다.
* 기본 제공되는 Redis 기반 Token Bucket 알고리즘을 활용하면 **재충전 속도(replenishRate)**, **버스트 용량(burstCapacity)**, **요청당 토큰 소모량(requestedTokens)** 등을 유연하게 설정 가능하다.
* KeyResolver와 RateLimiter를 직접 구현해 서비스 요구사항에 맞춘 **세밀한 제어**도 할 수 있다.



### Reference
- https://cloud.spring.io/spring-cloud-gateway/reference/html/
- https://spring.io/blog/2021/04/05/api-rate-limiting-with-spring-cloud-gateway
- https://cheese10yun.github.io/spring-cloud-gateway/
