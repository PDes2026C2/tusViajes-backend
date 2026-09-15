package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.model.Flight;

import java.time.LocalDateTime;

public class FlightBuilder {

    private static final Country DEFAULT_COUNTRY = new Country("AR", "Argentina");
    private Long id = 1L;
    private String airline = "Aerolíneas Argentinas";
    private City originCity = new City(1L, "Buenos Aires", DEFAULT_COUNTRY);
    private City destinationCity = new City(2L, "Bariloche", DEFAULT_COUNTRY);
    private LocalDateTime departureDate = LocalDateTime.now().plusDays(10);
    private LocalDateTime arrivalDate = LocalDateTime.now().plusDays(10).plusHours(2);

    public static FlightBuilder aFlight() {
        return new FlightBuilder();
    }

    public FlightBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public FlightBuilder withAirline(String airline) {
        this.airline = airline;
        return this;
    }

    public FlightBuilder withOriginCity(City originCity) {
        this.originCity = originCity;
        return this;
    }

    public FlightBuilder withDestinationCity(City destinationCity) {
        this.destinationCity = destinationCity;
        return this;
    }

    public FlightBuilder withDepartureDate(LocalDateTime departureDate) {
        this.departureDate = departureDate;
        return this;
    }

    public FlightBuilder withArrivalDate(LocalDateTime arrivalDate) {
        this.arrivalDate = arrivalDate;
        return this;
    }

    public Flight build() {
        return new Flight(id, airline, originCity, destinationCity, departureDate, arrivalDate);
    }
}
