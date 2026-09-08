package ar.edu.unq.tusViajes.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HotelTest {

    @Test
    void constructor_assignsAllFields() {
        Hotel hotel = new Hotel("Hotel Central", "Av. Bustillo 123, Bariloche",
                "http://ejemplo.com/foto.jpg", "Breakfast");

        assertThat(hotel.getName()).isEqualTo("Hotel Central");
        assertThat(hotel.getDestination()).isEqualTo("Av. Bustillo 123, Bariloche");
        assertThat(hotel.getPhotoUrl()).isEqualTo("http://ejemplo.com/foto.jpg");
        assertThat(hotel.getServices()).isEqualTo("Breakfast");
    }

    @Test
    void constructor_doesNotAssignIdYet() {
        Hotel hotel = new Hotel("Hotel Central", "Bariloche", null, null);

        assertThat(hotel.getId()).isNull();
    }
}