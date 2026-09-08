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

@Testcontainers
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
    void listar_retorna401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/admin/administradores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listar_retorna403_cuandoRolNoEsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/administradores").with(user("comprador").roles("COMPRADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_retorna200YListaDeAdmins() throws Exception {
        adminRepository.save(
                AdminBuilder.anAdmin()
                        .withEmail("admin@tusviajes.com")
                        .build()
        );

        mockMvc.perform(get("/api/admin/administradores").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].email").value("admin@tusviajes.com"));
    }

    @Test
    void buscarPorId_retorna401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/admin/administradores/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buscarPorId_retorna403_cuandoRolNoEsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/administradores/1").with(user("agencia").roles("AGENCIA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void buscarPorId_retorna200CuandoExiste() throws Exception {
        Admin guardado = adminRepository.save(
                AdminBuilder.anAdmin()
                        .withNombre("Admin")
                        .withEmail("admin@tusviajes.com")
                        .build()
        );

        mockMvc.perform(get("/api/admin/administradores/" + guardado.getId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombre").value("Admin"));
    }

    @Test
    void buscarPorId_retorna404CuandoNoExiste() throws Exception {
        mockMvc.perform(get("/api/admin/administradores/99999").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_retorna401_cuandoNoAutenticado() throws Exception {
        String json = """
                {
                    "nombre": "Admin",
                    "apellido": "Root",
                    "email": "admin@tusviajes.com",
                    "password": "rootPassword123"
                }
                """;

        mockMvc.perform(post("/api/admin/administradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crear_retorna403_cuandoRolNoEsAdmin() throws Exception {
        String json = """
                {
                    "nombre": "Admin",
                    "apellido": "Root",
                    "email": "admin@tusviajes.com",
                    "password": "rootPassword123"
                }
                """;

        mockMvc.perform(post("/api/admin/administradores")
                        .with(user("comprador").roles("COMPRADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_retorna201YLocationHeader() throws Exception {
        String json = """
                {
                    "nombre": "Admin",
                    "apellido": "Root",
                    "email": "admin@tusviajes.com",
                    "password": "rootPassword123"
                }
                """;

        mockMvc.perform(post("/api/admin/administradores")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@tusviajes.com"));
    }
}
