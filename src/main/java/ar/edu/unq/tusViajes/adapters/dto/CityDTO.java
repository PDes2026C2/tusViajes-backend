package ar.edu.unq.tusViajes.adapters.dto;

public record CityDTO(
        Long id,
        String name,
        CountryDTO country
) {}
