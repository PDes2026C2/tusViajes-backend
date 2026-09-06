package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.builder.PerfilAgenciaBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.PerfilAgenciaRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PerfilAgenciaResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.PerfilAgencia;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import ar.edu.unq.tusViajes.repository.PerfilAgenciaRepository;
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

@Testcontainers
@SpringBootTest
@Transactional 
class PerfilAgenciaServiceTest {

    
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    
    @Autowired
    private PerfilAgenciaService perfilAgenciaService;

    @Autowired
    private PerfilAgenciaRepository perfilAgenciaRepository;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Test
    void listar_retornaTodosLosPerfilesAgencia() {
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        PerfilAgencia perfil = PerfilAgenciaBuilder.aPerfilAgencia()
                .withAgencia(agencia)
                .build();
        perfilAgenciaRepository.save(perfil);

        List<PerfilAgenciaResponseDTO> resultado = perfilAgenciaService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).isEqualTo(perfil.getNombre());
    }

    @Test
    void buscarPorId_devuelvePerfilCuandoExiste() {
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        PerfilAgencia perfil = PerfilAgenciaBuilder.aPerfilAgencia()
                .withAgencia(agencia)
                .build();
        PerfilAgencia guardado = perfilAgenciaRepository.save(perfil);

        PerfilAgenciaResponseDTO resultado = perfilAgenciaService.buscarPorId(guardado.getId());

        assertThat(resultado.nombre()).isEqualTo(perfil.getNombre());
        assertThat(resultado.email()).isEqualTo(perfil.getEmail());
    }

    @Test
    void crear_creaPerfilVinculadoAAgencia() {
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());

        PerfilAgenciaRequestDTO dto = new PerfilAgenciaRequestDTO(
                "Carlos", "Perez", "carlos@agencia.com", "pass123", agencia.getId()
        );

        PerfilAgenciaResponseDTO resultado = perfilAgenciaService.crear(dto);

        assertThat(resultado.nombre()).isEqualTo("Carlos");
        assertThat(resultado.email()).isEqualTo("carlos@agencia.com");

        
        assertThat(perfilAgenciaRepository.existsById(resultado.id())).isTrue();
    }

    @Test
    void crear_lanzaExcepcionSiAgenciaYaTienePerfilAsociado() {
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());

        
        PerfilAgencia perfilExistente = PerfilAgenciaBuilder.aPerfilAgencia()
                .withAgencia(agencia)
                .build();
        perfilAgenciaRepository.save(perfilExistente);

        PerfilAgenciaRequestDTO dto = new PerfilAgenciaRequestDTO(
                "Carlos", "Perez", "carlos@agencia.com", "pass123", agencia.getId()
        );

        assertThatThrownBy(() -> perfilAgenciaService.crear(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("ya tiene un perfil asociado");
    }

    @Test
    void crear_lanzaExcepcionSiEmailYaExiste() {
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());

        
        PerfilAgencia perfilExistente = PerfilAgenciaBuilder.aPerfilAgencia()
                .withAgencia(agencia)
                .withEmail("repetido@agencia.com")
                .build();
        perfilAgenciaRepository.save(perfilExistente);

       
        Agencia otraAgencia = agenciaRepository.save(
                AgenciaBuilder.anAgencia().withCuit("30-99999999-9").build()
        );

        PerfilAgenciaRequestDTO dto = new PerfilAgenciaRequestDTO(
                "Carlos", "Perez", "repetido@agencia.com", "pass123", otraAgencia.getId()
        );

        assertThatThrownBy(() -> perfilAgenciaService.crear(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("repetido@agencia.com");
    }
}