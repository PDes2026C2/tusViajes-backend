package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Country;

public class CountryBuilder {
    private String isoCode = "AR";
    private String name = "Default Country";

    public static CountryBuilder aCountry() {
        return new CountryBuilder();
    }

    public CountryBuilder withIsoCode(String isoCode) {
        this.isoCode = isoCode;
        return this;
    }

    public CountryBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public Country build() {
        return new Country(isoCode, name);
    }
}
