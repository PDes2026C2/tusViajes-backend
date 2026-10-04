package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AdminBuilder;
import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.AgencyRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.BuyerRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.LoginRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.RefreshTokenRequest;
import ar.edu.unq.tusViajes.model.Admin;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import ar.edu.unq.tusViajes.repository.AdminRepository;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.security.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private ObjectMapper objectMapper = new ObjectMapper();
    @Test
    void login_returns200AndToken_whenAdminIsValid() throws Exception {
        adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword123"))
                .build());

        LoginRequestDTO request = new LoginRequestDTO("admin@test.com", "secretPassword123");
        String json = objectMapper.writeValueAsString(request);

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

        LoginRequestDTO request = new LoginRequestDTO("pending@agency.com", "secretPassword123");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("La agencia está pendiente de autorización por parte de un administrador."))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void login_returns200AndToken_whenAgencyIsAuthorized() throws Exception {
        agencyRepository.save(AgencyBuilder.anAgency()
                .withEmail("authorized@agency.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword123"))
                .withStatus(AgencyStatus.AUTHORIZED)
                .build());

        LoginRequestDTO request = new LoginRequestDTO("authorized@agency.com", "secretPassword123");
        String json = objectMapper.writeValueAsString(request);

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

        LoginRequestDTO request = new LoginRequestDTO("admin@test.com", "wrongPassword");
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales invalidas"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void registerAgency_returns201AndPendingStatus() throws Exception {
        AgencyRegistrationRequestDTO request = AgencyRegistrationRequestDTO.builder()
                .email("new@agency.com")
                .taxId("30-55667788-9")
                    .businessName("New Agency SA")
                .password("securePassword123")
                .build();
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/register/agency")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.businessName").value("New Agency SA"))
                .andExpect(jsonPath("$.message").value("Tu postulación ha sido enviada con éxito. Un administrador revisará tu solicitud."));
    }

    @Test
    void registerBuyer_returns201WithTokens() throws Exception {
        BuyerRegistrationRequestDTO request = BuyerRegistrationRequestDTO
                .builder()
                .firstName("Agustin")
                .lastName("Perez")
                .email("agustin@buyer.com")
                .password("securePassword123")
                .phoneNumber("123456789")
                .nationalId("12345678")
                .build();
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/register/buyer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("agustin@buyer.com"))
                .andExpect(jsonPath("$.role").value("BUYER"))
                .andExpect(jsonPath("$.name").value("Agustin Perez"));
    }

    @Test
    void refreshToken_returns401_whenTokenIsAccessToken() throws Exception {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin-access-ctrl@test.com")
                .build());

        String accessToken = jwtTokenService.generateToken(admin, false);

        RefreshTokenRequest request = new RefreshTokenRequest(accessToken);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Token de refresco inválido."))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
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

        RefreshTokenRequest request = new RefreshTokenRequest(expiredRefreshToken);
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Token de refresco inválido."))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void refreshToken_returns200AndNewTokens_whenRefreshTokenIsValid() throws Exception {
        Admin admin = adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin-valid-ctrl@test.com")
                .build());

        String refreshToken = jwtTokenService.generateToken(admin, true);

        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        String json = objectMapper.writeValueAsString(request);

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
