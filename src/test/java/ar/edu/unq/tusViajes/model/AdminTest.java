package ar.edu.unq.tusViajes.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AdminTest {

    @Test
    void crearAdmin_asignaDatosCorrectamente() {
        Admin admin = new Admin("Root", "Admin", "root@tusviajes.com", "hash123");

        assertThat(admin.getNombre()).isEqualTo("Root");
        assertThat(admin.getApellido()).isEqualTo("Admin");
        assertThat(admin.getEmail()).isEqualTo("root@tusviajes.com");
        assertThat(admin.getRol()).isEqualTo(Rol.ADMIN);
        assertThat(admin.isActivo()).isTrue();
    }

    @Test
    void actualizarDatos_modificaNombreYApellido() {
        Admin admin = new Admin("Root", "Admin", "root@tusviajes.com", "hash123");

        admin.actualizarDatos("Super", "Usuario");

        assertThat(admin.getNombre()).isEqualTo("Super");
        assertThat(admin.getApellido()).isEqualTo("Usuario");
    }
}
