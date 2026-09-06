package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.PerfilAdminBuilder;
import ar.edu.unq.tusViajes.model.PerfilAdmin;
import ar.edu.unq.tusViajes.repository.PerfilAdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class PerfilAdminControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PerfilAdminRepository perfilAdminRepository;

    @Test
    void listar_retorna200YListaDeAdmins() throws Exception {
        perfilAdminRepository.save(
                PerfilAdminBuilder.aPerfilAdmin()
                        .withEmail("admin@tusviajes.com")
                        .build()
        );

        mockMvc.perform(get("/api/admin/perfiles-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].email").value("admin@tusviajes.com"));
    }

    @Test
    void buscarPorId_retorna200CuandoExiste() throws Exception {
        PerfilAdmin guardado = perfilAdminRepository.save(
                PerfilAdminBuilder.aPerfilAdmin()
                        .withNombre("Admin")
                        .withEmail("admin@tusviajes.com")
                        .build()
        );

        mockMvc.perform(get("/api/admin/perfiles-admin/" + guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombre").value("Admin"));
    }

    @Test
    void buscarPorId_retorna404CuandoNoExiste() throws Exception {
        mockMvc.perform(get("/api/admin/perfiles-admin/99999"))
                .andExpect(status().isNotFound());
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

        mockMvc.perform(post("/api/admin/perfiles-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@tusviajes.com"));
    }
}