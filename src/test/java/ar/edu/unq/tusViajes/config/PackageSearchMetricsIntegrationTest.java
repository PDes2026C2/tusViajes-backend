package ar.edu.unq.tusViajes.config;

import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.service.TravelPackageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@AutoConfigureMetrics
@TestPropertySource(locations = "file:src/main/resources/application.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"management.server.port=0", "tusviajes.metrics.monitored-city-ids=2"})
class PackageSearchMetricsIntegrationTest {

    private static final String SEARCHES = "tusviajes_package_searches_total";
    private static final String DESTINATIONS = "tusviajes_package_destination_searches_total";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Value("${local.server.port}")
    private int applicationPort;

    @Value("${local.management.port}")
    private int managementPort;

    @MockitoBean
    private TravelPackageService travelPackageService;

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    void firstPageWithPackages_countsEachSuccessfulRequest() throws Exception {
        when(travelPackageService.search(any(), any())).thenAnswer(call -> new PageImpl<>(
                List.of(TravelPackageBuilder.aTravelPackage().build()), call.getArgument(1), 1));
        String before = metrics();

        assertThat(search("").statusCode()).isEqualTo(200);
        String afterFirst = metrics();
        assertThat(search("?page=0").statusCode()).isEqualTo(200);

        assertIncrease(before, afterFirst, SEARCHES, Map.of("result", "found"), 1);
        assertIncrease(before, metrics(), SEARCHES, Map.of("result", "found"), 2);
    }

    @Test
    void emptyFirstPage_countsWithoutAttributingAnUnspecifiedDestination() throws Exception {
        returnEmptyPage();
        String before = metrics();

        HttpResponse<String> response = search("?originCountryIso=AR&originCityId=2");
        String after = metrics();

        assertThat(response.statusCode()).isEqualTo(200);
        assertIncrease(before, after, SEARCHES, Map.of("result", "empty"), 1);
        assertIncrease(before, after, SEARCHES, Map.of("result", "found"), 0);
        assertIncrease(before, after, DESTINATIONS, Map.of(), 0);
    }

    @Test
    void explicitCountryAndCity_countIndependentlyEvenWhenThereAreNoResults() throws Exception {
        returnEmptyPage();
        String before = metrics();

        // Córdoba (2) is defined in the flights catalog migration V2__init_cities.sql.
        assertThat(search("?destinationCountryIso=%20ar%20&destinationCityId=2").statusCode()).isEqualTo(200);
        String after = metrics();

        assertIncrease(before, after, SEARCHES, Map.of(), 1);
        assertIncrease(before, after, DESTINATIONS, Map.of("type", "country", "destination", "AR"), 1);
        assertIncrease(before, after, DESTINATIONS, Map.of("type", "city", "destination", "2"), 1);
        assertIncrease(before, after, DESTINATIONS, Map.of(), 2);
    }

    @Test
    void filteringJustByAnUnmonitoredCity_usesOtherWithoutInferringACountry() throws Exception {
        returnEmptyPage();
        String before = metrics();

        // Mendoza (4) is in the catalog but is not configured for monitoring.
        assertThat(search("?destinationCityId=4").statusCode()).isEqualTo(200);
        assertThat(search("?destinationCityId=9223372036854775807").statusCode()).isEqualTo(200);
        String after = metrics();

        assertIncrease(before, after, DESTINATIONS, Map.of("type", "city", "destination", "other"), 2);
        assertIncrease(before, after, DESTINATIONS, Map.of("type", "country"), 0);
        assertIncrease(before, after, DESTINATIONS, Map.of(), 2);
        assertThat(after).doesNotContain("destination=\"4\"", "destination=\"9223372036854775807\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ZZ", "Argentina", "%C3%9F"})
    void invalidDestinationValues_doNotCreateDestinationLabels(String country) throws Exception {
        returnEmptyPage();
        String before = metrics();

        assertThat(search("?destinationCountryIso=" + country + "&destinationCityId=-1").statusCode()).isEqualTo(200);
        String after = metrics();

        assertIncrease(before, after, SEARCHES, Map.of("result", "empty"), 1);
        assertIncrease(before, after, DESTINATIONS, Map.of(), 0);
        assertThat(after).doesNotContain("destination=\"ZZ\"", "destination=\"Argentina\"", "destination=\"-1\"");
    }

    @Test
    void subsequentPages_doNotChangeEitherCounter() throws Exception {
        returnEmptyPage();
        String before = metrics();

        assertThat(search("?page=1&destinationCountryIso=AR&destinationCityId=2").statusCode()).isEqualTo(200);
        String after = metrics();

        assertIncrease(before, after, SEARCHES, Map.of(), 0);
        assertIncrease(before, after, DESTINATIONS, Map.of(), 0);
    }

    @Test
    void failedSearch_doesNotChangeEitherCounter() throws Exception {
        when(travelPackageService.search(any(), any())).thenThrow(new IllegalStateException("Search failed"));
        String before = metrics();

        assertThat(search("?page=0&destinationCountryIso=AR&destinationCityId=2").statusCode()).isEqualTo(500);
        String after = metrics();

        assertIncrease(before, after, SEARCHES, Map.of(), 0);
        assertIncrease(before, after, DESTINATIONS, Map.of(), 0);
    }

    private void returnEmptyPage() {
        when(travelPackageService.search(any(), any())).thenAnswer(call -> Page.empty(call.<Pageable>getArgument(1)));
    }

    private HttpResponse<String> search(String query) throws Exception {
        return get(applicationPort, "/api/travel-packages" + query);
    }

    private String metrics() throws Exception {
        HttpResponse<String> response = get(managementPort, "/actuator/prometheus");
        assertThat(response.statusCode()).isEqualTo(200);
        return response.body();
    }

    private HttpResponse<String> get(int port, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10))
                .GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void assertIncrease(String before, String after, String metric, Map<String, String> labels, int increment) {
        assertThat(total(after, metric, labels) - total(before, metric, labels))
                .as("%s %s", metric, labels)
                .isEqualTo(increment);
    }

    private double total(String exposition, String metric, Map<String, String> labels) {
        return exposition.lines()
                .filter(line -> line.startsWith(metric + "{"))
                .filter(line -> labels.entrySet().stream().allMatch(label ->
                        line.contains(label.getKey() + "=\"" + label.getValue() + "\"")))
                .mapToDouble(line -> Double.parseDouble(line.substring(line.indexOf("} ") + 2).trim()))
                .sum();
    }
}
