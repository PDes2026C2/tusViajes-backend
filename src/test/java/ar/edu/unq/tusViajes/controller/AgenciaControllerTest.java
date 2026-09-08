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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
    void listar_retorna401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/agencias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listar_retorna403_cuandoEsAgencia() throws Exception {
        mockMvc.perform(get("/api/agencias").with(user("agencia").roles("AGENCIA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_retorna403_cuandoEsComprador() throws Exception {
        mockMvc.perform(get("/api/agencias").with(user("comprador").roles("COMPRADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_retorna200YLista_cuandoEsAdmin() throws Exception {
        agenciaRepository.save(AgenciaBuilder.anAgencia().withRazonSocial("Turismo Sur").build());

        mockMvc.perform(get("/api/agencias").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].razonSocial").value("Turismo Sur"));
    }

    @Test
    void buscarPorId_retorna401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/agencias/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buscarPorId_retorna403_cuandoEsAgencia() throws Exception {
        mockMvc.perform(get("/api/agencias/1").with(user("agencia").roles("AGENCIA")))
                .andExpect(status().isForbidden());
    }

    @Test
    void buscarPorId_retorna403_cuandoEsComprador() throws Exception {
        mockMvc.perform(get("/api/agencias/1").with(user("comprador").roles("COMPRADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void buscarPorId_retorna200CuandoExiste_cuandoEsAdmin() throws Exception {
        Agencia guardada = agenciaRepository.save(
                AgenciaBuilder.anAgencia().withRazonSocial("Turismo Sur").withCuit("30-12345678-9").build()
        );

        mockMvc.perform(get("/api/agencias/" + guardada.getId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardada.getId()))
                .andExpect(jsonPath("$.razonSocial").value("Turismo Sur"));
    }

    @Test
    void buscarPorId_retorna404CuandoNoExiste() throws Exception {
        mockMvc.perform(get("/api/agencias/99999").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizar_retorna401_cuandoNoAutenticado() throws Exception {
        String json = """
                {
                    "razonSocial": "Modificada SA"
                }
                """;

        mockMvc.perform(put("/api/agencias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actualizar_modificaRazonSocialYRetorna200() throws Exception {
        Agencia guardada = agenciaRepository.save(AgenciaBuilder.anAgencia().withRazonSocial("Original SA").build());

        String json = """
                {
                    "razonSocial": "Modificada SA"
                }
                """;

        mockMvc.perform(put("/api/agencias/" + guardada.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardada.getId()))
                .andExpect(jsonPath("$.razonSocial").value("Modificada SA"));
    }

    @Test
    void eliminar_retorna401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(delete("/api/agencias/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eliminar_retorna204NoContent() throws Exception {
        Agencia guardada = agenciaRepository.save(AgenciaBuilder.anAgencia().build());

        mockMvc.perform(delete("/api/agencias/" + guardada.getId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        assertThat(agenciaRepository.existsById(guardada.getId())).isFalse();
    }
}