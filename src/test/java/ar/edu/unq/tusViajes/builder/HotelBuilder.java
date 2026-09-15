package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.model.Hotel;

public class HotelBuilder {

    private String name = "Hotel Gran Central";
    private City city = new City("Bariloche", new Country("AR", "Argentina"));
    private String photoUrl = "https://example.com/hotel.jpg";
    private String services = "Breakfast included, WiFi";

    public static HotelBuilder aHotel() {
        return new HotelBuilder();
    }

    public HotelBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public HotelBuilder withCity(City city) {
        this.city = city;
        return this;
    }

    public HotelBuilder withPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        return this;
    }

    public HotelBuilder withServices(String services) {
        this.services = services;
        return this;
    }

    public Hotel build() {
        return new Hotel(name, city, photoUrl, services);
    }
}
