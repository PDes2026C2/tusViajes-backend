package ar.edu.unq.tusViajes.model;

import static org.assertj.core.api.Assertions.assertThat;
 
import org.junit.jupiter.api.Test;
 
class HotelTest {
 
    @Test
    void constructor_asignaTodosLosCampos() {
        Hotel hotel = new Hotel("Hotel Central", "Av. Bustillo 123, Bariloche",
         "http://ejemplo.com/foto.jpg", "Desayuno");
 
        assertThat(hotel.getNombre()).isEqualTo("Hotel Central");
        assertThat(hotel.getDestino()).isEqualTo("Av. Bustillo 123, Bariloche");
        assertThat(hotel.getFotoUrl()).isEqualTo("http://ejemplo.com/foto.jpg");
        assertThat(hotel.getServicio()).isEqualTo("Desayuno");
    }
 
    @Test
    void constructor_noAsignaIdTodavia() {
        Hotel hotel = new Hotel("Hotel Central", "Bariloche", null, null);
 
        assertThat(hotel.getId()).isNull();
    }
 
}