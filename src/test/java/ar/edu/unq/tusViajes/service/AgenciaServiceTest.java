package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.CreateAgenciaRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AgenciaResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.EstadoAgencia;
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
        assertEquals(resultado.getFirst().razonSocial(), guardada.getRazonSocial());
    }

    @Test
    void buscarPorId_devuelveLaAgencia() {
        Agencia guardada = agenciaRepository.save(
                AgenciaBuilder.anAgencia().withRazonSocial("Huryn").withCuit("20-44576859-8").build()
        );

        AgenciaResponseDTO resultado = agenciaService.buscarPorId(guardada.getId());

        assertThat(resultado.razonSocial()).isEqualTo("Huryn");
        assertThat(resultado.cuit()).isEqualTo("20-44576859-8");
    }

    @Test
    void buscarPorId_lanzaExcepcionCuandoNoExiste() {
        assertThatThrownBy(() -> agenciaService.buscarPorId(99999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crear_guardaYDevuelveLaAgenciaPersistida() {
        CreateAgenciaRequestDTO dto = new CreateAgenciaRequestDTO("Viajes del Norte", "30-99887766-1");

        AgenciaResponseDTO resultado = agenciaService.crear(dto);

        assertThat(resultado.id()).isNotNull();
        assertThat(resultado.razonSocial()).isEqualTo("Viajes del Norte");
        assertThat(agenciaRepository.existsById(resultado.id())).isTrue();
    }

    @Test
    void crear_lanzaRecursoDuplicadoExceptionCuandoElCuitYaExiste() {
        agenciaRepository.save(AgenciaBuilder.anAgencia().withCuit("30-12345678-9").build());

        CreateAgenciaRequestDTO dtoDuplicado = new CreateAgenciaRequestDTO("Otra Agencia", "30-12345678-9");

        assertThatThrownBy(() -> agenciaService.crear(dtoDuplicado))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("30-12345678-9");
    }

    @Test
    void listarPendientes_retornaSoloAgenciasConEstadoPendiente() {
        agenciaRepository.save(AgenciaBuilder.anAgencia()
                .withRazonSocial("Agencia Pendiente")
                .withCuit("30-11111111-1")
                .withEmail("p1@agencia.com")
                .withEstado(EstadoAgencia.PENDIENTE)
                .build());

        agenciaRepository.save(AgenciaBuilder.anAgencia()
                .withRazonSocial("Agencia Autorizada")
                .withCuit("30-22222222-2")
                .withEmail("a1@agencia.com")
                .withEstado(EstadoAgencia.AUTORIZADA)
                .build());

        List<AgenciaResponseDTO> pendientes = agenciaService.listarPendientes();

        assertThat(pendientes).allMatch(a -> a.estado() == EstadoAgencia.PENDIENTE);
    }

    @Test
    void autorizar_cambiaEstadoAAutorizada() {
        Agencia guardada = agenciaRepository.save(AgenciaBuilder.anAgencia()
                .withEstado(EstadoAgencia.PENDIENTE)
                .build());

        AgenciaResponseDTO autorizada = agenciaService.autorizar(guardada.getId());

        assertThat(autorizada.estado()).isEqualTo(EstadoAgencia.AUTORIZADA);
        Agencia enDb = agenciaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(enDb.isAutorizada()).isTrue();
        assertThat(enDb.isActivo()).isTrue();
    }

    @Test
    void rechazar_cambiaEstadoARechazada() {
        Agencia guardada = agenciaRepository.save(AgenciaBuilder.anAgencia()
                .withEstado(EstadoAgencia.PENDIENTE)
                .build());

        AgenciaResponseDTO rechazada = agenciaService.rechazar(guardada.getId());

        assertThat(rechazada.estado()).isEqualTo(EstadoAgencia.RECHAZADA);
        Agencia enDb = agenciaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(enDb.isAutorizada()).isFalse();
        assertThat(enDb.isActivo()).isFalse();
    }
}