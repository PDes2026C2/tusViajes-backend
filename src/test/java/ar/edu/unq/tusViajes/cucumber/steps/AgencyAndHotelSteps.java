package ar.edu.unq.tusViajes.cucumber.steps;

import ar.edu.unq.tusViajes.controller.dto.request.HotelRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.UpdateAgencyRequestDTO;
import ar.edu.unq.tusViajes.cucumber.ScenarioContext;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.util.TestSecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RequiredArgsConstructor
public class AgencyAndHotelSteps {

    private final MockMvc mockMvc;
    private final ScenarioContext scenarioContext;
    private final AgencyRepository agencyRepository;
    private final HotelRepository hotelRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Cuando("el administrador consulta las agencias pendientes")
    public void elAdministradorConsultaLasAgenciasPendientes() throws Exception {
        ResultActions actions = mockMvc.perform(get("/api/admin/agencies/pending")
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser())));
        scenarioContext.recordResult(actions);
    }

    @Entonces("la lista de agencias devuelta contiene a {string}")
    public void laListaDeAgenciasDevueltaContieneA(String businessName) throws Exception {
        scenarioContext.getLastResultActions()
                .andExpect(jsonPath("$[*].businessName", hasItem(businessName)));
    }

    @Cuando("el administrador autoriza a la agencia {string}")
    public void elAdministradorAutorizaALaAgencia(String businessName) throws Exception {
        Agency agency = scenarioContext.getAgencies().get(businessName);
        ResultActions actions = mockMvc.perform(post("/api/admin/agencies/" + agency.getId() + "/authorize")
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser())));
        scenarioContext.recordResult(actions);
    }

    @Cuando("el administrador rechaza a la agencia {string}")
    public void elAdministradorRechazaALaAgencia(String businessName) throws Exception {
        Agency agency = scenarioContext.getAgencies().get(businessName);
        ResultActions actions = mockMvc.perform(post("/api/admin/agencies/" + agency.getId() + "/reject")
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser())));
        scenarioContext.recordResult(actions);
    }

    @Cuando("la agencia autenticada actualiza su razón social a {string}")
    public void laAgenciaAutenticadaActualizaSuRazonSocialA(String newBusinessName) throws Exception {
        UpdateAgencyRequestDTO dto = new UpdateAgencyRequestDTO(newBusinessName);
        ResultActions actions = mockMvc.perform(put("/api/agencies/me")
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
        scenarioContext.recordResult(actions);
    }

    @Entonces("la agencia con email {string} tiene la razón social {string} en la base de datos")
    public void laAgenciaConEmailTieneLaRazonSocialEnLaBaseDeDatos(String email, String expectedBusinessName) {
        Agency agency = agencyRepository.findAll().stream()
                .filter(a -> a.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró la agencia con email " + email));
        assertThat(agency.getBusinessName()).isEqualTo(expectedBusinessName);
    }

    @Cuando("el administrador registra un hotel llamado {string} en la ciudad {string} con servicios {string}")
    public void elAdministradorRegistraUnHotelLlamado(String hotelName, String cityName, String services) throws Exception {
        City city = scenarioContext.getCities().get(cityName);
        HotelRequestDTO dto = HotelRequestDTO.builder()
                .name(hotelName)
                .cityId(city.getId())
                .photoUrl("https://example.com/hotel.jpg")
                .services(services)
                .build();

        ResultActions actions = mockMvc.perform(post("/api/hotels")
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
        scenarioContext.recordResult(actions);
    }

    @Entonces("el hotel {string} se encuentra persistido en la base de datos asociado a la ciudad {string}")
    public void elHotelSeEncuentraPersistidoEnLaBaseDeDatos(String hotelName, String cityName) {
        City expectedCity = scenarioContext.getCities().get(cityName);
        Hotel hotel = hotelRepository.findAll().stream()
                .filter(h -> h.getName().equals(hotelName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el hotel " + hotelName));
        assertThat(hotel.getCity().getId()).isEqualTo(expectedCity.getId());
    }
}
