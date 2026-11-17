package ar.edu.unq.tusViajes.config;

import com.jayway.jsonpath.JsonPath;
import ar.edu.unq.tusViajes.adapters.FlightsApiClient;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.matchesPattern;
import java.util.Map;
import java.net.URI;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.micrometer.tracing.test.autoconfigure.AutoConfigureTracing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@AutoConfigureMetrics
@AutoConfigureTracing
@ExtendWith(OutputCaptureExtension.class)
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

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    private Tracer tracer;

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

    @Test
    void httpRequests_returnTheirIdAndStructuredLog(CapturedOutput output) throws Exception {
        String requestId = "09c409d2-cf9b-4a33-a5f5-388dcf4de99e";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + applicationPort + "/api/countries?privateValue=not-for-logs"))
                .header("X-Request-ID", requestId)
                .header("X-Private-Value", "private-header-value")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("X-Request-ID")).contains(requestId);
        assertThat(response.headers().firstValue("X-Trace-ID").orElseThrow()).matches("[0-9a-f]{32}");
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            Map<String, Object> log = requestLog(output, requestId);
            assertThat(log).containsEntry("httpMethod", "GET")
                    .containsEntry("httpRoute", "/api/countries").containsEntry("httpStatus", 200);
            assertThat(((Number) log.get("durationMs")).longValue()).isGreaterThanOrEqualTo(0);
            assertThat(JsonPath.<String>read(log, "$.service.name")).isEqualTo("tusViajes");
            assertThat(log.toString()).doesNotContain("privateValue", "not-for-logs", "private-header-value");
        });
    }

    @Test
    void httpRequests_generateDistinctIdsForMissingOrInvalidHeaders() throws Exception {
        Set<String> generated = new HashSet<>();
        for (String candidate : new String[]{null, null, "not-a-uuid", "1-1-1-1-1", "x".repeat(200)}) {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + applicationPort + "/api/countries"));
            if (candidate != null) {
                request.header("X-Request-ID", candidate);
            }

            HttpResponse<String> response = client.send(request.GET().build(), HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            String requestId = response.headers().firstValue("X-Request-ID").orElseThrow();
            assertThat(UUID.fromString(requestId).toString()).isEqualTo(requestId);
            assertThat(generated.add(requestId)).isTrue();
        }
    }

    @Test
    void errorResponses_keepTheirRequestId(CapturedOutput output) throws Exception {
        String requestId = "015e7bad-55b7-4ff7-81c5-ce36feffba9b";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + applicationPort + "/api/purchases"))
                .header("X-Request-ID", requestId).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue("X-Request-ID")).contains(requestId);
        assertThat(response.headers().firstValue("X-Trace-ID").orElseThrow()).matches("[0-9a-f]{32}");
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            Map<String, Object> log = requestLog(output, requestId);
            assertThat(log).containsEntry("httpStatus", 401);
            assertThat(JsonPath.<String>read(log, "$.log.level")).isEqualTo("WARN");
        });
    }

    @Test
    void concurrentRequests_keepTheirOwnIds(CapturedOutput output) throws Exception {
        List<String> requestIds = List.of("083541b8-ee65-41b9-903a-1c9ddb2bca50",
                "3c74e61f-d35e-41e3-86ae-ced9746a7ff1", "bfb8c617-e68f-4e49-805d-70b1aa7ff850");
        Map<String, String> traceIds = new HashMap<>();
        var responses = requestIds.stream().map(requestId -> client.sendAsync(
                HttpRequest.newBuilder(URI.create("http://localhost:" + applicationPort + "/api/countries"))
                        .header("X-Request-ID", requestId).GET().build(), HttpResponse.BodyHandlers.ofString())).toList();

        for (int index = 0; index < responses.size(); index++) {
            HttpResponse<String> response = responses.get(index).get(10, TimeUnit.SECONDS);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("X-Request-ID")).contains(requestIds.get(index));
            String traceId = response.headers().firstValue("X-Trace-ID").orElseThrow();
            assertThat(traceId).matches("[0-9a-f]{32}");
            traceIds.put(requestIds.get(index), traceId);
        }
        assertThat(new HashSet<>(traceIds.values())).hasSize(requestIds.size());
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            for (String requestId : requestIds) {
                assertThat(requestLog(output, requestId)).containsEntry("httpRoute", "/api/countries")
                        .containsEntry("httpStatus", 200).containsEntry("traceId", traceIds.get(requestId));
            }
        });
    }

    @Test
    void tracing_preservesIncomingTraceAndCorrelatesLogs(CapturedOutput output) throws Exception {
        String traceId = "4bf92f3577b34da6a3ce929d0e0e4736";
        String parentSpanId = "00f067aa0ba902b7";
        String requestId = "0c587ffe-2e27-4f38-825b-3def0dbf009d";
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + applicationPort + "/api/countries"))
                .header("traceparent", "00-" + traceId + "-" + parentSpanId + "-01")
                .header("X-Request-ID", requestId).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("X-Trace-ID")).contains(traceId);
        assertThat(response.headers().firstValue("X-Request-ID")).contains(requestId);
        assertThat(response.headers().firstValue("X-Trace-ID").orElseThrow()).matches("[0-9a-f]{32}");
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            Map<String, Object> log = requestLog(output, requestId);
            assertThat(log).containsEntry("traceId", traceId);
            assertThat((String) log.get("spanId")).matches("[0-9a-f]{16}").isNotEqualTo(parentSpanId);
        });
    }

    @Test
    void tracing_generatesIndependentIdsForMissingOrInvalidContext(CapturedOutput output) throws Exception {
        Set<String> traces = new HashSet<>();
        for (String candidate : new String[]{null, null, "not-a-traceparent"}) {
            String requestId = UUID.randomUUID().toString();
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + applicationPort + "/api/countries"))
                    .header("X-Request-ID", requestId);
            if (candidate != null) {
                request.header("traceparent", candidate);
            }

            HttpResponse<String> response = client.send(request.GET().build(), HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            String traceId = response.headers().firstValue("X-Trace-ID").orElseThrow();
            assertThat(traceId).matches("[0-9a-f]{32}").isNotEqualTo("0".repeat(32));
            assertThat(traces.add(traceId)).isTrue();
            await().atMost(Duration.ofSeconds(3)).untilAsserted(() ->
                    assertThat(requestLog(output, requestId)).containsEntry("traceId", traceId));
        }
    }

    @Test
    void flightsClient_propagatesW3cContextToTheExternalApi() {
        String traceId = "a3ce929d0e0e47364bf92f3577b34da6";
        RestClient.Builder builder = restClientBuilder.clone();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        FlightsApiClient flightsClient = new FlightsApiClient(builder, "http://flights.test");
        server.expect(requestTo("http://flights.test/flights"))
                .andExpect(header("traceparent", matchesPattern("00-" + traceId + "-[0-9a-f]{16}-01")))
                .andRespond(withSuccess("{\"content\":[],\"totalElements\":0,\"totalPages\":0,\"size\":10,\"number\":0}", MediaType.APPLICATION_JSON));
        Span parent = tracer.spanBuilder().setParent(tracer.traceContextBuilder()
                .traceId(traceId).spanId("00f067aa0ba902b7").sampled(true).build())
                .name("incoming-request").start();

        try (Tracer.SpanInScope scope = tracer.withSpan(parent)) {
            flightsClient.searchFlights();
        } finally {
            parent.end();
        }

        server.verify();
    }

    private Map<String, Object> requestLog(CapturedOutput output, String requestId) {
        return output.getOut().lines()
                .filter(line -> line.startsWith("{"))
                .map(line -> JsonPath.<Map<String, Object>>read(line, "$"))
                .filter(log -> requestId.equals(log.get("requestId"))
                        && "Solicitud HTTP finalizada".equals(log.get("message")))
                .findFirst().orElseThrow();
    }

    private HttpResponse<String> get(int port, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
