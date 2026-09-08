package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
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

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class AgencyControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgencyRepository agencyRepository;

    @Test
    void getAll_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/agencies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_returns403_whenRoleIsAgency() throws Exception {
        mockMvc.perform(get("/api/agencies").with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_returns403_whenRoleIsBuyer() throws Exception {
        mockMvc.perform(get("/api/agencies").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_returns200AndList_whenRoleIsAdmin() throws Exception {
        agencyRepository.save(AgencyBuilder.anAgency().withBusinessName("South Travel").build());

        mockMvc.perform(get("/api/agencies").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].businessName").value("South Travel"));
    }

    @Test
    void getById_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/agencies/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_returns403_whenRoleIsAgency() throws Exception {
        mockMvc.perform(get("/api/agencies/1").with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_returns403_whenRoleIsBuyer() throws Exception {
        mockMvc.perform(get("/api/agencies/1").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_returns200WhenExists_whenRoleIsAdmin() throws Exception {
        Agency saved = agencyRepository.save(
                AgencyBuilder.anAgency().withBusinessName("South Travel").withTaxId("30-12345678-9").build()
        );

        mockMvc.perform(get("/api/agencies/" + saved.getId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.businessName").value("South Travel"));
    }

    @Test
    void getById_returns404WhenDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/agencies/99999").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_returns401_whenUnauthenticated() throws Exception {
        String json = """
                {
                    "businessName": "Updated SA"
                }
                """;

        mockMvc.perform(put("/api/agencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_modifiesBusinessNameAndReturns200() throws Exception {
        Agency saved = agencyRepository.save(AgencyBuilder.anAgency().withBusinessName("Original SA").build());

        String json = """
                {
                    "businessName": "Updated SA"
                }
                """;

        mockMvc.perform(put("/api/agencies/" + saved.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.businessName").value("Updated SA"));
    }

    @Test
    void delete_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/agencies/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_returns204NoContent() throws Exception {
        Agency saved = agencyRepository.save(AgencyBuilder.anAgency().build());

        mockMvc.perform(delete("/api/agencies/" + saved.getId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        assertThat(agencyRepository.existsById(saved.getId())).isFalse();
    }
}
