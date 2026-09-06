package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.Paquete;

import java.time.LocalDateTime;

public class PaqueteBuilder {

    private String nombre = "Escapada Bariloche";
    private String descripcion = "Incluye vuelo ida/vuelta y estadia 7 dias";
    private Double precio = 150000.0;
    private LocalDateTime fechaInicio = LocalDateTime.now().plusDays(10);
    private LocalDateTime fechaFin = LocalDateTime.now().plusDays(17);
    private Hotel hotel = HotelBuilder.aHotel().build();
    private Agencia agencia = AgenciaBuilder.anAgencia().build();

    public static PaqueteBuilder aPaquete() {
        return new PaqueteBuilder();
    }

    public PaqueteBuilder withNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public PaqueteBuilder withDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public PaqueteBuilder withPrecio(Double precio) {
        this.precio = precio;
        return this;
    }

    public PaqueteBuilder withFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
        return this;
    }

    public PaqueteBuilder withFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
        return this;
    }

    public PaqueteBuilder withHotel(Hotel hotel) {
        this.hotel = hotel;
        return this;
    }

    public PaqueteBuilder withAgencia(Agencia agencia) {
        this.agencia = agencia;
        return this;
    }

    public Paquete build() {
        return new Paquete(nombre, descripcion, precio, fechaInicio, fechaFin, hotel, agencia);
    }
}

