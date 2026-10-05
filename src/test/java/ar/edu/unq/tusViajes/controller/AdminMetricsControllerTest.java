package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.*;
import ar.edu.unq.tusViajes.model.*;
import ar.edu.unq.tusViajes.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminMetricsControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BuyerRepository buyerRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private TravelPackageRepository travelPackageRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private FlightRepository flightRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private AgencyRepository agencyRepository;

    @Test
    void getTopBuyers_returnsBuyerDetailsAndPurchaseCount_whenIsAdmin() throws Exception {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer()
                .withFirstName("Ana").withLastName("Gomez").build());
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destination = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departure = flightRepository.save(FlightBuilder.aFlight()
                .withId(100L).withOriginCity(origin).withDestinationCity(destination).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                .withId(101L).withOriginCity(destination).withDestinationCity(origin).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel).withAgency(agency)
                .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(buyer).withTravelPackage(travelPackage).build());

        mockMvc.perform(get("/api/admin/metrics/top-buyers").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].buyer.id").value(buyer.getId()))
                .andExpect(jsonPath("$[0].buyer.firstName").value("Ana"))
                .andExpect(jsonPath("$[0].buyer.lastName").value("Gomez"))
                .andExpect(jsonPath("$[0].buyer.email").value(buyer.getEmail()))
                .andExpect(jsonPath("$[0].buyer.phoneNumber").value(buyer.getPhoneNumber()))
                .andExpect(jsonPath("$[0].buyer.nationalId").value(buyer.getNationalId()))
                .andExpect(jsonPath("$[0].buyer.passwordHash").doesNotExist())
                .andExpect(jsonPath("$[0].purchaseCount").value(1));
    }

    @Test
    void getTopBuyers_returnsOnlyFiveBuyersOrderedByPurchaseCount() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destination = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departure = flightRepository.save(FlightBuilder.aFlight()
                .withId(100L).withOriginCity(origin).withDestinationCity(destination).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                .withId(101L).withOriginCity(destination).withDestinationCity(origin).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel).withAgency(agency)
                .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
        for (int i = 0; i < 7; i++) {
            Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer()
                    .withFirstName("Buyer" + i).withEmail("buyer" + i + "@example.com")
                    .withNationalId(String.valueOf(40000000 + i)).build());
            for (int j = 0; j < i; j++) {
                TravelPackage purchasedPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                        .withName("Package " + i + "-" + j)
                        .withHotel(travelPackage.getHotel()).withAgency(travelPackage.getAgency())
                        .withDepartureFlight(travelPackage.getDepartureFlight())
                        .withReturnFlight(travelPackage.getReturnFlight()).build());
                purchaseRepository.save(PurchaseBuilder.aPurchase()
                        .withBuyer(buyer).withTravelPackage(purchasedPackage).build());
            }
        }

        mockMvc.perform(get("/api/admin/metrics/top-buyers").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].buyer.firstName").value("Buyer6"))
                .andExpect(jsonPath("$[0].purchaseCount").value(6))
                .andExpect(jsonPath("$[1].buyer.firstName").value("Buyer5"))
                .andExpect(jsonPath("$[1].purchaseCount").value(5))
                .andExpect(jsonPath("$[2].purchaseCount").value(4))
                .andExpect(jsonPath("$[3].purchaseCount").value(3))
                .andExpect(jsonPath("$[4].buyer.firstName").value("Buyer2"))
                .andExpect(jsonPath("$[4].purchaseCount").value(2));
    }

    @Test
    void getTopBuyers_ordersTiesByBuyerIdAndExcludesBuyersWithoutPurchases() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destination = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departure = flightRepository.save(FlightBuilder.aFlight()
                .withId(100L).withOriginCity(origin).withDestinationCity(destination).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                .withId(101L).withOriginCity(destination).withDestinationCity(origin).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel).withAgency(agency)
                .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
        Buyer first = buyerRepository.save(BuyerBuilder.aBuyer()
                .withFirstName("Buyer1").withEmail("buyer1@example.com")
                .withNationalId("40000001").build());
        Buyer second = buyerRepository.save(BuyerBuilder.aBuyer()
                .withFirstName("Buyer2").withEmail("buyer2@example.com")
                .withNationalId("40000002").build());
        buyerRepository.save(BuyerBuilder.aBuyer()
                .withFirstName("Buyer3").withEmail("buyer3@example.com")
                .withNationalId("40000003").build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(second).withTravelPackage(travelPackage).build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(first).withTravelPackage(travelPackage).build());

        mockMvc.perform(get("/api/admin/metrics/top-buyers").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].buyer.id").value(first.getId()))
                .andExpect(jsonPath("$[0].purchaseCount").value(1))
                .andExpect(jsonPath("$[1].buyer.id").value(second.getId()))
                .andExpect(jsonPath("$[1].purchaseCount").value(1));
    }

    @Test
    void getTopBuyers_returnsEmptyList_whenThereAreNoPurchases() throws Exception {
        buyerRepository.save(BuyerBuilder.aBuyer()
                .withFirstName("Buyer1").withEmail("buyer1@example.com")
                .withNationalId("40000001").build());

        mockMvc.perform(get("/api/admin/metrics/top-buyers").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTopBuyers_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/top-buyers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTopBuyers_returns403_whenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/top-buyers").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/metrics/top-buyers").with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTopBuyers_isDocumentedInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-buyers'].get.responses['200']"
                        + ".content['application/json'].schema.type").value("array"))
                .andExpect(jsonPath("$.components.schemas.BuyersTopResponseDTO.properties.buyer['$ref']")
                        .value("#/components/schemas/BuyerResponseDTO"))
                .andExpect(jsonPath("$.components.schemas.BuyersTopResponseDTO.properties.purchaseCount.type")
                        .value("integer"));
    }

}
