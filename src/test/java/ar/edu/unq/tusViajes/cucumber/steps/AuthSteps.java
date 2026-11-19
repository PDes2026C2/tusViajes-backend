package ar.edu.unq.tusViajes.cucumber.steps;

import ar.edu.unq.tusViajes.controller.dto.request.AgencyRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.BuyerRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.LoginRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.RefreshTokenRequest;
import ar.edu.unq.tusViajes.cucumber.ScenarioContext;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.repository.BuyerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RequiredArgsConstructor
public class AuthSteps {

    private final MockMvc mockMvc;
    private final ScenarioContext scenarioContext;
    private final BuyerRepository buyerRepository;
    private final AgencyRepository agencyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Cuando("un visitante se registra como comprador con nombre {string}, apellido {string}, email {string}, contraseña {string}, teléfono {string} y DNI {string}")
    public void unVisitanteSeRegistraComoComprador(String firstName, String lastName, String email,
                                                   String password, String phone, String nationalId) throws Exception {
        BuyerRegistrationRequestDTO request = BuyerRegistrationRequestDTO.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(password)
                .phoneNumber(phone)
                .nationalId(nationalId)
                .build();

        ResultActions actions = mockMvc.perform(post("/api/auth/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        scenarioContext.recordResult(actions);
    }

    @Cuando("una empresa se postula como agencia con razón social {string}, CUIT {string}, email {string} y contraseña {string}")
    public void unaEmpresaSePostulaComoAgencia(String businessName, String taxId, String email, String password) throws Exception {
        AgencyRegistrationRequestDTO request = AgencyRegistrationRequestDTO.builder()
                .businessName(businessName)
                .taxId(taxId)
                .email(email)
                .password(password)
                .build();

        ResultActions actions = mockMvc.perform(post("/api/auth/register/agency")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        scenarioContext.recordResult(actions);
    }

    @Cuando("el usuario inicia sesión con email {string} y contraseña {string}")
    @Dado("el usuario inició sesión con email {string} y contraseña {string}")
    public void elUsuarioIniciaSesion(String email, String password) throws Exception {
        LoginRequestDTO request = new LoginRequestDTO(email, password);

        ResultActions actions = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        scenarioContext.recordResult(actions);

        String responseBody = scenarioContext.getLastMvcResult().getResponse().getContentAsString();
        if (scenarioContext.getLastMvcResult().getResponse().getStatus() == 200 && !responseBody.isBlank()) {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.hasNonNull("token")) {
                scenarioContext.setLastAccessToken(jsonNode.get("token").asText());
            }
            if (jsonNode.hasNonNull("refreshToken")) {
                scenarioContext.setLastRefreshToken(jsonNode.get("refreshToken").asText());
            }
        }
    }

    @Cuando("solicita refrescar su sesión utilizando su refresh token")
    public void solicitaRefrescarSuSesion() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest(scenarioContext.getLastRefreshToken());

        ResultActions actions = mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        scenarioContext.recordResult(actions);
    }

    @Entonces("la respuesta contiene un token de acceso JWT válido para el rol {string}")
    public void laRespuestaContieneUnTokenDeAccesoValidoParaElRol(String expectedRole) throws Exception {
        scenarioContext.getLastResultActions()
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value(expectedRole));
    }

    @Entonces("el comprador con email {string} existe en la base de datos con DNI {string}")
    public void elCompradorConEmailExisteEnLaBaseDeDatosConDni(String email, String expectedDni) {
        Buyer buyer = buyerRepository.findAll().stream()
                .filter(b -> b.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el comprador con email " + email));
        assertThat(buyer.getNationalId()).isEqualTo(expectedDni);
    }

    @Entonces("la agencia con email {string} existe en la base de datos con estado {string}")
    public void laAgenciaConEmailExisteEnLaBaseDeDatosConEstado(String email, String expectedStatus) {
        Agency agency = agencyRepository.findAll().stream()
                .filter(a -> a.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró la agencia con email " + email));
        assertThat(agency.getStatus()).isEqualTo(AgencyStatus.valueOf(expectedStatus));
    }
}
