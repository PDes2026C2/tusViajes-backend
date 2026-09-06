package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class AgenciaControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Test
    void listar_retorna200YListaDesdePostgres() throws Exception {
        agenciaRepository.save(AgenciaBuilder.anAgencia().withRazonSocial("Turismo Sur").build());

        mockMvc.perform(get("/api/agencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].razonSocial").value("Turismo Sur"));
    }

    @Test
    void buscarPorId_retorna200CuandoExiste() throws Exception {
        Agencia guardada = agenciaRepository.save(
                AgenciaBuilder.anAgencia().withRazonSocial("Turismo Sur").withCuit("30-12345678-9").build()
        );

        mockMvc.perform(get("/api/agencias/" + guardada.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardada.getId()))
                .andExpect(jsonPath("$.razonSocial").value("Turismo Sur"));
    }

    @Test
    void buscarPorId_retorna404CuandoNoExiste() throws Exception {
        mockMvc.perform(get("/api/agencias/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_persisteEnPostgresYRetorna201() throws Exception {
        String json = """
                {
                    "razonSocial": "Nueva Agencia SA",
                    "cuit": "30-77665544-3"
                }
                """;

        mockMvc.perform(post("/api/agencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.razonSocial").value("Nueva Agencia SA"));
    }

    @Test
    void eliminar_retorna204NoContent() throws Exception {
        Agencia guardada = agenciaRepository.save(AgenciaBuilder.anAgencia().build());

        mockMvc.perform(delete("/api/agencias/" + guardada.getId()))
                .andExpect(status().isNoContent());

        assertThat(agenciaRepository.existsById(guardada.getId())).isFalse();
    }
}