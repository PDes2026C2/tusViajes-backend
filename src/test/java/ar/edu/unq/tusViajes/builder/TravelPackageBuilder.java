package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;

import java.time.LocalDateTime;

public class TravelPackageBuilder {

    private static final Country DEFAULT_COUNTRY = new Country("AR", "Argentina");
    private City originCity = new City(1L, "Buenos Aires", DEFAULT_COUNTRY);
    private City destinationCity = new City(2L, "Bariloche", DEFAULT_COUNTRY);

    private Long id;
    private String name = "Escapada Bariloche";
    private String description = "Includes round trip flight and 7 days stay";
    private Double price = 150000.0;
    private LocalDateTime startDate = LocalDateTime.now().plusDays(10);
    private LocalDateTime endDate = LocalDateTime.now().plusDays(17);
    private Hotel hotel = HotelBuilder.aHotel().build();
    private Agency agency = AgencyBuilder.anAgency().build();
    private Flight departureFlight = FlightBuilder.aFlight()
            .withId(101L)
            .withOriginCity(originCity)
            .withDestinationCity(destinationCity)
            .withDepartureDate(startDate)
            .withArrivalDate(startDate.plusHours(2))
            .build();
    private Flight returnFlight = FlightBuilder.aFlight()
            .withId(102L)
            .withOriginCity(destinationCity)
            .withDestinationCity(originCity)
            .withDepartureDate(endDate.minusHours(2))
            .withArrivalDate(endDate)
            .build();

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

    public TravelPackageBuilder withDepartureFlight(Flight departureFlight) {
        this.departureFlight = departureFlight;
        return this;
    }

    public TravelPackageBuilder withReturnFlight(Flight returnFlight) {
        this.returnFlight = returnFlight;
        return this;
    }

    public TravelPackageBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public TravelPackage build() {
        TravelPackage tp = new TravelPackage(name, description, price, startDate, endDate, hotel, agency, departureFlight, returnFlight);
        if (id != null) {
            org.springframework.test.util.ReflectionTestUtils.setField(tp, "id", id);
        }
        return tp;
    }
}
