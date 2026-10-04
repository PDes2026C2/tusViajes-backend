package ar.edu.unq.tusViajes.model;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.FlightBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.exception.UnauthorizedAgencyException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TravelPackageTest {

    @Test
    void createTravelPackage_assignsAllFieldsCorrectly() {
        Hotel hotel = HotelBuilder.aHotel().withName("Hotel Alvear").build();
        Agency agency = AgencyBuilder.anAgency().withBusinessName("Viajes SA").build();
        Flight departureFlight = FlightBuilder.aFlight().withId(1L).withAirline("Aerolíneas").build();
        Flight returnFlight = FlightBuilder.aReturnFlight().withId(2L).withAirline("Flybondi").build();

        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(12);

        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withName("Cataratas Premium")
                .withDescription("Flights + 5-star Hotel")
                .withPrice(250000.0)
                .withStartDate(start)
                .withEndDate(end)
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(departureFlight)
                .withReturnFlight(returnFlight)
                .build();

        assertThat(travelPackage.getName()).isEqualTo("Cataratas Premium");
        assertThat(travelPackage.getDescription()).isEqualTo("Flights + 5-star Hotel");
        assertThat(travelPackage.getPrice()).isEqualTo(250000.0);
        assertThat(travelPackage.getStartDate()).isEqualTo(start);
        assertThat(travelPackage.getEndDate()).isEqualTo(end);
        assertThat(travelPackage.getHotel()).isEqualTo(hotel);
        assertThat(travelPackage.getAgency()).isEqualTo(agency);
        assertThat(travelPackage.getDepartureFlight()).isEqualTo(departureFlight);
        assertThat(travelPackage.getReturnFlight()).isEqualTo(returnFlight);
    }

    @Test
    void updateData_modifiesTravelPackageValues() {
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().build();

        Hotel newHotel = HotelBuilder.aHotel().withName("Nuevo Hotel").build();
        Agency newAgency = travelPackage.getAgency();
        Flight newDeparture = FlightBuilder.aFlight().withId(201L).withAirline("LATAM").build();
        Flight newReturn = FlightBuilder.aReturnFlight().withId(202L).withAirline("JetSMART").build();
        LocalDateTime newStart = LocalDateTime.now().plusDays(20);
        LocalDateTime newEnd = LocalDateTime.now().plusDays(27);

        travelPackage.updateData("New Name", "New Desc", 300000.0, newStart, newEnd, newHotel, newAgency, newDeparture, newReturn);

        assertThat(travelPackage.getName()).isEqualTo("New Name");
        assertThat(travelPackage.getDescription()).isEqualTo("New Desc");
        assertThat(travelPackage.getPrice()).isEqualTo(300000.0);
        assertThat(travelPackage.getStartDate()).isEqualTo(newStart);
        assertThat(travelPackage.getEndDate()).isEqualTo(newEnd);
        assertThat(travelPackage.getHotel()).isEqualTo(newHotel);
        assertThat(travelPackage.getAgency()).isEqualTo(newAgency);
        assertThat(travelPackage.getDepartureFlight()).isEqualTo(newDeparture);
        assertThat(travelPackage.getReturnFlight()).isEqualTo(newReturn);
    }

    @Test
    void createTravelPackage_isActiveByDefault() {
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().build();

        assertThat(travelPackage.isActive()).isTrue();
    }

    @Test
    void deactivate_setsActiveToFalse_whenAgencyIsOwner() {
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().build();

        travelPackage.deactivate(travelPackage.getAgency());

        assertThat(travelPackage.isActive()).isFalse();
    }

    @Test
    void deactivate_throwsException_whenAgencyIsNotOwner() {
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().build();
        Agency otherAgency = AgencyBuilder
                .anAgency()
                .withBusinessName("Other Agency")
                .withEmail("otheragency@email.com")
                .build();

        assertThatThrownBy(() -> travelPackage.deactivate(otherAgency)).isInstanceOf(UnauthorizedAgencyException.class);
    }

    @Test
    void activate_setsActiveToTrue_whenAgencyIsOwner() {
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().withActive(false).build();

        travelPackage.activate(travelPackage.getAgency());

        assertThat(travelPackage.isActive()).isTrue();
    }

    @Test
    void activate_throwsException_whenAgencyIsNotOwner() {
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().build();
        Agency otherAgency = AgencyBuilder
                .anAgency()
                .withBusinessName("Other Agency")
                .withEmail("otheragency@email.com")
                .build();

        assertThatThrownBy(() -> travelPackage.activate(otherAgency)).isInstanceOf(UnauthorizedAgencyException.class);
    }

    @Test
    void hasStarted_returnsTrue_whenStartDateIsInThePast() {
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withStartDate(LocalDateTime.now().minusDays(1))
                .build();

        assertThat(travelPackage.hasStarted()).isTrue();
    }

    @Test
    void hasStarted_returnsFalse_whenStartDateIsInTheFuture() {
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withStartDate(LocalDateTime.now().plusDays(2))
                .build();

        assertThat(travelPackage.hasStarted()).isFalse();
    }
}
