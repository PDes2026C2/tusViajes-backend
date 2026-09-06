package ar.edu.unq.tusViajes.model;

import static org.assertj.core.api.Assertions.assertThat;
 
import org.junit.jupiter.api.Test;
 
class AgenciaTest {
 
    @Test
    void constructor_asigna_todos_los_campos() {
        Agencia agencia = new Agencia("Huryn", "20-44576859-8");
 
        assertThat(agencia.getRazonSocial()).isEqualTo("Huryn");
        assertThat(agencia.getCuit()).isEqualTo("20-44576859-8");
    }
 
    @Test
    void solo_cambia_razon_social() {
        Agencia agencia = new Agencia("Huryn", "20-44576859-8");
 
        agencia.actualizarRazonSocial("Metal");
 
        assertThat(agencia.getRazonSocial()).isEqualTo("Metal");
    }
}
