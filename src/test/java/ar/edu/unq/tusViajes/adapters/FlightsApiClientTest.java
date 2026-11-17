package ar.edu.unq.tusViajes.adapters;

import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.adapters.dto.PageResponseDTO;
import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FlightsApiClientTest {

    private FlightsApiClient flightsApiClient;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        flightsApiClient = new FlightsApiClient(builder, "http://localhost:8081");
    }

    @Test
    void searchFlightsReturnsListOfFlights() {
        String jsonResponse = """
                {
                    "content": [
                        {
                            "id": 1,
                            "airline": "Aerolíneas Argentinas",
                            "originCity": {
                                "id": 10,
                                "name": "Buenos Aires",
                                "country": {
                                    "isoCode": "AR",
                                    "name": "Argentina"
                                }
                            },
                            "destinationCity": {
                                "id": 20,
                                "name": "Madrid",
                                "country": {
                                    "isoCode": "ES",
                                    "name": "España"
                                }
                            },
                            "departureDate": "2026-10-01T10:00:00",
                            "arrivalDate": "2026-10-01T22:00:00"
                        }
                    ],
                    "totalElements": 1,
                    "totalPages": 1,
                    "size": 1,
                    "number": 0
                }
                """;

        server.expect(requestTo("http://localhost:8081/flights"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        PageResponseDTO<FlightDTO> flights = flightsApiClient.searchFlights();

        server.verify();
        assertThat(flights.content()).hasSize(1);
        FlightDTO flight = flights.content().getFirst();
        assertThat(flight.id()).isEqualTo(1L);
        assertThat(flight.airline()).isEqualTo("Aerolíneas Argentinas");
        assertThat(flight.originCity().name()).isEqualTo("Buenos Aires");
        assertThat(flight.destinationCity().name()).isEqualTo("Madrid");
    }

    @Test
    void sellFlightReturnsUpdatedFlight() {
        String jsonResponse = """
                {
                    "id": 1,
                    "airline": "Aerolíneas Argentinas",
                    "originCity": {
                        "id": 10,
                        "name": "Buenos Aires",
                        "country": {
                            "isoCode": "AR",
                            "name": "Argentina"
                        }
                    },
                    "destinationCity": {
                        "id": 20,
                        "name": "Madrid",
                        "country": {
                            "isoCode": "ES",
                            "name": "España"
                        }
                    },
                    "departureDate": "2026-10-01T10:00:00",
                    "arrivalDate": "2026-10-01T22:00:00"
                }
                """;

        server.expect(requestTo("http://localhost:8081/flights/1/sell"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        PassengerDTO passenger = new PassengerDTO(40123456, "Lionel", "Messi");
        FlightDTO flight = flightsApiClient.sellFlight(1L, passenger);

        server.verify();
        assertThat(flight).isNotNull();
        assertThat(flight.id()).isEqualTo(1L);
        assertThat(flight.airline()).isEqualTo("Aerolíneas Argentinas");
    }

    @Test
    void cancelFlightReturnsUpdatedFlight() {
        String jsonResponse = """
                {
                    "id": 1,
                    "airline": "Aerolíneas Argentinas",
                    "originCity": {
                        "id": 10,
                        "name": "Buenos Aires",
                        "country": {
                            "isoCode": "AR",
                            "name": "Argentina"
                        }
                    },
                    "destinationCity": {
                        "id": 20,
                        "name": "Madrid",
                        "country": {
                            "isoCode": "ES",
                            "name": "España"
                        }
                    },
                    "departureDate": "2026-10-01T10:00:00",
                    "arrivalDate": "2026-10-01T22:00:00"
                }
                """;

        server.expect(requestTo("http://localhost:8081/flights/1/cancel"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        PassengerDTO passenger = new PassengerDTO(40123456, "Lionel", "Messi");
        FlightDTO flight = flightsApiClient.cancelFlight(1L, passenger);

        server.verify();
        assertThat(flight).isNotNull();
        assertThat(flight.id()).isEqualTo(1L);
        assertThat(flight.airline()).isEqualTo("Aerolíneas Argentinas");
    }

    @Test
    void getFlightReturnsFlightById() {
        String jsonResponse = """
                {
                    "id": 1,
                    "airline": "Aerolíneas Argentinas",
                    "originCity": {
                        "id": 10,
                        "name": "Buenos Aires",
                        "country": {
                            "isoCode": "AR",
                            "name": "Argentina"
                        }
                    },
                    "destinationCity": {
                        "id": 20,
                        "name": "Madrid",
                        "country": {
                            "isoCode": "ES",
                            "name": "España"
                        }
                    },
                    "departureDate": "2026-10-01T10:00:00",
                    "arrivalDate": "2026-10-01T22:00:00"
                }
                """;

        server.expect(requestTo("http://localhost:8081/flights/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        FlightDTO flight = flightsApiClient.getFlight(1L);

        server.verify();
        assertThat(flight).isNotNull();
        assertThat(flight.id()).isEqualTo(1L);
        assertThat(flight.airline()).isEqualTo("Aerolíneas Argentinas");
        assertThat(flight.originCity().name()).isEqualTo("Buenos Aires");
        assertThat(flight.destinationCity().name()).isEqualTo("Madrid");
    }

    @Test
    void getFlightThrowsResourceNotFoundExceptionWhenUpstreamReturns404() {
        server.expect(requestTo("http://localhost:8081/flights/999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        assertThatThrownBy(() -> flightsApiClient.getFlight(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vuelo con id 999 no encontrado");

        server.verify();
    }

    @AfterEach
    void clearRequestContext() {
        MDC.remove("requestId");
    }

    @Test
    void requestsForwardCurrentRequestIdWithoutReusingPreviousContext() {
        String firstId = "a24a75dd-5d60-470a-8cd8-47f72cd1e792";
        String secondId = "fc870244-6909-4389-b913-f7d783da0476";
        String emptyPage = "{\"content\":[],\"totalElements\":0,\"totalPages\":0,\"size\":10,\"number\":0}";
        server.expect(requestTo("http://localhost:8081/flights"))
                .andExpect(header("X-Request-ID", firstId))
                .andRespond(withSuccess(emptyPage, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8081/flights/1"))
                .andExpect(header("X-Request-ID", secondId))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8081/flights"))
                .andExpect(headerDoesNotExist("X-Request-ID"))
                .andRespond(withSuccess(emptyPage, MediaType.APPLICATION_JSON));

        MDC.put("requestId", firstId);
        flightsApiClient.searchFlights();
        MDC.put("requestId", secondId);
        flightsApiClient.getFlight(1L);
        MDC.remove("requestId");
        flightsApiClient.searchFlights();

        server.verify();
    }
}
