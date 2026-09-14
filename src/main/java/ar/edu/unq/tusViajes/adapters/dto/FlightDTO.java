package ar.edu.unq.tusViajes.adapters.dto;


import java.time.LocalDateTime;

public record FlightDTO(
        long id,
        String airline,
        CityDTO originCity,
        CityDTO destinationCity,
        LocalDateTime departureDate,
        LocalDateTime arrivalDate
) {}
