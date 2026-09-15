package ar.edu.unq.tusViajes.adapters.dto;

import ar.edu.unq.tusViajes.model.City;

public record CityDTO(
        Long id,
        String name,
        CountryDTO country
) {
    public static CityDTO from(City city) {
        if (city == null) {
            return null;
        }
        return new CityDTO(
                city.getId(),
                city.getName(),
                CountryDTO.from(city.getCountry())
        );
    }

    public static City to(CityDTO cityDTO) {
        if (cityDTO == null) {
            return null;
        }
        return new City(
                cityDTO.id(),
                cityDTO.name(),
                CountryDTO.to(cityDTO.country())
        );
    }
}
