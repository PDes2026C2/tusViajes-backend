package ar.edu.unq.tusViajes.model;

import ar.edu.unq.tusViajes.builder.FlightBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FlightTest {

    @Test
    void constructor_assignsAllFieldsCorrectly() {
        City origin = new City(1L, "Buenos Aires", new Country("AR", "Argentina"));
        City destination = new City(2L, "Bariloche", new Country("AR", "Argentina"));
        LocalDateTime departure = LocalDateTime.of(2026, 10, 15, 8, 0);
        LocalDateTime arrival = LocalDateTime.of(2026, 10, 15, 10, 30);

        Flight flight = new Flight(100L, "Aerolíneas Argentinas", origin, destination, departure, arrival);

        assertThat(flight.getId()).isEqualTo(100L);
        assertThat(flight.getAirline()).isEqualTo("Aerolíneas Argentinas");
        assertThat(flight.getOriginCity()).isEqualTo(origin);
        assertThat(flight.getDestinationCity()).isEqualTo(destination);
        assertThat(flight.getDepartureDate()).isEqualTo(departure);
        assertThat(flight.getArrivalDate()).isEqualTo(arrival);
    }

    @Test
    void setters_modifyFlightFields() {
        Flight flight = FlightBuilder.aFlight().build();

        City newOrigin = new City(3L, "Córdoba", new Country("AR", "Argentina"));
        City newDest = new City(4L, "Mendoza", new Country("AR", "Argentina"));
        LocalDateTime newDep = LocalDateTime.now().plusDays(2);
        LocalDateTime newArr = LocalDateTime.now().plusDays(2).plusHours(1);

        flight.setId(200L);
        flight.setAirline("Flybondi");
        flight.setOriginCity(newOrigin);
        flight.setDestinationCity(newDest);
        flight.setDepartureDate(newDep);
        flight.setArrivalDate(newArr);

        assertThat(flight.getId()).isEqualTo(200L);
        assertThat(flight.getAirline()).isEqualTo("Flybondi");
        assertThat(flight.getOriginCity()).isEqualTo(newOrigin);
        assertThat(flight.getDestinationCity()).isEqualTo(newDest);
        assertThat(flight.getDepartureDate()).isEqualTo(newDep);
        assertThat(flight.getArrivalDate()).isEqualTo(newArr);
    }
}
