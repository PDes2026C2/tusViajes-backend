package ar.edu.unq.tusViajes.adapters;

import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightFilterDTO;
import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class FlightsApiClient {

    private final RestClient restClient;

    @Autowired
    public FlightsApiClient(
            @Value("${flights.api.base-url:http://localhost:8081}") String baseUrl
    ) {
        this(RestClient.builder(), baseUrl);
    }

    public FlightsApiClient(
            RestClient.Builder restClientBuilder,
            String baseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public List<FlightDTO> searchFlights() {
        return searchFlights(null, null, null);
    }

    public List<FlightDTO> searchFlights(FlightFilterDTO filter, Integer page, Integer size) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/flights");
                    if (filter != null) {
                        if (filter.airline() != null) uriBuilder.queryParam("airline", filter.airline());
                        if (filter.departureDateFrom() != null) uriBuilder.queryParam("departureDateFrom", filter.departureDateFrom());
                        if (filter.departureDateTo() != null) uriBuilder.queryParam("departureDateTo", filter.departureDateTo());
                        if (filter.arrivalDateFrom() != null) uriBuilder.queryParam("arrivalDateFrom", filter.arrivalDateFrom());
                        if (filter.arrivalDateTo() != null) uriBuilder.queryParam("arrivalDateTo", filter.arrivalDateTo());
                        if (filter.originCityId() != null) uriBuilder.queryParam("originCityId", filter.originCityId());
                        if (filter.originCountryIsoCode() != null) uriBuilder.queryParam("originCountryIsoCode", filter.originCountryIsoCode());
                        if (filter.destinationCityId() != null) uriBuilder.queryParam("destinationCityId", filter.destinationCityId());
                        if (filter.destinationCountryIsoCode() != null) uriBuilder.queryParam("destinationCountryIsoCode", filter.destinationCountryIsoCode());
                    }
                    if (page != null) uriBuilder.queryParam("page", page);
                    if (size != null) uriBuilder.queryParam("size", size);
                    return uriBuilder.build();
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<FlightDTO>>() {});
    }

    public FlightDTO sellFlight(Long flightId, PassengerDTO passenger) {
        return restClient.post()
                .uri("/flights/{id}/sell", flightId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(passenger)
                .retrieve()
                .body(FlightDTO.class);
    }
}
