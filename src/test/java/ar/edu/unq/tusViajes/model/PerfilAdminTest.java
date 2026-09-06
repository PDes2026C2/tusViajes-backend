package ar.edu.unq.tusViajes.model;

import ar.edu.unq.tusViajes.builder.PerfilAdminBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerfilAdminTest {

    @Test
    void crearPerfilAdmin_asignaDatosCorrectamente() {
        PerfilAdmin admin = PerfilAdminBuilder.aPerfilAdmin()
                .withNombre("Root")
                .withApellido("Admin")
                .withEmail("root@tusviajes.com")
                .build();

        assertThat(admin.getNombre()).isEqualTo("Root");
        assertThat(admin.getApellido()).isEqualTo("Admin");
        assertThat(admin.getEmail()).isEqualTo("root@tusviajes.com");
    }

    @Test
    void actualizarDatos_modificaNombreYApellido() {
        PerfilAdmin admin = PerfilAdminBuilder.aPerfilAdmin().build();

        admin.actualizarDatos("Super", "Usuario");

        assertThat(admin.getNombre()).isEqualTo("Super");
        assertThat(admin.getApellido()).isEqualTo("Usuario");
    }
}

