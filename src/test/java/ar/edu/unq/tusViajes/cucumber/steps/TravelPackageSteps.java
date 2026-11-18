package ar.edu.unq.tusViajes.cucumber.steps;

import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.UpdateTravelPackageRequestDTO;
import ar.edu.unq.tusViajes.cucumber.ScenarioContext;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import ar.edu.unq.tusViajes.util.TestSecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RequiredArgsConstructor
public class TravelPackageSteps {

    private final MockMvc mockMvc;
    private final ScenarioContext scenarioContext;
    private final TravelPackageRepository travelPackageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Dado("la agencia {string} tiene publicado el paquete {string} con precio {int}, hotel {string}, vuelo de ida {long} y vuelo de vuelta {long} que inicia en {int} días y finaliza en {int} días")
    public void laAgenciaTienePublicadoElPaqueteFuturo(String agencyName, String packageName, int price,
                                                       String hotelName, Long depFlightId, Long retFlightId,
                                                       int startDays, int endDays) {
        savePackage(agencyName, packageName, (double) price, hotelName, depFlightId, retFlightId,
                LocalDateTime.now().plusDays(startDays), LocalDateTime.now().plusDays(endDays));
    }

    @Dado("que la agencia {string} tiene publicado el paquete {string} con precio {int}, hotel {string}, vuelo de ida {long} y vuelo de vuelta {long} que inició hace {int} días y finaliza en {int} días")
    @Dado("la agencia {string} tiene publicado el paquete {string} con precio {int}, hotel {string}, vuelo de ida {long} y vuelo de vuelta {long} que inició hace {int} días y finaliza en {int} días")
    public void laAgenciaTienePublicadoElPaqueteYaIniciado(String agencyName, String packageName, int price,
                                                           String hotelName, Long depFlightId, Long retFlightId,
                                                           int startedDaysAgo, int endDays) {
        savePackage(agencyName, packageName, (double) price, hotelName, depFlightId, retFlightId,
                LocalDateTime.now().minusDays(startedDaysAgo), LocalDateTime.now().plusDays(endDays));
    }

    @Dado("que la agencia {string} tiene publicado el paquete {string} con precio {int}, hotel {string}, vuelo de ida {long} y vuelo de vuelta {long} que inició hace {int} días y finalizó hace {int} días")
    @Dado("la agencia {string} tiene publicado el paquete {string} con precio {int}, hotel {string}, vuelo de ida {long} y vuelo de vuelta {long} que inició hace {int} días y finalizó hace {int} días")
    public void laAgenciaTienePublicadoElPaqueteYaFinalizado(String agencyName, String packageName, int price,
                                                             String hotelName, Long depFlightId, Long retFlightId,
                                                             int startedDaysAgo, int endedDaysAgo) {
        savePackage(agencyName, packageName, (double) price, hotelName, depFlightId, retFlightId,
                LocalDateTime.now().minusDays(startedDaysAgo), LocalDateTime.now().minusDays(endedDaysAgo));
    }

    private void savePackage(String agencyName, String packageName, Double price, String hotelName,
                             Long depFlightId, Long retFlightId, LocalDateTime startDate, LocalDateTime endDate) {
        Agency agency = scenarioContext.getAgencies().get(agencyName);
        Hotel hotel = scenarioContext.getHotels().get(hotelName);
        Flight depFlight = scenarioContext.getFlights().get(String.valueOf(depFlightId));
        Flight retFlight = scenarioContext.getFlights().get(String.valueOf(retFlightId));

        TravelPackage travelPackage = travelPackageRepository.save(
                TravelPackageBuilder.aTravelPackage()
                        .withName(packageName)
                        .withDescription("Descripción de " + packageName)
                        .withPrice(price)
                        .withStartDate(startDate)
                        .withEndDate(endDate)
                        .withHotel(hotel)
                        .withAgency(agency)
                        .withDepartureFlight(depFlight)
                        .withReturnFlight(retFlight)
                        .build()
        );
        scenarioContext.getTravelPackages().put(packageName, travelPackage);
    }

    @Cuando("la agencia crea un paquete llamado {string} con precio {int}, hotel {string}, vuelo de ida {long} y vuelo de vuelta {long} que inicia en {int} días y finaliza en {int} días")
    public void laAgenciaCreaUnPaquete(String packageName, int price, String hotelName,
                                       Long depFlightId, Long retFlightId, int startDays, int endDays) throws Exception {
        Hotel hotel = scenarioContext.getHotels().get(hotelName);
        TravelPackageRequestDTO dto = TravelPackageRequestDTO.builder()
                .name(packageName)
                .description("Paquete turístico completo")
                .price((double) price)
                .startDate(LocalDateTime.now().plusDays(startDays))
                .endDate(LocalDateTime.now().plusDays(endDays))
                .hotelId(hotel.getId())
                .departureFlightId(depFlightId)
                .returnFlightId(retFlightId)
                .build();

        ResultActions actions = mockMvc.perform(post("/api/travel-packages")
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        scenarioContext.recordResult(actions);
    }

    @Cuando("la agencia autenticada actualiza el paquete {string} al nuevo nombre {string} y precio {int}")
    public void laAgenciaAutenticadaActualizaElPaquete(String currentName, String newName, int newPrice) throws Exception {
        TravelPackage tp = scenarioContext.getTravelPackages().get(currentName);
        UpdateTravelPackageRequestDTO dto = UpdateTravelPackageRequestDTO.builder()
                .id(tp.getId())
                .name(newName)
                .description("Descripción actualizada")
                .price((double) newPrice)
                .startDate(tp.getStartDate())
                .endDate(tp.getEndDate())
                .hotelId(tp.getHotel().getId())
                .departureFlightId(tp.getDepartureFlight().getId())
                .returnFlightId(tp.getReturnFlight().getId())
                .build();

        ResultActions actions = mockMvc.perform(put("/api/travel-packages/" + tp.getId())
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        scenarioContext.recordResult(actions);
    }

    @Cuando("la agencia autenticada intenta eliminar el paquete {string}")
    @Cuando("la agencia autenticada elimina su paquete {string}")
    public void laAgenciaAutenticadaEliminaElPaquete(String packageName) throws Exception {
        TravelPackage travelPackage = scenarioContext.getTravelPackages().get(packageName);
        ResultActions actions = mockMvc.perform(delete("/api/travel-packages/" + travelPackage.getId())
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser())));
        scenarioContext.recordResult(actions);
    }

    @Cuando("un visitante busca paquetes de viaje sin filtros")
    public void unVisitanteBuscaPaquetesSinFiltros() throws Exception {
        ResultActions actions = mockMvc.perform(get("/api/travel-packages"));
        scenarioContext.recordResult(actions);
    }

    @Cuando("un visitante busca paquetes de viaje con código ISO de país de destino {string}")
    public void unVisitanteBuscaPaquetesConPaisDestino(String destinationCountryIso) throws Exception {
        ResultActions actions = mockMvc.perform(get("/api/travel-packages")
                .param("destinationCountryIso", destinationCountryIso));
        scenarioContext.recordResult(actions);
    }

    @Entonces("el paquete {string} existe activo en la base de datos con precio {int}")
    public void elPaqueteExisteActivoEnLaBaseDeDatosConPrecio(String packageName, int expectedPrice) {
        TravelPackage tp = travelPackageRepository.findAll().stream()
                .filter(p -> p.getName().equals(packageName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el paquete " + packageName));
        assertThat(tp.isActive()).isTrue();
        assertThat(tp.getPrice()).isEqualTo((double) expectedPrice);
    }

    @Entonces("el paquete {string} sigue activo en la base de datos")
    public void elPaqueteSigueActivoEnLaBaseDeDatos(String packageName) {
        TravelPackage tp = travelPackageRepository.findAll().stream()
                .filter(p -> p.getName().equals(packageName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el paquete " + packageName));
        assertThat(tp.isActive()).isTrue();
    }

    @Entonces("el paquete {string} existe inactivo en la base de datos")
    public void elPaqueteExisteInactivoEnLaBaseDeDatos(String packageName) {
        TravelPackage tp = travelPackageRepository.findAll().stream()
                .filter(p -> p.getName().equals(packageName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el paquete " + packageName));
        assertThat(tp.isActive()).isFalse();
    }

    @Entonces("la página de paquetes devuelta incluye a {string}")
    public void laPaginaDePaquetesDevueltaIncluyeA(String packageName) throws Exception {
        scenarioContext.getLastResultActions()
                .andExpect(jsonPath("$.content[*].name", hasItem(packageName)));
    }

    @Entonces("la página de paquetes devuelta no incluye a {string}")
    public void laPaginaDePaquetesDevueltaNoIncluyeA(String packageName) throws Exception {
        scenarioContext.getLastResultActions()
                .andExpect(jsonPath("$.content[*].name", not(hasItem(packageName))));
    }
}
