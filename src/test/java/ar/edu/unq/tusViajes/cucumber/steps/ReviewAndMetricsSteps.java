package ar.edu.unq.tusViajes.cucumber.steps;

import ar.edu.unq.tusViajes.builder.PurchaseBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.ReviewRequestDTO;
import ar.edu.unq.tusViajes.cucumber.ScenarioContext;
import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.Purchase;
import ar.edu.unq.tusViajes.model.Review;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.BuyerRepository;
import ar.edu.unq.tusViajes.repository.PurchaseRepository;
import ar.edu.unq.tusViajes.repository.ReviewRepository;
import ar.edu.unq.tusViajes.util.TestSecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RequiredArgsConstructor
public class ReviewAndMetricsSteps {

    private final MockMvc mockMvc;
    private final ScenarioContext scenarioContext;
    private final BuyerRepository buyerRepository;
    private final PurchaseRepository purchaseRepository;
    private final ReviewRepository reviewRepository;
    private final TransactionTemplate transactionTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Dado("que la compradora {string} registró la compra del paquete {string}")
    public void queLaCompradoraRegistroLaCompraDelPaquete(String buyerEmail, String packageName) {
        Buyer buyer = scenarioContext.getBuyers().get(buyerEmail);
        TravelPackage tp = scenarioContext.getTravelPackages().get(packageName);

        transactionTemplate.executeWithoutResult(status -> {
            Buyer managedBuyer = buyerRepository.findById(buyer.getId()).orElseThrow();
            Purchase purchase = PurchaseBuilder.aPurchase()
                    .withBuyer(managedBuyer)
                    .withTravelPackage(tp)
                    .withPrice(tp.getPrice())
                    .build();
            managedBuyer.addPurchase(purchase);
            purchaseRepository.save(purchase);
            buyerRepository.save(managedBuyer);
        });
    }

    @Cuando("el comprador publica una reseña para el paquete {string} con puntaje {int} y comentario {string}")
    public void elCompradorPublicaUnaResenaParaElPaquete(String packageName, Integer score, String comment) throws Exception {
        TravelPackage tp = scenarioContext.getTravelPackages().get(packageName);
        ReviewRequestDTO dto = new ReviewRequestDTO(score, comment);

        ResultActions actions = mockMvc.perform(post("/api/reviews/" + tp.getId())
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        scenarioContext.recordResult(actions);
    }

    @Entonces("existe en la base de datos una reseña para el paquete {string} con puntaje {int} y comentario {string}")
    public void existeEnLaBaseDeDatosUnaResenaParaElPaquete(String packageName, Integer expectedScore, String expectedComment) {
        TravelPackage tp = scenarioContext.getTravelPackages().get(packageName);
        List<Review> reviews = reviewRepository.findByTravelPackageIdOrderByCreatedAtDesc(tp.getId(), PageRequest.of(0, 10)).getContent();
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getScore()).isEqualTo(expectedScore);
        assertThat(reviews.get(0).getComment()).isEqualTo(expectedComment);
    }

    @Cuando("el administrador consulta el reporte {string}")
    public void elAdministradorConsultaElReporte(String endpoint) throws Exception {
        ResultActions actions = mockMvc.perform(get(endpoint)
                .with(TestSecurityUtils.withCustomUserDetails(scenarioContext.getCurrentUser())));
        scenarioContext.recordResult(actions);
    }

    @Entonces("el primer comprador del ranking tiene email {string} y {int} compra")
    public void elPrimerCompradorDelRankingTieneEmailYCompra(String expectedEmail, int expectedCount) throws Exception {
        scenarioContext.getLastResultActions()
                .andExpect(jsonPath("$[0].buyer.email").value(expectedEmail))
                .andExpect(jsonPath("$[0].purchaseCount").value(expectedCount));
    }

    @Entonces("el primer destino más vendido es {string} con {int} venta")
    public void elPrimerDestinoMasVendidoEsConVenta(String expectedCity, int expectedCount) throws Exception {
        scenarioContext.getLastResultActions()
                .andExpect(jsonPath("$[0].city.name").value(expectedCity))
                .andExpect(jsonPath("$[0].salesCount").value(expectedCount));
    }

    @Entonces("el primer destino mejor calificado es {string} con promedio {int}")
    public void elPrimerDestinoMejorCalificadoEsConPromedio(String expectedCity, int expectedAverage) throws Exception {
        scenarioContext.getLastResultActions()
                .andExpect(jsonPath("$[0].city.name").value(expectedCity))
                .andExpect(jsonPath("$[0].stars").value((double) expectedAverage));
    }

    @Entonces("la primera agencia vendedora es {string} con {int} venta")
    public void laPrimeraAgenciaVendedoraEsConVenta(String expectedBusinessName, int expectedCount) throws Exception {
        scenarioContext.getLastResultActions()
                .andExpect(jsonPath("$[0].agency.businessName").value(expectedBusinessName))
                .andExpect(jsonPath("$[0].sellsCount").value(expectedCount));
    }
}
