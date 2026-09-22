package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PurchaseResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.BuyerRepository;
import ar.edu.unq.tusViajes.repository.PurchaseRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import ar.edu.unq.tusViajes.builder.BuyerBuilder;
import ar.edu.unq.tusViajes.builder.FlightBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.model.Flight;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private BuyerRepository buyerRepository;
    @Mock
    private TravelPackageRepository travelPackageRepository;
    @Mock
    private FlightsApiService flightsApiService;
    @Mock
    private EntityValidator entityValidator;

    @InjectMocks
    private PurchaseService purchaseService;

    @Test
    void purchase_createsPurchaseAndCallsFlightsApiTwice() {
        Buyer buyer = BuyerBuilder.aBuyer().withFirstName("Ana").withLastName("Gomez").withNationalId("40123456").build();
        Country country = new Country("AR", "Argentina");
        City originCity = new City(1L, "Buenos Aires", country);
        City destCity = new City(2L, "Bariloche", country);
        Flight departureFlight = FlightBuilder.aFlight().withId(10L).withOriginCity(originCity).withDestinationCity(destCity).build();
        Flight returnFlight = FlightBuilder.aFlight().withId(11L).withOriginCity(destCity).withDestinationCity(originCity).build();
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withId(99L)
                .withPrice(250000.0)
                .withDepartureFlight(departureFlight)
                .withReturnFlight(returnFlight)
                .build();

        when(entityValidator.findByIdOrThrow(buyerRepository, 1L, "Comprador")).thenReturn(buyer);
        when(travelPackageRepository.findById(99L)).thenReturn(Optional.of(travelPackage));
        when(purchaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PurchaseResponseDTO result = purchaseService.purchase(1L, 99L);

        assertThat(result.price()).isEqualTo(250000.0);
        assertThat(result.purchasedAt()).isNotNull();
        assertThat(buyer.getTravelPackagesPurchased()).hasSize(1);
        assertThat(buyer.hasAcquired(travelPackage)).isTrue();
        verify(flightsApiService).sellFlight(eq(10L), any(PassengerDTO.class));
        verify(flightsApiService).sellFlight(eq(11L), any(PassengerDTO.class));

        ArgumentCaptor<PassengerDTO> captor = ArgumentCaptor.forClass(PassengerDTO.class);
        verify(flightsApiService).sellFlight(eq(10L), captor.capture());
        assertThat(captor.getValue().dni()).isEqualTo(40123456);
        assertThat(captor.getValue().name()).isEqualTo("Ana");
    }

    @Test
    void purchase_throwsWhenTravelPackageNotFound() {
        Buyer buyer = BuyerBuilder.aBuyer().build();
        when(entityValidator.findByIdOrThrow(buyerRepository, 1L, "Comprador")).thenReturn(buyer);
        when(travelPackageRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.purchase(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Paquete de viaje con id 999 no encontrado");

        verifyNoInteractions(flightsApiService);
        verifyNoInteractions(purchaseRepository);
    }

    @Test
    void purchase_throwsWhenPriceIsNull() {
        Buyer buyer = BuyerBuilder.aBuyer().build();
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().withPrice(null).build();
        when(entityValidator.findByIdOrThrow(buyerRepository, 1L, "Comprador")).thenReturn(buyer);
        when(travelPackageRepository.findById(1L)).thenReturn(Optional.of(travelPackage));

        assertThatThrownBy(() -> purchaseService.purchase(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El precio del paquete de viaje no puede ser nulo");
    }

    @Test
    void purchase_throwsWhenNationalIdNotNumeric() {
        Buyer buyer = BuyerBuilder.aBuyer().withNationalId("ABC12345").build();
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().build();
        when(entityValidator.findByIdOrThrow(buyerRepository, 1L, "Comprador")).thenReturn(buyer);
        when(travelPackageRepository.findById(1L)).thenReturn(Optional.of(travelPackage));

        assertThatThrownBy(() -> purchaseService.purchase(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El DNI del comprador debe ser numérico");
    }

    @Test
    void purchase_throwsWhenBuyerAlreadyAcquiredPackage() {
        Buyer buyer = BuyerBuilder.aBuyer().build();
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().withId(10L).build();
        buyer.buy(travelPackage);

        when(entityValidator.findByIdOrThrow(buyerRepository, 1L, "Comprador")).thenReturn(buyer);
        when(travelPackageRepository.findById(10L)).thenReturn(Optional.of(travelPackage));

        assertThatThrownBy(() -> purchaseService.purchase(1L, 10L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El comprador ya adquirió este paquete de viaje");

        verifyNoInteractions(flightsApiService);
        verifyNoInteractions(purchaseRepository);
    }

    @Test
    void purchase_throwsWhenTravelPackageHasEnded() {
        Buyer buyer = BuyerBuilder.aBuyer().build();
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withId(10L)
                .withEndDate(java.time.LocalDateTime.now().minusDays(1))
                .build();

        when(entityValidator.findByIdOrThrow(buyerRepository, 1L, "Comprador")).thenReturn(buyer);
        when(travelPackageRepository.findById(10L)).thenReturn(Optional.of(travelPackage));

        assertThatThrownBy(() -> purchaseService.purchase(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se puede comprar un paquete de viaje que ya ha finalizado");

        verifyNoInteractions(flightsApiService);
        verifyNoInteractions(purchaseRepository);
    }
}
