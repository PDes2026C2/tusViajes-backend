package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Hotel;

public class HotelBuilder {

    private String nombre = "Hotel Gran Central";
    private String destino = "Bariloche";
    private String fotoUrl = "https://ejemplo.com/hotel.jpg";
    private String servicio = "Desayuno incluido, WiFi";

    public static HotelBuilder aHotel() {
        return new HotelBuilder();
    }

    public HotelBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public HotelBuilder withDestino(String destino) {
        this.destino = destino;
        return this;
    }

    public HotelBuilder withFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
        return this;
    }

    public HotelBuilder withServicio(String servicio) {
        this.servicio = servicio;
        return this;
    }

    public Hotel build() {
        return new Hotel(nombre, destino, fotoUrl, servicio);
    }
}

