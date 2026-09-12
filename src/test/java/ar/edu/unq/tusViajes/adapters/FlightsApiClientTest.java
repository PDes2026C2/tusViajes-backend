package ar.edu.unq.tusViajes.adapters;

import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
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
                [
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
                ]
                """;

        server.expect(requestTo("http://localhost:8081/flights"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        List<FlightDTO> flights = flightsApiClient.searchFlights();

        server.verify();
        assertThat(flights).hasSize(1);
        FlightDTO flight = flights.getFirst();
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
}
