package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.adapters.dto.CityDTO;
import ar.edu.unq.tusViajes.model.Flight;

import java.time.LocalDateTime;

public record FlightResponseDTO(
        Long id,
        String airline,
        CityDTO originCity,
        CityDTO destinationCity,
        LocalDateTime departureDate,
        LocalDateTime arrivalDate
) {
    public static FlightResponseDTO from(Flight flight) {
        if (flight == null) {
            return null;
        }
        return new FlightResponseDTO(
                flight.getId(),
                flight.getAirline(),
                CityDTO.from(flight.getOriginCity()),
                CityDTO.from(flight.getDestinationCity()),
                flight.getDepartureDate(),
                flight.getArrivalDate()
        );
    }
}
