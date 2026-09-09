package ar.edu.unq.tusViajes.adapters.dto;

public record FlightDTO(
        long id,
        String airline,
        CityDTO originCity,
        CityDTO destinationCity,
        String departureDate,
        String arrivalDate
) {}
