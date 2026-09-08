package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.UpdateAgencyRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AgencyResponseDTO;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Transactional
public class AgencyServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private AgencyService agencyService;

    @Autowired
    private AgencyRepository agencyRepository;

    @Test
    void getAll_returnsAllAgenciesFromDatabase() {
        Agency saved = agencyRepository.save(AgencyBuilder.anAgency().build());

        List<AgencyResponseDTO> result = agencyService.getAll();

        assertThat(result).isNotEmpty();
        assertEquals(result.getFirst().businessName(), saved.getBusinessName());
    }

    @Test
    void getById_returnsAgencyWhenExists() {
        Agency saved = agencyRepository.save(
                AgencyBuilder.anAgency().withBusinessName("Huryn").withTaxId("20-44576859-8").build()
        );

        AgencyResponseDTO result = agencyService.getById(saved.getId());

        assertThat(result.businessName()).isEqualTo("Huryn");
        assertThat(result.taxId()).isEqualTo("20-44576859-8");
    }

    @Test
    void getById_throwsExceptionWhenDoesNotExist() {
        assertThatThrownBy(() -> agencyService.getById(99999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_modifiesBusinessNameAndPersists() {
        Agency saved = agencyRepository.save(
                AgencyBuilder.anAgency().withBusinessName("Old Name").build()
        );

        UpdateAgencyRequestDTO dto = new UpdateAgencyRequestDTO("New Name SA");

        AgencyResponseDTO result = agencyService.update(saved.getId(), dto);

        assertThat(result.businessName()).isEqualTo("New Name SA");
        Agency inDb = agencyRepository.findById(saved.getId()).orElseThrow();
        assertThat(inDb.getBusinessName()).isEqualTo("New Name SA");
    }

    @Test
    void getPending_returnsOnlyAgenciesWithPendingStatus() {
        agencyRepository.save(AgencyBuilder.anAgency()
                .withBusinessName("Pending Agency")
                .withTaxId("30-11111111-1")
                .withEmail("p1@agency.com")
                .withStatus(AgencyStatus.PENDING)
                .build());

        agencyRepository.save(AgencyBuilder.anAgency()
                .withBusinessName("Authorized Agency")
                .withTaxId("30-22222222-2")
                .withEmail("a1@agency.com")
                .withStatus(AgencyStatus.AUTHORIZED)
                .build());

        List<AgencyResponseDTO> pending = agencyService.getPending();

        assertThat(pending).allMatch(a -> a.status() == AgencyStatus.PENDING);
    }

    @Test
    void authorize_changesStatusToAuthorized() {
        Agency saved = agencyRepository.save(AgencyBuilder.anAgency()
                .withStatus(AgencyStatus.PENDING)
                .build());

        AgencyResponseDTO authorized = agencyService.authorize(saved.getId());

        assertThat(authorized.status()).isEqualTo(AgencyStatus.AUTHORIZED);
        Agency inDb = agencyRepository.findById(saved.getId()).orElseThrow();
        assertThat(inDb.isAuthorized()).isTrue();
        assertThat(inDb.isActive()).isTrue();
    }

    @Test
    void reject_changesStatusToRejected() {
        Agency saved = agencyRepository.save(AgencyBuilder.anAgency()
                .withStatus(AgencyStatus.PENDING)
                .build());

        AgencyResponseDTO rejected = agencyService.reject(saved.getId());

        assertThat(rejected.status()).isEqualTo(AgencyStatus.REJECTED);
        Agency inDb = agencyRepository.findById(saved.getId()).orElseThrow();
        assertThat(inDb.isAuthorized()).isFalse();
        assertThat(inDb.isActive()).isFalse();
    }
}
