package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.FlightsApiClient;
import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;
import ar.edu.unq.tusViajes.builder.*;
import ar.edu.unq.tusViajes.exception.*;
import ar.edu.unq.tusViajes.model.*;
import ar.edu.unq.tusViajes.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Transactional
class PurchaseServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private TravelPackageRepository travelPackageRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @MockitoBean
    private FlightsApiClient flightsApiClient;

    @BeforeEach
    void setUp() {
        reset(flightsApiClient);
    }

    private TravelPackage createAndSaveTravelPackage(Long depFlightId, Long retFlightId, Double price, LocalDateTime startDate) {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires " + depFlightId).withCountry(country).build());
        City dest = cityRepository.save(CityBuilder.aCity().withName("Bariloche " + retFlightId).withCountry(country).build());
        Flight dep = flightRepository.save(FlightBuilder.aFlight().withId(depFlightId).withOriginCity(origin).withDestinationCity(dest).build());
        Flight ret = flightRepository.save(FlightBuilder.aFlight().withId(retFlightId).withOriginCity(dest).withDestinationCity(origin).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(dest).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());

        TravelPackageBuilder builder = TravelPackageBuilder.aTravelPackage()
                .withPrice(price)
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(dep)
                .withReturnFlight(ret);

        if (startDate != null) {
            builder.withStartDate(startDate);
        }

        return travelPackageRepository.save(builder.build());
    }

    private Buyer createAndSaveBuyer(String nationalId, String firstName, String lastName) {
        return buyerRepository.save(BuyerBuilder.aBuyer()
                .withNationalId(nationalId)
                .withFirstName(firstName)
                .withLastName(lastName)
                .withEmail("buyer_" + nationalId + "@test.com")
                .build());
    }

    @Test
    void purchase_createsPurchaseAndCallsFlightsApiTwice() {
        Buyer buyer = createAndSaveBuyer("40123456", "Ana", "Gomez");
        TravelPackage travelPackage = createAndSaveTravelPackage(10L, 11L, 250000.0, null);

        when(flightsApiClient.sellFlight(any(), any(PassengerDTO.class))).thenReturn(null);

        Purchase result = purchaseService.purchase(buyer.getId(), travelPackage.getId());

        assertThat(result.getPrice()).isEqualTo(250000.0);
        assertThat(result.getPurchasedAt()).isNotNull();
        assertThat(result.getBuyer().getId()).isEqualTo(buyer.getId());
        assertThat(result.getTravelPackage().getId()).isEqualTo(travelPackage.getId());

        assertThat(purchaseRepository.findById(result.getId())).isPresent();
        Buyer persistedBuyer = buyerRepository.findById(buyer.getId()).orElseThrow();
        assertThat(persistedBuyer.getTravelPackagesPurchased()).hasSize(1);
        assertThat(persistedBuyer.hasAcquired(travelPackage)).isTrue();

        verify(flightsApiClient).sellFlight(eq(10L), any(PassengerDTO.class));
        verify(flightsApiClient).sellFlight(eq(11L), any(PassengerDTO.class));

        ArgumentCaptor<PassengerDTO> captor = ArgumentCaptor.forClass(PassengerDTO.class);
        verify(flightsApiClient).sellFlight(eq(10L), captor.capture());
        assertThat(captor.getValue().dni()).isEqualTo(40123456);
        assertThat(captor.getValue().name()).isEqualTo("Ana");
        assertThat(captor.getValue().surname()).isEqualTo("Gomez");
    }

    @Test
    void purchase_throwsWhenTravelPackageNotFound() {
        Buyer buyer = createAndSaveBuyer("40123456", "Ana", "Gomez");

        assertThatThrownBy(() -> purchaseService.purchase(buyer.getId(), 99999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Paquete de viaje con id 99999 no encontrado");

        verifyNoInteractions(flightsApiClient);
        assertThat(purchaseRepository.findByBuyerId(buyer.getId())).isEmpty();
    }

    @Test
    void purchase_throwsWhenBuyerNotFound() {
        TravelPackage travelPackage = createAndSaveTravelPackage(20L, 21L, 200000.0, null);

        assertThatThrownBy(() -> purchaseService.purchase(99999L, travelPackage.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comprador con id 99999 no encontrado");

        verifyNoInteractions(flightsApiClient);
    }


    @Test
    void purchase_throwsWhenNationalIdNotNumeric() {
        Buyer buyer = createAndSaveBuyer("ABC12345", "Ana", "Gomez");
        TravelPackage travelPackage = createAndSaveTravelPackage(40L, 41L, 200000.0, null);

        assertThatThrownBy(() -> purchaseService.purchase(buyer.getId(), travelPackage.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El DNI del comprador debe ser numérico");

        verifyNoInteractions(flightsApiClient);
    }

    @Test
    void purchase_throwsWhenBuyerAlreadyAcquiredPackage() {
        Buyer buyer = createAndSaveBuyer("40123456", "Ana", "Gomez");
        TravelPackage travelPackage = createAndSaveTravelPackage(50L, 51L, 200000.0, null);

        when(flightsApiClient.sellFlight(any(), any(PassengerDTO.class))).thenReturn(null);
        purchaseService.purchase(buyer.getId(), travelPackage.getId());

        assertThatThrownBy(() -> purchaseService.purchase(buyer.getId(), travelPackage.getId()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El comprador ya adquirió este paquete de viaje");
    }

    @Test
    void purchase_throwsWhenTravelPackageHasStarted() {
        Buyer buyer = createAndSaveBuyer("40123456", "Ana", "Gomez");
        TravelPackage travelPackage = createAndSaveTravelPackage(60L, 61L, 200000.0, LocalDateTime.now().minusDays(1));

        assertThatThrownBy(() -> purchaseService.purchase(buyer.getId(), travelPackage.getId()))
                .isInstanceOf(PackageAlreadyStartedException.class)
                .hasMessageContaining("No se puede comprar un paquete de viaje que ya ha comenzado");

        verifyNoInteractions(flightsApiClient);
        assertThat(purchaseRepository.findByBuyerId(buyer.getId())).isEmpty();
    }

    @Test
    void purchase_throwsFlightFullExceptionAndCompensatesDepartureFlightWhenReturnFlightIsFull() {
        Buyer buyer = createAndSaveBuyer("40123456", "Ana", "Gomez");
        TravelPackage travelPackage = createAndSaveTravelPackage(70L, 71L, 250000.0, null);

        when(flightsApiClient.sellFlight(eq(70L), any(PassengerDTO.class))).thenReturn(null);
        when(flightsApiClient.sellFlight(eq(71L), any(PassengerDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT, "Flight is full"));

        assertThatThrownBy(() -> purchaseService.purchase(buyer.getId(), travelPackage.getId()))
                .isInstanceOf(FlightFullException.class)
                .hasMessageContaining("Uno de los vuelos asociados no cuenta con cupo disponible");

        verify(flightsApiClient).sellFlight(eq(70L), any(PassengerDTO.class));
        verify(flightsApiClient).sellFlight(eq(71L), any(PassengerDTO.class));
        verify(flightsApiClient).cancelFlight(eq(70L), any(PassengerDTO.class));
        verify(flightsApiClient, never()).cancelFlight(eq(71L), any(PassengerDTO.class));
        assertThat(purchaseRepository.findByBuyerId(buyer.getId())).isEmpty();
    }

    @Test
    void purchase_throwsFlightFullExceptionWhenDepartureFlightIsFull() {
        Buyer buyer = createAndSaveBuyer("40123456", "Ana", "Gomez");
        TravelPackage travelPackage = createAndSaveTravelPackage(80L, 81L, 250000.0, null);

        when(flightsApiClient.sellFlight(eq(80L), any(PassengerDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT, "Flight is full"));

        assertThatThrownBy(() -> purchaseService.purchase(buyer.getId(), travelPackage.getId()))
                .isInstanceOf(FlightFullException.class)
                .hasMessageContaining("Uno de los vuelos asociados no cuenta con cupo disponible");

        verify(flightsApiClient).sellFlight(eq(80L), any(PassengerDTO.class));
        verify(flightsApiClient, never()).sellFlight(eq(81L), any(PassengerDTO.class));
        verify(flightsApiClient, never()).cancelFlight(any(), any());
        assertThat(purchaseRepository.findByBuyerId(buyer.getId())).isEmpty();
    }

    @Test
    void getPurchasesByBuyer_returnsPaginatedPurchases() {
        Buyer buyer = createAndSaveBuyer("40123456", "Ana", "Gomez");
        TravelPackage travelPackage = createAndSaveTravelPackage(90L, 91L, 180000.0, null);

        when(flightsApiClient.sellFlight(any(), any(PassengerDTO.class))).thenReturn(null);
        purchaseService.purchase(buyer.getId(), travelPackage.getId());

        Page<Purchase> page = purchaseService.getPurchasesByBuyer(buyer.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getTravelPackage().getId()).isEqualTo(travelPackage.getId());
    }

    @Test
    void getSalesByAgency_returnsPaginatedSales() {
        Buyer buyer = createAndSaveBuyer("40123456", "Ana", "Gomez");
        TravelPackage travelPackage = createAndSaveTravelPackage(92L, 93L, 220000.0, null);

        when(flightsApiClient.sellFlight(any(), any(PassengerDTO.class))).thenReturn(null);
        purchaseService.purchase(buyer.getId(), travelPackage.getId());

        Long agencyId = travelPackage.getAgency().getId();
        Page<Purchase> page = purchaseService.getSalesByAgency(agencyId, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getTravelPackage().getId()).isEqualTo(travelPackage.getId());
    }
}
