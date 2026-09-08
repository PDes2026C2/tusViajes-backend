package ar.edu.unq.tusViajes.model;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TravelPackageTest {

    @Test
    void createTravelPackage_assignsAllFieldsCorrectly() {
        Hotel hotel = HotelBuilder.aHotel().withName("Hotel Alvear").build();
        Agency agency = AgencyBuilder.anAgency().withBusinessName("Viajes SA").build();

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
                .build();

        assertThat(travelPackage.getName()).isEqualTo("Cataratas Premium");
        assertThat(travelPackage.getDescription()).isEqualTo("Flights + 5-star Hotel");
        assertThat(travelPackage.getPrice()).isEqualTo(250000.0);
        assertThat(travelPackage.getStartDate()).isEqualTo(start);
        assertThat(travelPackage.getEndDate()).isEqualTo(end);
        assertThat(travelPackage.getHotel()).isEqualTo(hotel);
        assertThat(travelPackage.getAgency()).isEqualTo(agency);
    }

    @Test
    void updateData_modifiesTravelPackageValues() {
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().build();

        Hotel newHotel = HotelBuilder.aHotel().withName("Nuevo Hotel").build();
        Agency newAgency = AgencyBuilder.anAgency().withBusinessName("Nueva Agencia").build();
        LocalDateTime newStart = LocalDateTime.now().plusDays(20);
        LocalDateTime newEnd = LocalDateTime.now().plusDays(27);

        travelPackage.updateData("New Name", "New Desc", 300000.0, newStart, newEnd, newHotel, newAgency);

        assertThat(travelPackage.getName()).isEqualTo("New Name");
        assertThat(travelPackage.getDescription()).isEqualTo("New Desc");
        assertThat(travelPackage.getPrice()).isEqualTo(300000.0);
        assertThat(travelPackage.getStartDate()).isEqualTo(newStart);
        assertThat(travelPackage.getEndDate()).isEqualTo(newEnd);
        assertThat(travelPackage.getHotel()).isEqualTo(newHotel);
        assertThat(travelPackage.getAgency()).isEqualTo(newAgency);
    }
}
