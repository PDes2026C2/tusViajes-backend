package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AdminBuilder;
import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.model.Admin;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import ar.edu.unq.tusViajes.repository.AdminRepository;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void login_returns200AndToken_whenAdminIsValid() throws Exception {
        adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword123"))
                .build());

        String json = """
                {
                    "email": "admin@test.com",
                    "password": "secretPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    void login_returns403_whenAgencyIsPending() throws Exception {
        agencyRepository.save(AgencyBuilder.anAgency()
                .withEmail("pending@agency.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword123"))
                .withStatus(AgencyStatus.PENDING)
                .build());

        String json = """
                {
                    "email": "pending@agency.com",
                    "password": "secretPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("The agency is pending authorization by an administrator."));
    }

    @Test
    void login_returns200AndToken_whenAgencyIsAuthorized() throws Exception {
        agencyRepository.save(AgencyBuilder.anAgency()
                .withEmail("authorized@agency.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword123"))
                .withStatus(AgencyStatus.AUTHORIZED)
                .build());

        String json = """
                {
                    "email": "authorized@agency.com",
                    "password": "secretPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("AGENCY"));
    }

    @Test
    void login_returns401_whenPasswordIsIncorrect() throws Exception {
        adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword123"))
                .build());

        String json = """
                {
                    "email": "admin@test.com",
                    "password": "wrongPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerAgency_returns201AndPendingStatus() throws Exception {
        String json = """
                {
                    "businessName": "New Agency SA",
                    "taxId": "30-55667788-9",
                    "email": "new@agency.com",
                    "password": "secretPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/register/agency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.businessName").value("New Agency SA"))
                .andExpect(jsonPath("$.message").value("Registration request received. Pending authorization by an administrator."));
    }

    @Test
    void registerBuyer_returns201AndBuyerData() throws Exception {
        String json = """
                {
                    "firstName": "Agustin",
                    "lastName": "Perez",
                    "email": "agustin@buyer.com",
                    "password": "secretPassword123",
                    "phoneNumber": "1122334455",
                    "nationalId": "39123456"
                }
                """;

        mockMvc.perform(post("/api/auth/register/buyer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("Agustin"))
                .andExpect(jsonPath("$.email").value("agustin@buyer.com"));
    }

    @Test
    void refreshToken_returns401_whenTokenIsAccessToken() throws Exception {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin-access-ctrl@test.com")
                .build());

        String accessToken = jwtTokenService.generateToken(admin, false);

        String json = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(accessToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid refresh token."));
    }

    @Test
    void refreshToken_returns401_whenTokenIsExpired() throws Exception {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin-exp-ctrl@test.com")
                .build());

        JwtTokenService expiredService = new JwtTokenService(
                "tusViajesSuperSecretKeyForJwtSigningMustBeAtLeast256BitsLong2026!",
                86400000L,
                -10000L
        );
        String expiredRefreshToken = expiredService.generateToken(admin, true);

        String json = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(expiredRefreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid refresh token."));
    }

    @Test
    void refreshToken_returns200AndNewTokens_whenRefreshTokenIsValid() throws Exception {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin-valid-ctrl@test.com")
                .build());

        String refreshToken = jwtTokenService.generateToken(admin, true);

        String json = """
                {
                    "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin-valid-ctrl@test.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
