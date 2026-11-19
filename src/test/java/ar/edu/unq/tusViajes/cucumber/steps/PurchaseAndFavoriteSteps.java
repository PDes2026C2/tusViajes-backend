package ar.edu.unq.tusViajes.cucumber.steps;

import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;
import ar.edu.unq.tusViajes.cucumber.ScenarioContext;
import ar.edu.unq.tusViajes.exception.FlightFullException;
import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.Purchase;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.BuyerRepository;
import ar.edu.unq.tusViajes.repository.PurchaseRepository;
import ar.edu.unq.tusViajes.service.FlightsApiService;
import ar.edu.unq.tusViajes.util.TestSecurityUtils;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import lombok.RequiredArgsConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RequiredArgsConstructor
public class PurchaseAndFavoriteSteps {

    private final MockMvc mockMvc;
    private final ScenarioContext scenarioContext;
    private final BuyerRepository buyerRepository;
    private final PurchaseRepository purchaseRepository;
    private final FlightsApiService flightsApiService;
    private final TransactionTemplate transactionTemplate;

    @Cuando("el comprador agrega el paquete {string} a sus favoritos")
    public void elCompradorAgregaElPaqueteASusFavoritos(String packageName) throws Exception {
        TravelPackage tp = scenarioContext.getTravelPackages().get(packageName);
        ResultActions actions = mockMvc.perform(post("/api/buyers/me/favorites/" + tp.getId())
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser())));
        scenarioContext.recordResult(actions);
    }

    @Cuando("el comprador consulta su lista de paquetes favoritos")
    public void elCompradorConsultaSuListaDePaquetesFavoritos() throws Exception {
        ResultActions actions = mockMvc.perform(get("/api/buyers/me/favorites")
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser())));
        scenarioContext.recordResult(actions);
    }

    @Cuando("el comprador elimina el paquete {string} de sus favoritos")
    public void elCompradorEliminaElPaqueteDeSusFavoritos(String packageName) throws Exception {
        TravelPackage tp = scenarioContext.getTravelPackages().get(packageName);
        ResultActions actions = mockMvc.perform(delete("/api/buyers/me/favorites/" + tp.getId())
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser())));
        scenarioContext.recordResult(actions);
    }

    @Entonces("la lista de favoritos devuelta contiene el paquete {string}")
    public void laListaDeFavoritosDevueltaContieneElPaquete(String packageName) throws Exception {
        scenarioContext.getLastResultActions()
                .andExpect(jsonPath("$[*].name", hasItem(packageName)));
    }

    @Entonces("la lista de favoritos devuelta está vacía")
    public void laListaDeFavoritosDevueltaEstaVacia() throws Exception {
        scenarioContext.getLastResultActions()
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Dado("que el servicio externo de vuelos tiene cupo disponible para todos los vuelos")
    public void queElServicioExternoDeVuelosTieneCupoDisponible() {
        when(flightsApiService.sellFlight(any(), any(PassengerDTO.class))).thenReturn(null);
    }

    @Dado("que el vuelo de ida {long} tiene cupo pero el vuelo de vuelta {long} está completo")
    public void queElVueloDeIdaTieneCupoPeroElDeVueltaEstaCompleto(Long depFlightId, Long retFlightId) {
        when(flightsApiService.sellFlight(eq(depFlightId), any(PassengerDTO.class))).thenReturn(null);
        when(flightsApiService.sellFlight(eq(retFlightId), any(PassengerDTO.class)))
                .thenThrow(new FlightFullException("Uno de los vuelos asociados no cuenta con cupo disponible"));
    }

    @Dado("el comprador ya adquirió el paquete {string}")
    public void elCompradorYaAdquirioElPaquete(String packageName) throws Exception {
        elCompradorAdquiereElPaquete(packageName);
    }

    @Cuando("el comprador adquiere el paquete {string}")
    public void elCompradorAdquiereElPaquete(String packageName) throws Exception {
        TravelPackage tp = scenarioContext.getTravelPackages().get(packageName);
        ResultActions actions = mockMvc.perform(post("/api/purchases/" + tp.getId())
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser())));
        scenarioContext.recordResult(actions);
    }

    @Entonces("se registra en la base de datos la compra del paquete {string} para el comprador {string} por un monto de {int}")
    public void seRegistraEnLaBaseDeDatosLaCompra(String packageName, String buyerEmail, int expectedPrice) {
        Buyer buyer = scenarioContext.getBuyers().get(buyerEmail);
        TravelPackage tp = scenarioContext.getTravelPackages().get(packageName);

        transactionTemplate.executeWithoutResult(status -> {
            List<Purchase> purchases = purchaseRepository.findByBuyerId(buyer.getId());
            assertThat(purchases).hasSize(1);
            assertThat(purchases.get(0).getPrice()).isEqualTo((double) expectedPrice);
            assertThat(purchases.get(0).getTravelPackage().getId()).isEqualTo(tp.getId());

            Buyer persistedBuyer = buyerRepository.findById(buyer.getId()).orElseThrow();
            assertThat(persistedBuyer.hasAcquired(tp)).isTrue();
        });
    }

    @Entonces("se emitieron los pasajes en el servicio externo para los vuelos {long} y {long}")
    public void seEmitieronLosPasajesEnElServicioExterno(Long depFlightId, Long retFlightId) {
        verify(flightsApiService).sellFlight(eq(depFlightId), any(PassengerDTO.class));
        verify(flightsApiService).sellFlight(eq(retFlightId), any(PassengerDTO.class));
    }

    @Entonces("se compensó la reserva del vuelo {long} en el servicio externo")
    public void seCompensoLaReservaDelVueloEnElServicioExterno(Long depFlightId) {
        verify(flightsApiService).cancelFlight(eq(depFlightId), any(PassengerDTO.class));
    }

    @Entonces("el comprador {string} no registra compras en la base de datos")
    public void elCompradorNoRegistraComprasEnLaBaseDeDatos(String buyerEmail) {
        Buyer buyer = scenarioContext.getBuyers().get(buyerEmail);
        assertThat(purchaseRepository.findByBuyerId(buyer.getId())).isEmpty();
    }
}
