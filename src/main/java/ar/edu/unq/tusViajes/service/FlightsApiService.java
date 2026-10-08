package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.FlightsApiClient;
import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightFilterDTO;
import ar.edu.unq.tusViajes.adapters.dto.PageResponseDTO;
import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FlightsApiService {

    private final FlightsApiClient flightsApiClient;

    public PageResponseDTO<FlightDTO> searchFlights() {
        return flightsApiClient.searchFlights();
    }

    public PageResponseDTO<FlightDTO> searchFlights(FlightFilterDTO filter, Integer page, Integer size) {
        return flightsApiClient.searchFlights(filter, page, size);
    }

    public FlightDTO sellFlight(Long flightId, PassengerDTO passenger) {
        return flightsApiClient.sellFlight(flightId, passenger);
    }

    public FlightDTO cancelFlight(Long flightId, PassengerDTO passenger) {
        return flightsApiClient.cancelFlight(flightId, passenger);
    }

    public FlightDTO getFlight(Long flightId) {
        return flightsApiClient.getFlight(flightId);
    }
}
