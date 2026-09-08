package ar.edu.unq.tusViajes.model;

import ar.edu.unq.tusViajes.builder.PaqueteBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompradorTest {

    @Test
    void crearComprador_asignaDatosBasicosYEspecificos() {
        Comprador comprador = new Comprador("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");

        assertThat(comprador.getNombre()).isEqualTo("Maria");
        assertThat(comprador.getApellido()).isEqualTo("Lopez");
        assertThat(comprador.getEmail()).isEqualTo("maria@example.com");
        assertThat(comprador.getTelefono()).isEqualTo("11-9999-8888");
        assertThat(comprador.getDni()).isEqualTo("40123456");
        assertThat(comprador.getRol()).isEqualTo(Rol.COMPRADOR);
        assertThat(comprador.isActivo()).isTrue();
    }

    @Test
    void agregarFavorito_añadeElPaqueteAEstaColeccion() {
        Comprador comprador = new Comprador("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        Paquete paquete = PaqueteBuilder.aPaquete().withNombre("Viaje a Mendoza").build();

        comprador.agregarFavorito(paquete);

        assertThat(comprador.getPaquetesFavoritos()).hasSize(1);
        assertThat(comprador.getPaquetesFavoritos()).contains(paquete);
    }

    @Test
    void agregarFavorito_noAgregaDuplicadosSiSePasaElMismoPaquete() {
        Comprador comprador = new Comprador("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        Paquete paquete = PaqueteBuilder.aPaquete().build();

        comprador.agregarFavorito(paquete);
        comprador.agregarFavorito(paquete);

        assertThat(comprador.getPaquetesFavoritos()).hasSize(1);
    }

    @Test
    void quitarFavorito_remueveElPaqueteDeLaColeccion() {
        Comprador comprador = new Comprador("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        Paquete paquete = PaqueteBuilder.aPaquete().build();
        comprador.agregarFavorito(paquete);

        comprador.quitarFavorito(paquete);

        assertThat(comprador.getPaquetesFavoritos()).isEmpty();
    }
}
