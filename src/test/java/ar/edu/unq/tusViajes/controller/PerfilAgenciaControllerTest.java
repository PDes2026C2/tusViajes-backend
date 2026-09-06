package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.builder.PerfilAgenciaBuilder;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.PerfilAgencia;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import ar.edu.unq.tusViajes.repository.PerfilAgenciaRepository;
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
class PerfilAgenciaControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PerfilAgenciaRepository perfilAgenciaRepository;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Test
    void listar_retorna200YListaDePerfiles() throws Exception {
        Agencia agencia = agenciaRepository.save(
                AgenciaBuilder.anAgencia().withRazonSocial("Agencia SA").build()
        );

        perfilAgenciaRepository.save(
                PerfilAgenciaBuilder.aPerfilAgencia()
                        .withAgencia(agencia)
                        .withNombre("Carlos")
                        .withEmail("carlos@agencia.com")
                        .build()
        );

        mockMvc.perform(get("/api/admin/perfiles-agencia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].agenciaRazonSocial").value("Agencia SA"));
    }

    @Test
    void buscarPorId_retorna200CuandoExiste() throws Exception {
        Agencia agencia = agenciaRepository.save(
                AgenciaBuilder.anAgencia().withRazonSocial("Agencia SA").build()
        );

        PerfilAgencia guardado = perfilAgenciaRepository.save(
                PerfilAgenciaBuilder.aPerfilAgencia()
                        .withAgencia(agencia)
                        .withEmail("carlos@agencia.com")
                        .build()
        );

        mockMvc.perform(get("/api/admin/perfiles-agencia/" + guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.email").value("carlos@agencia.com"));
    }

    @Test
    void buscarPorId_retorna404CuandoNoExiste() throws Exception {
        mockMvc.perform(get("/api/admin/perfiles-agencia/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_retorna201YLocationHeader() throws Exception {
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());

        String json = """
                {
                    "nombre": "Carlos",
                    "apellido": "Perez",
                    "email": "carlos@agencia.com",
                    "password": "secretPassword123",
                    "agenciaId": %d
                }
                """.formatted(agencia.getId());

        mockMvc.perform(post("/api/admin/perfiles-agencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("carlos@agencia.com"));
    }
}