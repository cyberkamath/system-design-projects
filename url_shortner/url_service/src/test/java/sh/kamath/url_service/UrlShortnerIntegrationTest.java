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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Testcontainers
class UrlShortnerIntegrationTest {

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
                .expectStatus().isCreated() // Replaces: assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                .expectBody(Map.class)
                .value(responseMap -> {
                    // Keeps your precise AssertJ assertions on the parsed body Map
                    assertThat(responseMap).containsKey("code");
                    assertThat(responseMap).containsKey("shortUrl");
                });
    }
}