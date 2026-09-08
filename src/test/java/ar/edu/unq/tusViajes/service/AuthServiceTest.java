package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AdminBuilder;
import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.AgencyRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.LoginRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AgencyRegistrationResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.LoginResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.InvalidCredentialsException;
import ar.edu.unq.tusViajes.exception.InvalidRefreshTokenException;
import ar.edu.unq.tusViajes.exception.UnauthorizedAgencyException;
import ar.edu.unq.tusViajes.model.Admin;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import ar.edu.unq.tusViajes.model.Role;
import ar.edu.unq.tusViajes.repository.AdminRepository;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
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

@Testcontainers(disabledWithoutDocker = true)
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
    private AgencyRepository agencyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void login_returnsTokenAndData_whenAdminIsValid() {
        adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword"))
                .build());

        LoginResponseDTO response = authService.login(new LoginRequestDTO("admin@test.com", "secretPassword"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.email()).isEqualTo("admin@test.com");
        assertThat(response.role()).isEqualTo(Role.ADMIN.name());
    }

    @Test
    void login_returnsToken_whenAgencyIsAuthorized() {
        agencyRepository.save(AgencyBuilder.anAgency()
                .withEmail("agency@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword"))
                .withStatus(AgencyStatus.AUTHORIZED)
                .build());

        LoginResponseDTO response = authService.login(new LoginRequestDTO("agency@test.com", "secretPassword"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.role()).isEqualTo(Role.AGENCY.name());
    }

    @Test
    void login_throwsUnauthorizedAgency_whenAgencyIsPending() {
        agencyRepository.save(AgencyBuilder.anAgency()
                .withEmail("pending@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword"))
                .withStatus(AgencyStatus.PENDING)
                .build());

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("pending@test.com", "secretPassword")))
                .isInstanceOf(UnauthorizedAgencyException.class)
                .hasMessageContaining("pending authorization");
    }

    @Test
    void login_throwsInvalidCredentials_whenPasswordIsIncorrect() {
        adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword"))
                .build());

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("admin@test.com", "wrongPassword")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throwsInvalidCredentials_whenEmailDoesNotExist() {
        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("notfound@test.com", "anyPassword")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void registerAgency_createsAgencyInPendingStatus() {
        AgencyRegistrationRequestDTO dto = new AgencyRegistrationRequestDTO(
                "SkyTravel SRL", "30-55555555-5", "contact@skytravel.com", "secretPassword123"
        );

        AgencyRegistrationResponseDTO response = authService.registerAgency(dto);

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(AgencyStatus.PENDING);
        assertThat(response.businessName()).isEqualTo("SkyTravel SRL");

        Agency inDb = agencyRepository.findById(response.id()).orElseThrow();
        assertThat(inDb.getStatus()).isEqualTo(AgencyStatus.PENDING);
        assertThat(inDb.isActive()).isFalse();
    }

    @Test
    void registerAgency_throwsException_whenTaxIdAlreadyExists() {
        agencyRepository.save(AgencyBuilder.anAgency().withTaxId("30-77777777-7").build());

        AgencyRegistrationRequestDTO dto = new AgencyRegistrationRequestDTO(
                "Another SRL", "30-77777777-7", "another@test.com", "secretPassword123"
        );

        assertThatThrownBy(() -> authService.registerAgency(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("30-77777777-7");
    }

    @Test
    void refreshToken_throwsInvalidRefreshToken_whenTokenIsAccessToken() {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin-access-token@test.com")
                .build());

        String accessToken = jwtTokenService.generateToken(admin, false);

        assertThatThrownBy(() -> authService.refreshToken(accessToken))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refreshToken_throwsInvalidRefreshToken_whenTokenIsExpiredOrInvalid() {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin-expired-token@test.com")
                .build());

        JwtTokenService expiredService = new JwtTokenService(
                "tusViajesSuperSecretKeyForJwtSigningMustBeAtLeast256BitsLong2026!",
                86400000L,
                -10000L
        );
        String expiredRefreshToken = expiredService.generateToken(admin, true);

        assertThatThrownBy(() -> authService.refreshToken(expiredRefreshToken))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refreshToken_returnsNewTokensAndData_whenRefreshTokenIsValid() {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin-valid-refresh@test.com")
                .build());

        String refreshToken = jwtTokenService.generateToken(admin, true);

        LoginResponseDTO response = authService.refreshToken(refreshToken);

        assertThat(response.token()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.email()).isEqualTo("admin-valid-refresh@test.com");
        assertThat(response.role()).isEqualTo(Role.ADMIN.name());
    }
}
