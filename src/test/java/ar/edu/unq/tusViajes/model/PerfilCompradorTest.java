package ar.edu.unq.tusViajes.model;

import ar.edu.unq.tusViajes.builder.PaqueteBuilder;
import ar.edu.unq.tusViajes.builder.PerfilCompradorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerfilCompradorTest {

    @Test
    void crearPerfilComprador_asignaDatosBasicosYEspecificos() {
        PerfilComprador comprador = PerfilCompradorBuilder.aPerfilComprador()
                .withNombre("Maria")
                .withApellido("Lopez")
                .withEmail("maria@example.com")
                .withTelefono("11-9999-8888")
                .withDni("40123456")
                .build();

        assertThat(comprador.getNombre()).isEqualTo("Maria");
        assertThat(comprador.getApellido()).isEqualTo("Lopez");
        assertThat(comprador.getEmail()).isEqualTo("maria@example.com");
        assertThat(comprador.getTelefono()).isEqualTo("11-9999-8888");
        assertThat(comprador.getDni()).isEqualTo("40123456");
    }

    @Test
    void agregarFavorito_añadeElPaqueteAEstaColeccion() {
        
        PerfilComprador comprador = PerfilCompradorBuilder.aPerfilComprador()
                .build();
        Paquete paquete = PaqueteBuilder.aPaquete().withNombre("Viaje a Mendoza").build();

        comprador.agregarFavorito(paquete);

        assertThat(comprador.getPaquetesFavoritos()).hasSize(1);
        assertThat(comprador.getPaquetesFavoritos()).contains(paquete);
    }

    @Test
    void agregarFavorito_noAgregaDuplicadosSiSePasaElMismoPaquete() {
        
        PerfilComprador comprador = PerfilCompradorBuilder.aPerfilComprador()
                .build();
        Paquete paquete = PaqueteBuilder.aPaquete().build();

        
        comprador.agregarFavorito(paquete);
        comprador.agregarFavorito(paquete);

        
        assertThat(comprador.getPaquetesFavoritos()).hasSize(1);
    }

    @Test
    void quitarFavorito_remueveElPaqueteDeLaColeccion() {
       
        PerfilComprador comprador = PerfilCompradorBuilder.aPerfilComprador()
                .build();
        Paquete paquete = PaqueteBuilder.aPaquete().build();
        comprador.agregarFavorito(paquete);

        comprador.quitarFavorito(paquete);

        assertThat(comprador.getPaquetesFavoritos()).isEmpty();
    }

}

