package ar.edu.unq.tusViajes.config;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgencyInitializerTest {

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AgencyInitializer agencyInitializer;

    @BeforeEach
    void setUp() {
        agencyInitializer = new AgencyInitializer(agencyRepository, passwordEncoder);
    }

    @Test
    void run_createsInitialAgency_whenNoAgenciesExist() {
        when(agencyRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("agency123")).thenReturn("encodedPassword");

        agencyInitializer.run();

        ArgumentCaptor<Agency> captor = ArgumentCaptor.forClass(Agency.class);
        verify(agencyRepository).save(captor.capture());

        Agency createdAgency = captor.getValue();
        assertThat(createdAgency.getEmail()).isEqualTo("agency@tusviajes.com");
        assertThat(createdAgency.getPasswordHash()).isEqualTo("encodedPassword");
        assertThat(createdAgency.getBusinessName()).isEqualTo("Agency");
        assertThat(createdAgency.getTaxId()).isEqualTo("30-40663891-2");
        assertThat(createdAgency.getStatus()).isEqualTo(AgencyStatus.AUTHORIZED);
    }

    @Test
    void run_doesNotCreateAgency_whenAgenciesAlreadyExist() {
        when(agencyRepository.count()).thenReturn(1L);

        agencyInitializer.run();

        verify(agencyRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
