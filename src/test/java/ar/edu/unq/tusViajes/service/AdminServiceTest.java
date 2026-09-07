package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AdminBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.CreateAdminRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AdminResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Admin;
import ar.edu.unq.tusViajes.repository.AdminRepository;
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
class AdminServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRepository adminRepository;

    @Test
    void listar_retornaTodosLosAdmins() {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin().build());

        List<AdminResponseDTO> resultado = adminService.listar();

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.getFirst().nombre()).isEqualTo(admin.getNombre());
    }

    @Test
    void buscarPorId_retornaAdminCuandoExiste() {
        Admin guardado = adminRepository.save(AdminBuilder.anAdmin().build());

        AdminResponseDTO resultado = adminService.buscarPorId(guardado.getId());

        assertThat(resultado.nombre()).isEqualTo(guardado.getNombre());
        assertThat(resultado.email()).isEqualTo(guardado.getEmail());
    }

    @Test
    void buscarPorId_lanzaExcepcionCuandoNoExiste() {
        assertThatThrownBy(() -> adminService.buscarPorId(99999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crear_guardaYRetornaAdmin() {
        CreateAdminRequestDTO dto = new CreateAdminRequestDTO("Super", "Admin", "newadmin@test.com", "secretPassword123");

        AdminResponseDTO resultado = adminService.crear(dto);

        assertThat(resultado.id()).isNotNull();
        assertThat(resultado.email()).isEqualTo("newadmin@test.com");
    }

    @Test
    void crear_lanzaExcepcionCuandoEmailYaExiste() {
        adminRepository.save(AdminBuilder.anAdmin().withEmail("repetido@test.com").build());

        CreateAdminRequestDTO dto = new CreateAdminRequestDTO("Otro", "Admin", "repetido@test.com", "secretPassword123");

        assertThatThrownBy(() -> adminService.crear(dto))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
