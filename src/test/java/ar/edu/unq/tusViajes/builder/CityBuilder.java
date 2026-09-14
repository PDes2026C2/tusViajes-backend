package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;

import static ar.edu.unq.tusViajes.builder.CountryBuilder.aCountry;

public class CityBuilder {
    private Long id = null;
    private String name = "Default City";
    private Country country = aCountry().build();

    public static CityBuilder aCity() {
        return new CityBuilder();
    }

    public CityBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public CityBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CityBuilder withCountry(Country country) {
        this.country = country;
        return this;
    }

    public City build() {
        City city = new City(name, country);
        city.setId(id);
        return city;
    }
}
