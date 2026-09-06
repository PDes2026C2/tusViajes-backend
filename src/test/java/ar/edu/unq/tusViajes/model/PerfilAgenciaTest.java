package ar.edu.unq.tusViajes.model;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.builder.PerfilAgenciaBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PerfilAgenciaTest {

    @Test
    void crearPerfilAgencia_vinculaConAgencia() {
        Agencia agencia = AgenciaBuilder.anAgencia().withRazonSocial("Despegar UNQ").build();

        PerfilAgencia perfil = PerfilAgenciaBuilder.aPerfilAgencia()
                .withNombre("Carlos")
                .withApellido("Perez")
                .withEmail("carlos@despegar.com")
                .withAgencia(agencia)
                .build();

        assertThat(perfil.getNombre()).isEqualTo("Carlos");
        assertThat(perfil.getApellido()).isEqualTo("Perez");
        assertThat(perfil.getEmail()).isEqualTo("carlos@despegar.com");
        assertThat(perfil.getAgencia()).isEqualTo(agencia);
    }

    @Test
    void actualizarDatos_modificaNombreYApellido() {
        PerfilAgencia perfil = PerfilAgenciaBuilder.aPerfilAgencia().build();

        perfil.actualizarDatos("Carlos Alberto", "Perez Gomez");

        assertThat(perfil.getNombre()).isEqualTo("Carlos Alberto");
        assertThat(perfil.getApellido()).isEqualTo("Perez Gomez");
    }
}

