package ar.edu.unq.tusViajes.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@AutoConfigureMetrics
// application.properties de test reemplaza al principal; aquí se verifica la configuración de producción.
@TestPropertySource(locations = "file:src/main/resources/application.properties")
// Los puertos reales permiten verificar la separación entre los endpoints de negocio y los de gestión.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.server.port=0")
class ObservabilityIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Value("${local.server.port}")
    private int applicationPort;

    @Value("${local.management.port}")
    private int managementPort;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    void healthAndProbes_areAccessibleWithoutAuthenticationAndHideDetails() throws Exception {
        for (String path : new String[]{"/actuator/health", "/actuator/health/liveness", "/actuator/health/readiness"}) {
            HttpResponse<String> response = get(managementPort, path);

            assertThat(response.statusCode()).as(path).isEqualTo(200);
            assertThat(response.body()).contains("\"status\":\"UP\"").doesNotContain("components", "details");
        }
    }

    @Test
    void prometheus_exportsHttpHistogramsAndJvmMetrics() throws Exception {
        assertThat(get(applicationPort, "/api/countries").statusCode()).isEqualTo(200);

        HttpResponse<String> response = get(managementPort, "/actuator/prometheus");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("jvm_memory_used_bytes", "http_server_requests_seconds_bucket",
                "uri=\"/api/countries\"", "application=\"tusViajes\"");
    }

    @Test
    void prometheus_isNotExposedOnApplicationPort() throws Exception {
        assertThat(get(applicationPort, "/actuator/prometheus").statusCode()).isEqualTo(401);
    }

    @Test
    void otherManagementEndpoints_areNotPublic() throws Exception {
        assertThat(get(managementPort, "/actuator/env").statusCode()).isEqualTo(401);
    }

    @Test
    void businessEndpoints_requireAuthentication() throws Exception {
        assertThat(get(applicationPort, "/api/purchases").statusCode()).isEqualTo(401);
    }

    private HttpResponse<String> get(int port, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
