package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;

import java.time.LocalDateTime;

public class TravelPackageBuilder {

    private String name = "Escapada Bariloche";
    private String description = "Includes round trip flight and 7 days stay";
    private Double price = 150000.0;
    private LocalDateTime startDate = LocalDateTime.now().plusDays(10);
    private LocalDateTime endDate = LocalDateTime.now().plusDays(17);
    private Hotel hotel = HotelBuilder.aHotel().build();
    private Agency agency = AgencyBuilder.anAgency().build();

    public static TravelPackageBuilder aTravelPackage() {
        return new TravelPackageBuilder();
    }

    public TravelPackageBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TravelPackageBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public TravelPackageBuilder withPrice(Double price) {
        this.price = price;
        return this;
    }

    public TravelPackageBuilder withStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public TravelPackageBuilder withEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public TravelPackageBuilder withHotel(Hotel hotel) {
        this.hotel = hotel;
        return this;
    }

    public TravelPackageBuilder withAgency(Agency agency) {
        this.agency = agency;
        return this;
    }

    public TravelPackage build() {
        return new TravelPackage(name, description, price, startDate, endDate, hotel, agency);
    }
}
