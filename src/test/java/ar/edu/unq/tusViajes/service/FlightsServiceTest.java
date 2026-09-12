package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.FlightsApiClient;
import ar.edu.unq.tusViajes.adapters.dto.CityDTO;
import ar.edu.unq.tusViajes.adapters.dto.CountryDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightFilterDTO;
import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightsServiceTest {

    @Mock
    private FlightsApiClient flightsApiClient;

    @InjectMocks
    private FlightsService flightsService;

    @Test
    void searchFlights_callsClientAndReturnsFlights() {
        CountryDTO country = new CountryDTO("AR", "Argentina");
        CityDTO origin = new CityDTO(1L, "Buenos Aires", country);
        CityDTO destination = new CityDTO(2L, "Madrid", new CountryDTO("ES", "España"));
        FlightDTO flight = new FlightDTO(1L, "Aerolíneas Argentinas", origin, destination, "2026-10-15T08:00", "2026-10-15T21:00");

        when(flightsApiClient.searchFlights()).thenReturn(List.of(flight));

        List<FlightDTO> result = flightsService.searchFlights();

        assertThat(result).containsExactly(flight);
        verify(flightsApiClient).searchFlights();
    }

    @Test
    void searchFlightsWithFilter_delegatesToClient() {
        FlightFilterDTO filter = FlightFilterDTO.empty();
        when(flightsApiClient.searchFlights(filter, 0, 10)).thenReturn(List.of());

        List<FlightDTO> result = flightsService.searchFlights(filter, 0, 10);

        assertThat(result).isEmpty();
        verify(flightsApiClient).searchFlights(filter, 0, 10);
    }

    @Test
    void sellFlight_delegatesToClient() {
        PassengerDTO passenger = new PassengerDTO(12345678, "Lionel", "Messi");
        FlightDTO flight = new FlightDTO(1L, "Aerolíneas Argentinas", null, null, "2026-10-15T08:00", "2026-10-15T21:00");

        when(flightsApiClient.sellFlight(1L, passenger)).thenReturn(flight);

        FlightDTO result = flightsService.sellFlight(1L, passenger);

        assertThat(result).isEqualTo(flight);
        verify(flightsApiClient).sellFlight(1L, passenger);
    }
}
