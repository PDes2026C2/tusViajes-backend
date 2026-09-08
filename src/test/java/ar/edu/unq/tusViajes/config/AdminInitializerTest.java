package ar.edu.unq.tusViajes.config;

import ar.edu.unq.tusViajes.model.Admin;
import ar.edu.unq.tusViajes.repository.AdminRepository;
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
class AdminInitializerTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminInitializer adminInitializer;

    @BeforeEach
    void setUp() {
        adminInitializer = new AdminInitializer(adminRepository, passwordEncoder);
    }

    @Test
    void run_creaAdminInicial_cuandoNoExistenAdmins() {
        when(adminRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("admin123")).thenReturn("encodedPassword");

        adminInitializer.run();

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(captor.capture());

        Admin adminCreado = captor.getValue();
        assertThat(adminCreado.getEmail()).isEqualTo("admin@tusviajes.com");
        assertThat(adminCreado.getPasswordHash()).isEqualTo("encodedPassword");
        assertThat(adminCreado.getNombre()).isEqualTo("Admin");
        assertThat(adminCreado.getApellido()).isEqualTo("Sistema");
    }

    @Test
    void run_noCreaAdmin_cuandoYaExistenAdmins() {
        when(adminRepository.count()).thenReturn(1L);

        adminInitializer.run();

        verify(adminRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
