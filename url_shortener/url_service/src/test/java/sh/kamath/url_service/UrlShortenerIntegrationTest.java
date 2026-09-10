package sh.kamath.url_service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import sh.kamath.url_service.dto.ShortenResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Testcontainers
class UrlShortenerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection(name="redis")
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);


    @Autowired
    private RestTestClient restClient; // The new modern standard in Spring Boot 4

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clearCache(){
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    @Test
    void shortenReturnsCreatedWithCode() {
        Map<String, String> body = Map.of("url", "https://example.com/test");

        restClient.post()
                .uri("/shorten")
                .body(body)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ShortenResponse.class)
                .value(response -> {
                    assertThat(response.code()).isNotBlank();
                    assertThat(response.shortUrl()).isNotBlank();
                });
    }

    @Test
    void resolveReturnsFoundWithLocationHeader() {
        Map<String, String> body = Map.of("url", "https://example.com/target");

        ShortenResponse shortened = restClient.post()
                        .uri("/shorten")
                        .body(body)
                        .exchange()
                        .expectStatus().isCreated()
                        .expectBody(ShortenResponse.class)
                        .returnResult()
                        .getResponseBody();

        restClient.get()
                .uri("/{code}",shortened.code())
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location","https://example.com/target");
    }

    @Test
    void resolveReturnsNotFoundForUnknownCode() {
        restClient.get()
                .uri("/{code}", "does-not-exist-xyz")
                .exchange()
                .expectStatus().isNotFound();
    }

    // Redis Specific tests

    @Test
    void resolveCachesResultInRedis() {
        Map<String, String> body = Map.of("url", "https://example.com/cached");

        ShortenResponse shortened = restClient.post()
                .uri("/shorten").body(body).exchange()
                .expectStatus().isCreated()
                .expectBody(ShortenResponse.class)
                .returnResult().getResponseBody();

        // Before resolve — cache should be empty for this code
        String cacheKey = "url:" + shortened.code();
        assertThat(redisTemplate.opsForValue().get(cacheKey)).isNull();

        // Resolve
        restClient.get()
                .uri("/{code}", shortened.code())
                .exchange()
                .expectStatus().isFound();

        // After resolve — cache should have the URL
        String cached = redisTemplate.opsForValue().get(cacheKey);
        assertThat(cached).isEqualTo("https://example.com/cached");
    }

    @Test
    void resolveReturnsCachedValueWithoutHittingDb() {
        // Manually put a value in the cache that isn't in the DB
        String fakeCode = "fake123";
        String cachedUrl = "https://example.com/from-cache-only";
        redisTemplate.opsForValue().set("url:" + fakeCode, cachedUrl);

        // Resolve — should return cached value even though DB has no such code
        restClient.get()
                .uri("/{code}", fakeCode)
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", cachedUrl);
    }
}
