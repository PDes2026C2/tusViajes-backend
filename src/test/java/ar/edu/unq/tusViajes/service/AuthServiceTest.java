package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AdminBuilder;
import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.LoginRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.RegistroAgenciaRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.LoginResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.RegistroAgenciaResponseDTO;
import ar.edu.unq.tusViajes.exception.AgenciaNoAutorizadaException;
import ar.edu.unq.tusViajes.exception.CredencialesInvalidasException;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.InvalidRefreshTokenException;
import ar.edu.unq.tusViajes.model.Admin;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.EstadoAgencia;
import ar.edu.unq.tusViajes.model.Rol;
import ar.edu.unq.tusViajes.repository.AdminRepository;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import ar.edu.unq.tusViajes.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Transactional
class AuthServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void login_retornaTokenYDatos_cuandoAdminValido() {
        adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword"))
                .build());

        LoginResponseDTO respuesta = authService.login(new LoginRequestDTO("admin@test.com", "secretPassword"));

        assertThat(respuesta.token()).isNotBlank();
        assertThat(respuesta.tokenType()).isEqualTo("Bearer");
        assertThat(respuesta.email()).isEqualTo("admin@test.com");
        assertThat(respuesta.rol()).isEqualTo(Rol.ADMIN.name());
    }

    @Test
    void login_retornaToken_cuandoAgenciaAutorizada() {
        agenciaRepository.save(AgenciaBuilder.anAgencia()
                .withEmail("agencia@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword"))
                .withEstado(EstadoAgencia.AUTORIZADA)
                .build());

        LoginResponseDTO respuesta = authService.login(new LoginRequestDTO("agencia@test.com", "secretPassword"));

        assertThat(respuesta.token()).isNotBlank();
        assertThat(respuesta.rol()).isEqualTo(Rol.AGENCIA.name());
    }

    @Test
    void login_lanzaAgenciaNoAutorizada_cuandoAgenciaEstaPendiente() {
        agenciaRepository.save(AgenciaBuilder.anAgencia()
                .withEmail("pendiente@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword"))
                .withEstado(EstadoAgencia.PENDIENTE)
                .build());

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("pendiente@test.com", "secretPassword")))
                .isInstanceOf(AgenciaNoAutorizadaException.class)
                .hasMessageContaining("pendiente de autorizacion");
    }

    @Test
    void login_lanzaCredencialesInvalidas_cuandoPasswordIncorrecto() {
        adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword"))
                .build());

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("admin@test.com", "wrongPassword")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void login_lanzaCredencialesInvalidas_cuandoEmailNoExiste() {
        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("noexiste@test.com", "anyPassword")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void registrarAgencia_creaAgenciaEnEstadoPendiente() {
        RegistroAgenciaRequestDTO dto = new RegistroAgenciaRequestDTO(
                "Despegar SRL", "30-55555555-5", "contacto@despegar.com", "secretPassword123"
        );

        RegistroAgenciaResponseDTO respuesta = authService.registrarAgencia(dto);

        assertThat(respuesta.id()).isNotNull();
        assertThat(respuesta.estado()).isEqualTo(EstadoAgencia.PENDIENTE);
        assertThat(respuesta.razonSocial()).isEqualTo("Despegar SRL");

        Agencia enDb = agenciaRepository.findById(respuesta.id()).orElseThrow();
        assertThat(enDb.getEstado()).isEqualTo(EstadoAgencia.PENDIENTE);
        assertThat(enDb.isActivo()).isFalse();
    }

    @Test
    void registrarAgencia_lanzaExcepcion_cuandoCuitYaExiste() {
        agenciaRepository.save(AgenciaBuilder.anAgencia().withCuit("30-77777777-7").build());

        RegistroAgenciaRequestDTO dto = new RegistroAgenciaRequestDTO(
                "Otra SRL", "30-77777777-7", "otra@test.com", "secretPassword123"
        );

        assertThatThrownBy(() -> authService.registrarAgencia(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("30-77777777-7");
    }

    @Test
    void refreshToken_lanzaInvalidRefreshToken_cuandoTokenEsDeAcceso() {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin-access-token@test.com")
                .build());

        String accessToken = jwtTokenService.generarToken(admin, false);

        assertThatThrownBy(() -> authService.refreshToken(accessToken))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refreshToken_lanzaInvalidRefreshToken_cuandoTokenExpiradoOInvalido() {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin-expired-token@test.com")
                .build());

        JwtTokenService expiredService = new JwtTokenService(
                "tusViajesSuperSecretKeyForJwtSigningMustBeAtLeast256BitsLong2026!",
                86400000L,
                -10000L
        );
        String expiredRefreshToken = expiredService.generarToken(admin, true);

        assertThatThrownBy(() -> authService.refreshToken(expiredRefreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refreshToken_retornaNuevosTokensYDatos_cuandoRefreshTokenEsValido() {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin-valid-refresh@test.com")
                .build());

        String refreshToken = jwtTokenService.generarToken(admin, true);

        LoginResponseDTO respuesta = authService.refreshToken(refreshToken);

        assertThat(respuesta.token()).isNotBlank();
        assertThat(respuesta.refreshToken()).isNotBlank();
        assertThat(respuesta.email()).isEqualTo("admin-valid-refresh@test.com");
        assertThat(respuesta.rol()).isEqualTo(Rol.ADMIN.name());
    }
}
