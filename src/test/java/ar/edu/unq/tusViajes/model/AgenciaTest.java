package ar.edu.unq.tusViajes.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AgenciaTest {

    @Test
    void constructor_asigna_todos_los_campos() {
        Agencia agencia = new Agencia("Huryn", "20-44576859-8");

        assertThat(agencia.getRazonSocial()).isEqualTo("Huryn");
        assertThat(agencia.getCuit()).isEqualTo("20-44576859-8");
        assertThat(agencia.getEstado()).isEqualTo(EstadoAgencia.PENDIENTE);
        assertThat(agencia.getRol()).isEqualTo(Rol.AGENCIA);
        assertThat(agencia.isAutorizada()).isFalse();
        assertThat(agencia.isActivo()).isFalse();
    }

    @Test
    void solo_cambia_razon_social() {
        Agencia agencia = new Agencia("Huryn", "20-44576859-8");

        agencia.actualizarRazonSocial("Metal");

        assertThat(agencia.getRazonSocial()).isEqualTo("Metal");
    }

    @Test
    void autorizar_cambiaEstadoAAutorizadaYActivaUsuario() {
        Agencia agencia = new Agencia("turismo@test.com", "hash123", "Turismo Express", "30-12345678-9");
        assertThat(agencia.isActivo()).isFalse();

        agencia.autorizar();

        assertThat(agencia.getEstado()).isEqualTo(EstadoAgencia.AUTORIZADA);
        assertThat(agencia.isAutorizada()).isTrue();
        assertThat(agencia.isActivo()).isTrue();
    }

    @Test
    void rechazar_cambiaEstadoARechazadaYUsuarioNoActivo() {
        Agencia agencia = new Agencia("turismo@test.com", "hash123", "Turismo Express", "30-12345678-9");

        agencia.rechazar();

        assertThat(agencia.getEstado()).isEqualTo(EstadoAgencia.RECHAZADA);
        assertThat(agencia.isAutorizada()).isFalse();
        assertThat(agencia.isActivo()).isFalse();
    }
}
