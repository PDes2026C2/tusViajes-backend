package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.FlightsApiClient;
import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightFilterDTO;
import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightsService {

    private final FlightsApiClient flightsApiClient;

    public List<FlightDTO> searchFlights() {
        return flightsApiClient.searchFlights();
    }

    public List<FlightDTO> searchFlights(FlightFilterDTO filter, Integer page, Integer size) {
        return flightsApiClient.searchFlights(filter, page, size);
    }

    public FlightDTO sellFlight(Long flightId, PassengerDTO passenger) {
        return flightsApiClient.sellFlight(flightId, passenger);
    }
}
