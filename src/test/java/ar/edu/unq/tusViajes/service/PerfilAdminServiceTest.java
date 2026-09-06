package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.PerfilAdminBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.PerfilAdminRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PerfilAdminResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.model.PerfilAdmin;
import ar.edu.unq.tusViajes.repository.PerfilAdminRepository;
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
class PerfilAdminServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PerfilAdminService perfilAdminService;

    @Autowired
    private PerfilAdminRepository perfilAdminRepository;

    @Test
    void listar_retornaTodosLosAdmins() {
        PerfilAdmin admin = perfilAdminRepository.save(PerfilAdminBuilder.aPerfilAdmin().build());

        List<PerfilAdminResponseDTO> resultado = perfilAdminService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).isEqualTo(admin.getNombre());
    }

    @Test
    void buscarPorId_devuelveAdminCuandoExiste() {
        PerfilAdmin admin = perfilAdminRepository.save(PerfilAdminBuilder.aPerfilAdmin().build());

        PerfilAdminResponseDTO resultado = perfilAdminService.buscarPorId(admin.getId());

        assertThat(resultado.nombre()).isEqualTo(admin.getNombre());
        assertThat(resultado.email()).isEqualTo(admin.getEmail());
    }

    @Test
    void crear_guardaAdminConPasswordHasheado() {
        PerfilAdminRequestDTO dto = new PerfilAdminRequestDTO(
                "Super", "Admin", "super@admin.com", "root123"
        );

        PerfilAdminResponseDTO resultado = perfilAdminService.crear(dto);

        assertThat(resultado.nombre()).isEqualTo("Super");
        assertThat(resultado.email()).isEqualTo("super@admin.com");

        PerfilAdmin guardadoEnDb = perfilAdminRepository.findById(resultado.id()).orElseThrow();
        assertThat(guardadoEnDb.getPasswordHash()).isNotEqualTo("root123");
    }

    @Test
    void crear_lanzaExcepcionSiEmailEstaDuplicado() {
        PerfilAdmin adminExistente = PerfilAdminBuilder.aPerfilAdmin()
                .withEmail("duplicado@admin.com")
                .build();
        perfilAdminRepository.save(adminExistente);

        PerfilAdminRequestDTO dto = new PerfilAdminRequestDTO(
                "Super", "Admin", "duplicado@admin.com", "root123"
        );

        assertThatThrownBy(() -> perfilAdminService.crear(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("duplicado@admin.com");
    }
}