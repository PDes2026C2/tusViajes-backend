package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.EstadoAgencia;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminAgenciaControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Test
    void listarPendientes_retorna401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/admin/agencias/pendientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarPendientes_retorna403_cuandoRolNoEsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/agencias/pendientes").with(user("comprador").roles("COMPRADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarPendientes_retorna200YLista_cuandoEsAdmin() throws Exception {
        agenciaRepository.save(AgenciaBuilder.anAgencia()
                .withRazonSocial("Agencia P1")
                .withCuit("30-11111111-1")
                .withEstado(EstadoAgencia.PENDIENTE)
                .build());

        mockMvc.perform(get("/api/admin/agencias/pendientes").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].razonSocial").value("Agencia P1"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    void autorizar_retorna200YCambiaEstadoAAutorizada() throws Exception {
        Agencia guardada = agenciaRepository.save(AgenciaBuilder.anAgencia()
                .withEstado(EstadoAgencia.PENDIENTE)
                .build());

        mockMvc.perform(post("/api/admin/agencias/" + guardada.getId() + "/autorizar").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("AUTORIZADA"));

        Agencia enDb = agenciaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(enDb.isAutorizada()).isTrue();
    }

    @Test
    void rechazar_retorna200YCambiaEstadoARechazada() throws Exception {
        Agencia guardada = agenciaRepository.save(AgenciaBuilder.anAgencia()
                .withEstado(EstadoAgencia.PENDIENTE)
                .build());

        mockMvc.perform(post("/api/admin/agencias/" + guardada.getId() + "/rechazar").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));

        Agencia enDb = agenciaRepository.findById(guardada.getId()).orElseThrow();
        assertThat(enDb.getEstado()).isEqualTo(EstadoAgencia.RECHAZADA);
    }
}
