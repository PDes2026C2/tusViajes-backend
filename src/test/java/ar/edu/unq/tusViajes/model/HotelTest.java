package ar.edu.unq.tusViajes.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HotelTest {

    @Test
    void constructor_assignsAllFields() {
        City city = new City("Bariloche", new Country("AR", "Argentina"));
        Hotel hotel = new Hotel("Hotel Central", city,
                "http://example.com/photo.jpg", "Breakfast");

        assertThat(hotel.getName()).isEqualTo("Hotel Central");
        assertThat(hotel.getCity()).isEqualTo(city);
        assertThat(hotel.getCity().getName()).isEqualTo("Bariloche");
        assertThat(hotel.getPhotoUrl()).isEqualTo("http://example.com/photo.jpg");
        assertThat(hotel.getServices()).isEqualTo("Breakfast");
    }

    @Test
    void constructor_doesNotAssignIdYet() {
        City city = new City("Bariloche", new Country("AR", "Argentina"));
        Hotel hotel = new Hotel("Hotel Central", city, null, null);

        assertThat(hotel.getId()).isNull();
    }
}