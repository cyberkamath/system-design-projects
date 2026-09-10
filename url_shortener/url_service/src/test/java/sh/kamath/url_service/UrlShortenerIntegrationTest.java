package sh.kamath.url_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
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

    @Autowired
    private RestTestClient restClient; // The new modern standard in Spring Boot 4

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
}
