package ar.edu.unq.tusViajes.adapters.dto;

import ar.edu.unq.tusViajes.model.Country;

public record CountryDTO(
        String isoCode,
        String name
) {
    public static CountryDTO from(Country country) {
        if (country == null) {
            return null;
        }
        return new CountryDTO(
                country.getIsoCode(),
                country.getName()
        );
    }
}
