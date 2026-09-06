package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.AgenciaRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AgenciaResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
@Transactional 
public class AgenciaServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    
    @Autowired
    private AgenciaService agenciaService;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Test
    void listar_retornaTodasLasAgenciasDeLaBaseDeDatos() {
        
        Agencia guardada = agenciaRepository.save(AgenciaBuilder.anAgencia().build());

        List<AgenciaResponseDTO> resultado = agenciaService.listar();

        assertThat(resultado).isNotEmpty();
        assertEquals(resultado.get(0).razonSocial(),guardada.getRazonSocial());
    }

    @Test
    void buscarPorId_devuelveLaAgenciaCuandoExisteEnPostgres() {
        Agencia guardada = agenciaRepository.save(
                AgenciaBuilder.anAgencia().withRazonSocial("Huryn").withCuit("20-44576859-8").build()
        );

        AgenciaResponseDTO resultado = agenciaService.buscarPorId(guardada.getId());

        assertThat(resultado.razonSocial()).isEqualTo("Huryn");
        assertThat(resultado.cuit()).isEqualTo("20-44576859-8");
    }

    @Test
    void buscarPorId_lanzaExcepcionCuandoNoExisteEnPostgres() {
        assertThatThrownBy(() -> agenciaService.buscarPorId(99999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crear_guardaYDevuelveLaAgenciaPersistida() {
        AgenciaRequestDTO dto = new AgenciaRequestDTO("Viajes del Norte", "30-99887766-1");

        AgenciaResponseDTO resultado = agenciaService.crear(dto);

        assertThat(resultado.id()).isNotNull();
        assertThat(resultado.razonSocial()).isEqualTo("Viajes del Norte");

        
        assertThat(agenciaRepository.existsById(resultado.id())).isTrue();
    }

    @Test
    void crear_lanzaRecursoDuplicadoExceptionCuandoElCuitYaExisteEnPostgres() {
        agenciaRepository.save(AgenciaBuilder.anAgencia().withCuit("30-12345678-9").build());

        AgenciaRequestDTO dtoDuplicado = new AgenciaRequestDTO("Otra Agencia", "30-12345678-9");

        assertThatThrownBy(() -> agenciaService.crear(dtoDuplicado))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("30-12345678-9");
    }
}