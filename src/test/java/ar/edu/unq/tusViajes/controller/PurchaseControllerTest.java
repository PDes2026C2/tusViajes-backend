package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.*;
import ar.edu.unq.tusViajes.model.*;
import ar.edu.unq.tusViajes.repository.*;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ar.edu.unq.tusViajes.service.FlightsApiService;
import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PurchaseControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

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
    @Autowired
    private PurchaseRepository purchaseRepository;

    @MockitoBean
    private FlightsApiService flightsApiService;

    @Test
    void purchase_returns201AndPersistsPurchase() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City dest = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight dep = flightRepository.save(FlightBuilder.aFlight().withId(100L).withOriginCity(origin).withDestinationCity(dest).build());
        Flight ret = flightRepository.save(FlightBuilder.aFlight().withId(101L).withOriginCity(dest).withDestinationCity(origin).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(dest).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage tp = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withPrice(300000.0)
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(dep)
                .withReturnFlight(ret)
                .build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().withNationalId("40123456").withFirstName("Ana").withLastName("Gomez").build());

        when(flightsApiService.sellFlight(any(), any(PassengerDTO.class))).thenReturn(null);

        mockMvc.perform(post("/api/purchases/" + tp.getId())
                        .with(user(new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(),
                                createAuthorityList("ROLE_BUYER"), true))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.price").value(300000.0))
                .andExpect(jsonPath("$.buyerId").value(buyer.getId()))
                .andExpect(jsonPath("$.travelPackage.id").value(tp.getId()))
                .andExpect(jsonPath("$.purchasedAt").isNotEmpty());

        assertThat(purchaseRepository.findByBuyerId(buyer.getId())).hasSize(1);
        assertThat(purchaseRepository.findByBuyerId(buyer.getId()).get(0).getPrice()).isEqualTo(300000.0);
        verify(flightsApiService).sellFlight(eq(100L), any(PassengerDTO.class));
        verify(flightsApiService).sellFlight(eq(101L), any(PassengerDTO.class));
    }

    @Test
    void purchase_returns404WhenTravelPackageNotFound() throws Exception {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        when(flightsApiService.sellFlight(any(), any())).thenReturn(null);

        mockMvc.perform(post("/api/purchases/99999")
                        .with(user(new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(),
                                createAuthorityList("ROLE_BUYER"), true))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Paquete de viaje con id 99999 no encontrado"));
    }

    @Test
    void purchase_returns401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/purchases/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void purchase_returns403WhenNotBuyer() throws Exception {
        mockMvc.perform(post("/api/purchases/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/purchases/1")
                        .with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void purchase_returns400WhenNationalIdNotNumeric() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City dest = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight dep = flightRepository.save(FlightBuilder.aFlight().withId(100L).withOriginCity(origin).withDestinationCity(dest).build());
        Flight ret = flightRepository.save(FlightBuilder.aFlight().withId(101L).withOriginCity(dest).withDestinationCity(origin).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(dest).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage tp = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withPrice(200000.0)
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(dep)
                .withReturnFlight(ret)
                .build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().withNationalId("ABC12345").build());
        when(flightsApiService.sellFlight(any(), any())).thenReturn(null);

        mockMvc.perform(post("/api/purchases/" + tp.getId())
                        .with(user(new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(),
                                createAuthorityList("ROLE_BUYER"), true))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El DNI del comprador debe ser numérico: ABC12345"));
    }
}
