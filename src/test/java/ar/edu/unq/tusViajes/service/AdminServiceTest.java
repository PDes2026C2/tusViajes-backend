package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AdminBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.AdminRegistrationRequestDTO;
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

@Testcontainers(disabledWithoutDocker = true)
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
    void getAll_returnsAllAdmins() {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin().build());

        List<AdminResponseDTO> result = adminService.getAll();

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().firstName()).isEqualTo(admin.getFirstName());
    }

    @Test
    void getById_returnsAdminWhenExists() {
        Admin saved = adminRepository.save(AdminBuilder.anAdmin().build());

        AdminResponseDTO result = adminService.getById(saved.getId());

        assertThat(result.firstName()).isEqualTo(saved.getFirstName());
        assertThat(result.email()).isEqualTo(saved.getEmail());
    }

    @Test
    void getById_throwsExceptionWhenDoesNotExist() {
        assertThatThrownBy(() -> adminService.getById(99999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesAndReturnsAdmin() {
        AdminRegistrationRequestDTO dto = new AdminRegistrationRequestDTO("Super", "Admin", "newadmin@test.com", "secretPassword123");

        AdminResponseDTO result = adminService.create(dto);

        assertThat(result.id()).isNotNull();
        assertThat(result.email()).isEqualTo("newadmin@test.com");
    }

    @Test
    void create_throwsExceptionWhenEmailAlreadyExists() {
        adminRepository.save(AdminBuilder.anAdmin().withEmail("repetido@test.com").build());

        AdminRegistrationRequestDTO dto = new AdminRegistrationRequestDTO("Other", "Admin", "repetido@test.com", "secretPassword123");

        assertThatThrownBy(() -> adminService.create(dto))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
