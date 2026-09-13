package ar.edu.unq.tusViajes.adapters.dto;

import java.time.LocalDateTime;

public record FlightFilterDTO(
        String airline,
        LocalDateTime departureDateFrom,
        LocalDateTime departureDateTo,
        LocalDateTime arrivalDateFrom,
        LocalDateTime arrivalDateTo,
        Long originCityId,
        String originCountryIsoCode,
        Long destinationCityId,
        String destinationCountryIsoCode
) {
    public static FlightFilterDTO empty() {
        return new FlightFilterDTO(null, null, null, null, null, null, null, null, null);
    }
}
