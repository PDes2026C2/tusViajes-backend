package ar.edu.unq.tusViajes.model;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.PaqueteBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PaqueteTest {

    @Test
    void crearPaquete_asignaTodosLosCamposCorrectamente() {
        Hotel hotel = HotelBuilder.aHotel().withNombre("Hotel Alvear").build();
        Agencia agencia = AgenciaBuilder.anAgencia().withRazonSocial("Viajes SA").build();

        LocalDateTime inicio = LocalDateTime.now().plusDays(5);
        LocalDateTime fin = LocalDateTime.now().plusDays(12);

        Paquete paquete = PaqueteBuilder.aPaquete()
                .withNombre("Cataratas Premium")
                .withDescripcion("Vuelos + Hotel 5 estrellas")
                .withPrecio(250000.0)
                .withFechaInicio(inicio)
                .withFechaFin(fin)
                .withHotel(hotel)
                .withAgencia(agencia)
                .build();

        assertThat(paquete.getNombre()).isEqualTo("Cataratas Premium");
        assertThat(paquete.getDescripcion()).isEqualTo("Vuelos + Hotel 5 estrellas");
        assertThat(paquete.getPrecio()).isEqualTo(250000.0);
        assertThat(paquete.getFechaInicio()).isEqualTo(inicio);
        assertThat(paquete.getFechaFin()).isEqualTo(fin);
        assertThat(paquete.getHotel()).isEqualTo(hotel);
        assertThat(paquete.getAgencia()).isEqualTo(agencia);
    }

    @Test
    void actualizarDatos_modificaValoresDelPaquete() {
        Paquete paquete = PaqueteBuilder.aPaquete().build();

        Hotel nuevoHotel = HotelBuilder.aHotel().withNombre("Nuevo Hotel").build();
        Agencia nuevaAgencia = AgenciaBuilder.anAgencia().withRazonSocial("Nueva Agencia").build();
        LocalDateTime nuevoInicio = LocalDateTime.now().plusDays(20);
        LocalDateTime nuevoFin = LocalDateTime.now().plusDays(27);

        paquete.actualizarDatos("Nuevo Nombre", "Nueva Desc", 300000.0, nuevoInicio, nuevoFin, nuevoHotel, nuevaAgencia);

        assertThat(paquete.getNombre()).isEqualTo("Nuevo Nombre");
        assertThat(paquete.getDescripcion()).isEqualTo("Nueva Desc");
        assertThat(paquete.getPrecio()).isEqualTo(300000.0);
        assertThat(paquete.getFechaInicio()).isEqualTo(nuevoInicio);
        assertThat(paquete.getFechaFin()).isEqualTo(nuevoFin);
        assertThat(paquete.getHotel()).isEqualTo(nuevoHotel);
        assertThat(paquete.getAgencia()).isEqualTo(nuevaAgencia);
    }
}

