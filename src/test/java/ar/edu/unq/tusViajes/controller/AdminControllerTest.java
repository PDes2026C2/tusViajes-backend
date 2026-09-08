package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AdminBuilder;
import ar.edu.unq.tusViajes.model.Admin;
import ar.edu.unq.tusViajes.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class AdminControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Test
    void getAll_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/administrators"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_returns403_whenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/administrators").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_returns200AndListOfAdmins() throws Exception {
        adminRepository.save(
                AdminBuilder.anAdmin()
                        .withEmail("admin@tusviajes.com")
                        .build()
        );

        mockMvc.perform(get("/api/admin/administrators").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].email").value("admin@tusviajes.com"));
    }

    @Test
    void getById_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/administrators/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_returns403_whenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/administrators/1").with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_returns200WhenExists() throws Exception {
        Admin saved = adminRepository.save(
                AdminBuilder.anAdmin()
                        .withFirstName("Admin")
                        .withEmail("admin@tusviajes.com")
                        .build()
        );

        mockMvc.perform(get("/api/admin/administrators/" + saved.getId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.firstName").value("Admin"));
    }

    @Test
    void getById_returns404WhenDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/admin/administrators/99999").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns401_whenUnauthenticated() throws Exception {
        String json = """
                {
                    "firstName": "Admin",
                    "lastName": "Root",
                    "email": "admin@tusviajes.com",
                    "password": "rootPassword123"
                }
                """;

        mockMvc.perform(post("/api/admin/administrators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_returns403_whenRoleIsNotAdmin() throws Exception {
        String json = """
                {
                    "firstName": "Admin",
                    "lastName": "Root",
                    "email": "admin@tusviajes.com",
                    "password": "rootPassword123"
                }
                """;

        mockMvc.perform(post("/api/admin/administrators")
                        .with(user("buyer").roles("BUYER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_returns201AndLocationHeader() throws Exception {
        String json = """
                {
                    "firstName": "Admin",
                    "lastName": "Root",
                    "email": "admin@tusviajes.com",
                    "password": "rootPassword123"
                }
                """;

        mockMvc.perform(post("/api/admin/administrators")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@tusviajes.com"));
    }
}
