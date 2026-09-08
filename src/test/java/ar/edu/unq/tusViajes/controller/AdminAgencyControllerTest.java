package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
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

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminAgencyControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgencyRepository agencyRepository;

    @Test
    void getPending_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/agencies/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPending_returns403_whenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/agencies/pending").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPending_returns200AndList_whenIsAdmin() throws Exception {
        agencyRepository.save(AgencyBuilder.anAgency()
                .withBusinessName("Agencia P1")
                .withTaxId("30-11111111-1")
                .withStatus(AgencyStatus.PENDING)
                .build());

        mockMvc.perform(get("/api/admin/agencies/pending").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].businessName").value("Agencia P1"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void authorize_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/admin/agencies/1/authorize"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorize_returns403_whenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/agencies/1/authorize").with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorize_returns200AndChangesStatusToAuthorized() throws Exception {
        Agency saved = agencyRepository.save(AgencyBuilder.anAgency()
                .withStatus(AgencyStatus.PENDING)
                .build());

        mockMvc.perform(post("/api/admin/agencies/" + saved.getId() + "/authorize").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"));

        Agency inDb = agencyRepository.findById(saved.getId()).orElseThrow();
        assertThat(inDb.isAuthorized()).isTrue();
    }

    @Test
    void reject_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/admin/agencies/1/reject"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reject_returns403_whenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/agencies/1/reject").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void reject_returns200AndChangesStatusToRejected() throws Exception {
        Agency saved = agencyRepository.save(AgencyBuilder.anAgency()
                .withStatus(AgencyStatus.PENDING)
                .build());

        mockMvc.perform(post("/api/admin/agencies/" + saved.getId() + "/reject").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        Agency inDb = agencyRepository.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getStatus()).isEqualTo(AgencyStatus.REJECTED);
    }
}
