package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Hotel;

public class HotelBuilder {

    private String name = "Hotel Gran Central";
    private String destination = "Bariloche";
    private String photoUrl = "https://ejemplo.com/hotel.jpg";
    private String services = "Breakfast included, WiFi";

    public static HotelBuilder aHotel() {
        return new HotelBuilder();
    }

    public HotelBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public HotelBuilder withDestination(String destination) {
        this.destination = destination;
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
        return new Hotel(name, destination, photoUrl, services);
    }
}
