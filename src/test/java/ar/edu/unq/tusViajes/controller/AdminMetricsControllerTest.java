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

import java.util.ArrayList;
import java.util.List;

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
    private ReviewRepository reviewRepository;
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
    void getTopDestinations_returnsCityDetailsAndSalesAcrossPackages_whenIsAdmin() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destination = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departure = flightRepository.save(FlightBuilder.aFlight()
                .withId(100L).withOriginCity(origin).withDestinationCity(destination).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                .withId(101L).withOriginCity(destination).withDestinationCity(origin).build());
        Hotel firstHotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
        Hotel secondHotel = hotelRepository.save(HotelBuilder.aHotel().withName("Otro hotel").withCity(destination).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage firstPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(firstHotel).withAgency(agency)
                .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
        TravelPackage secondPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(secondHotel).withAgency(agency)
                .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
        Buyer firstBuyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        Buyer secondBuyer = buyerRepository.save(BuyerBuilder.aBuyer()
                .withEmail("buyer2@example.com").withNationalId("40000002").build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(firstBuyer).withTravelPackage(firstPackage).build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(secondBuyer).withTravelPackage(firstPackage).build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(firstBuyer).withTravelPackage(secondPackage).build());

        mockMvc.perform(get("/api/admin/metrics/top-destinations").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].city.id").value(destination.getId()))
                .andExpect(jsonPath("$[0].city.name").value("Bariloche"))
                .andExpect(jsonPath("$[0].city.country.isoCode").value(country.getIsoCode()))
                .andExpect(jsonPath("$[0].city.country.name").value(country.getName()))
                .andExpect(jsonPath("$[0].salesCount").value(3));
    }

    @Test
    void getTopDestinations_returnsOnlyFiveCitiesOrderedBySalesCount() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        for (int i = 0; i < 7; i++) {
            City destination = cityRepository.save(CityBuilder.aCity()
                    .withName("City" + i).withCountry(country).build());
            Flight departure = flightRepository.save(FlightBuilder.aFlight()
                    .withId(100L + i * 2).withOriginCity(origin).withDestinationCity(destination).build());
            Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                    .withId(101L + i * 2).withOriginCity(destination).withDestinationCity(origin).build());
            Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
            for (int j = 0; j < i; j++) {
                TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                        .withName("Package " + i + "-" + j).withHotel(hotel).withAgency(agency)
                        .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
                purchaseRepository.save(PurchaseBuilder.aPurchase()
                        .withBuyer(buyer).withTravelPackage(travelPackage).build());
            }
        }

        mockMvc.perform(get("/api/admin/metrics/top-destinations").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].city.name").value("City6"))
                .andExpect(jsonPath("$[0].salesCount").value(6))
                .andExpect(jsonPath("$[1].city.name").value("City5"))
                .andExpect(jsonPath("$[1].salesCount").value(5))
                .andExpect(jsonPath("$[2].city.name").value("City4"))
                .andExpect(jsonPath("$[2].salesCount").value(4))
                .andExpect(jsonPath("$[3].city.name").value("City3"))
                .andExpect(jsonPath("$[3].salesCount").value(3))
                .andExpect(jsonPath("$[4].city.name").value("City2"))
                .andExpect(jsonPath("$[4].salesCount").value(2));
    }

    @Test
    void getTopDestinations_ordersTiesByCityIdAndExcludesCitiesWithoutSales() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        List<City> destinations = new ArrayList<>();
        List<TravelPackage> packages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            City destination = cityRepository.save(CityBuilder.aCity()
                    .withName("City" + i).withCountry(country).build());
            destinations.add(destination);
            Flight departure = flightRepository.save(FlightBuilder.aFlight()
                    .withId(100L + i * 2).withOriginCity(origin).withDestinationCity(destination).build());
            Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                    .withId(101L + i * 2).withOriginCity(destination).withDestinationCity(origin).build());
            Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
            packages.add(travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                    .withHotel(hotel).withAgency(agency)
                    .withDepartureFlight(departure).withReturnFlight(returnFlight).build()));
        }
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(buyer).withTravelPackage(packages.get(1)).build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(buyer).withTravelPackage(packages.get(0)).build());

        mockMvc.perform(get("/api/admin/metrics/top-destinations").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].city.id").value(destinations.get(0).getId()))
                .andExpect(jsonPath("$[0].salesCount").value(1))
                .andExpect(jsonPath("$[1].city.id").value(destinations.get(1).getId()))
                .andExpect(jsonPath("$[1].salesCount").value(1));
    }

    @Test
    void getTopDestinations_returnsEmptyList_whenThereAreNoPurchases() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());

        mockMvc.perform(get("/api/admin/metrics/top-destinations").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTopDestinations_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/top-destinations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Acceso no autorizado")));
    }

    @Test
    void getTopDestinations_returns403_whenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/top-destinations").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Acceso denegado")));
        mockMvc.perform(get("/api/admin/metrics/top-destinations").with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Acceso denegado")));
    }

    @Test
    void getTopRatedDestinations_returnsCityDetailsAndAverageAcrossReviews_whenIsAdmin() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destination = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departure = flightRepository.save(FlightBuilder.aFlight()
                .withId(100L).withOriginCity(origin).withDestinationCity(destination).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                .withId(101L).withOriginCity(destination).withDestinationCity(origin).build());
        Hotel firstHotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
        Hotel secondHotel = hotelRepository.save(HotelBuilder.aHotel().withName("Otro hotel").withCity(destination).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage firstPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(firstHotel).withAgency(agency)
                .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
        TravelPackage secondPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(secondHotel).withAgency(agency)
                .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
        Buyer firstBuyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        Buyer secondBuyer = buyerRepository.save(BuyerBuilder.aBuyer()
                .withEmail("buyer2@example.com").withNationalId("40000002").build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(firstBuyer).withTravelPackage(firstPackage).build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(secondBuyer).withTravelPackage(firstPackage).build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(firstBuyer).withTravelPackage(secondPackage).build());
        reviewRepository.save(ReviewBuilder.aReview()
                .withBuyer(firstBuyer).withTravelPackage(firstPackage).withScore(10).build());
        reviewRepository.save(ReviewBuilder.aReview()
                .withBuyer(secondBuyer).withTravelPackage(firstPackage).withScore(10).build());
        reviewRepository.save(ReviewBuilder.aReview()
                .withBuyer(firstBuyer).withTravelPackage(secondPackage).withScore(2).build());

        mockMvc.perform(get("/api/admin/metrics/top-rated-destinations").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].city.id").value(destination.getId()))
                .andExpect(jsonPath("$[0].city.name").value("Bariloche"))
                .andExpect(jsonPath("$[0].city.country.isoCode").value(country.getIsoCode()))
                .andExpect(jsonPath("$[0].city.country.name").value(country.getName()))
                .andExpect(jsonPath("$[0].stars").value(org.hamcrest.Matchers.closeTo(7.3333333333, 0.0000001)));
    }

    @Test
    void getTopRatedDestinations_returnsOnlyFiveCitiesOrderedByAverageRating() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Buyer firstBuyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        Buyer secondBuyer = buyerRepository.save(BuyerBuilder.aBuyer()
                .withEmail("buyer2@example.com").withNationalId("40000002").build());
        Buyer thirdBuyer = buyerRepository.save(BuyerBuilder.aBuyer()
                .withEmail("buyer3@example.com").withNationalId("40000003").build());
        for (int i = 0; i < 6; i++) {
            City destination = cityRepository.save(CityBuilder.aCity()
                    .withName("City" + i).withCountry(country).build());
            Flight departure = flightRepository.save(FlightBuilder.aFlight()
                    .withId(100L + i * 2).withOriginCity(origin).withDestinationCity(destination).build());
            Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                    .withId(101L + i * 2).withOriginCity(destination).withDestinationCity(origin).build());
            Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
            TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                    .withHotel(hotel).withAgency(agency)
                    .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
            purchaseRepository.save(PurchaseBuilder.aPurchase()
                    .withBuyer(firstBuyer).withTravelPackage(travelPackage).build());
            reviewRepository.save(ReviewBuilder.aReview()
                    .withBuyer(firstBuyer).withTravelPackage(travelPackage).withScore(4 + i).build());
            if (i == 0) {
                purchaseRepository.save(PurchaseBuilder.aPurchase()
                        .withBuyer(secondBuyer).withTravelPackage(travelPackage).build());
                purchaseRepository.save(PurchaseBuilder.aPurchase()
                        .withBuyer(thirdBuyer).withTravelPackage(travelPackage).build());
                reviewRepository.save(ReviewBuilder.aReview()
                        .withBuyer(secondBuyer).withTravelPackage(travelPackage).withScore(4).build());
                reviewRepository.save(ReviewBuilder.aReview()
                        .withBuyer(thirdBuyer).withTravelPackage(travelPackage).withScore(4).build());
            }
        }

        mockMvc.perform(get("/api/admin/metrics/top-rated-destinations").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].city.name").value("City5"))
                .andExpect(jsonPath("$[0].stars").value(9.0))
                .andExpect(jsonPath("$[1].city.name").value("City4"))
                .andExpect(jsonPath("$[1].stars").value(8.0))
                .andExpect(jsonPath("$[2].city.name").value("City3"))
                .andExpect(jsonPath("$[2].stars").value(7.0))
                .andExpect(jsonPath("$[3].city.name").value("City2"))
                .andExpect(jsonPath("$[3].stars").value(6.0))
                .andExpect(jsonPath("$[4].city.name").value("City1"))
                .andExpect(jsonPath("$[4].stars").value(5.0));
    }

    @Test
    void getTopRatedDestinations_ordersTiesByCityIdAndExcludesCitiesWithoutScores() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        List<City> destinations = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            City destination = cityRepository.save(CityBuilder.aCity()
                    .withName("City" + i).withCountry(country).build());
            destinations.add(destination);
            Flight departure = flightRepository.save(FlightBuilder.aFlight()
                    .withId(100L + i * 2).withOriginCity(origin).withDestinationCity(destination).build());
            Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                    .withId(101L + i * 2).withOriginCity(destination).withDestinationCity(origin).build());
            Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
            TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                    .withHotel(hotel).withAgency(agency)
                    .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
            purchaseRepository.save(PurchaseBuilder.aPurchase()
                    .withBuyer(buyer).withTravelPackage(travelPackage).build());
            if (i < 2) {
                reviewRepository.save(ReviewBuilder.aReview()
                        .withBuyer(buyer).withTravelPackage(travelPackage).withScore(0).build());
            } else if (i == 2) {
                reviewRepository.save(ReviewBuilder.aReview()
                        .withBuyer(buyer).withTravelPackage(travelPackage).withScore(null).build());
            }
        }

        mockMvc.perform(get("/api/admin/metrics/top-rated-destinations").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].city.id").value(destinations.get(0).getId()))
                .andExpect(jsonPath("$[0].stars").value(0.0))
                .andExpect(jsonPath("$[1].city.id").value(destinations.get(1).getId()))
                .andExpect(jsonPath("$[1].stars").value(0.0));
    }

    @Test
    void getTopRatedDestinations_returnsEmptyList_whenThereAreNoReviews() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());

        mockMvc.perform(get("/api/admin/metrics/top-rated-destinations").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTopRatedDestinations_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/top-rated-destinations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Acceso no autorizado")));
    }

    @Test
    void getTopRatedDestinations_returns403_whenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/top-rated-destinations").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Acceso denegado")));
        mockMvc.perform(get("/api/admin/metrics/top-rated-destinations").with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Acceso denegado")));
    }

    @Test
    void getTopDestinations_isDocumentedInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-destinations'].get.responses['200']"
                        + ".content['application/json'].schema.type").value("array"))
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-destinations'].get.responses['200']"
                        + ".content['application/json'].schema.items['$ref']")
                        .value("#/components/schemas/DestinationsTopResponseDTO"))
                .andExpect(jsonPath("$.components.schemas.DestinationsTopResponseDTO.properties.city['$ref']")
                        .value("#/components/schemas/CityDTO"))
                .andExpect(jsonPath("$.components.schemas.DestinationsTopResponseDTO.properties.salesCount.type")
                        .value("integer"))
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-destinations'].get.responses['401']"
                        + ".content['application/json'].schema['$ref']").value("#/components/schemas/ErrorDTO"))
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-destinations'].get.responses['403']"
                        + ".content['application/json'].schema['$ref']").value("#/components/schemas/ErrorDTO"))
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray());
    }

    @Test
    void getTopRatedDestinations_isDocumentedInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-rated-destinations'].get.responses['200']"
                        + ".content['application/json'].schema.type").value("array"))
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-rated-destinations'].get.responses['200']"
                        + ".content['application/json'].schema.items['$ref']")
                        .value("#/components/schemas/RatedDestinationsTopResponseDTO"))
                .andExpect(jsonPath("$.components.schemas.RatedDestinationsTopResponseDTO.properties.city['$ref']")
                        .value("#/components/schemas/CityDTO"))
                .andExpect(jsonPath("$.components.schemas.RatedDestinationsTopResponseDTO.properties.stars.type")
                        .value("number"))
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-rated-destinations'].get.responses['401']"
                        + ".content['application/json'].schema['$ref']").value("#/components/schemas/ErrorDTO"))
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-rated-destinations'].get.responses['403']"
                        + ".content['application/json'].schema['$ref']").value("#/components/schemas/ErrorDTO"))
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray());
    }

    @Test
    void getTopAgencies_returnsAgencyDetailsAndSellsCountAcrossPackages_whenIsAdmin() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destination = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departure = flightRepository.save(FlightBuilder.aFlight()
                .withId(100L).withOriginCity(origin).withDestinationCity(destination).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                .withId(101L).withOriginCity(destination).withDestinationCity(origin).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().withBusinessName("Agencia Demo").build());
        TravelPackage firstPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel).withAgency(agency)
                .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
        TravelPackage secondPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Otro paquete").withHotel(hotel).withAgency(agency)
                .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
        Buyer firstBuyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        Buyer secondBuyer = buyerRepository.save(BuyerBuilder.aBuyer()
                .withEmail("buyer2@example.com").withNationalId("40000002").build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(firstBuyer).withTravelPackage(firstPackage).build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(secondBuyer).withTravelPackage(firstPackage).build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(firstBuyer).withTravelPackage(secondPackage).build());

        mockMvc.perform(get("/api/admin/metrics/top-agencies").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].agency.id").value(agency.getId()))
                .andExpect(jsonPath("$[0].agency.businessName").value("Agencia Demo"))
                .andExpect(jsonPath("$[0].agency.email").value(agency.getEmail()))
                .andExpect(jsonPath("$[0].agency.length()").value(3))
                .andExpect(jsonPath("$[0].agency.passwordHash").doesNotExist())
                .andExpect(jsonPath("$[0].agency.taxId").doesNotExist())
                .andExpect(jsonPath("$[0].agency.status").doesNotExist())
                .andExpect(jsonPath("$[0].sellsCount").value(3))
                .andExpect(jsonPath("$[0].salesCount").doesNotExist());
    }

    @Test
    void getTopAgencies_returnsOnlyFiveAgenciesOrderedBySellsCount() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destination = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departure = flightRepository.save(FlightBuilder.aFlight()
                .withId(100L).withOriginCity(origin).withDestinationCity(destination).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                .withId(101L).withOriginCity(destination).withDestinationCity(origin).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        for (int i = 0; i < 7; i++) {
            Agency agency = agencyRepository.save(AgencyBuilder.anAgency()
                    .withBusinessName("Agency" + i).withEmail("agency" + i + "@example.com")
                    .withTaxId("20-" + (12345670 + i) + "-3").build());
            for (int j = 0; j < i; j++) {
                TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                        .withName("Package " + i + "-" + j).withHotel(hotel).withAgency(agency)
                        .withDepartureFlight(departure).withReturnFlight(returnFlight).build());
                purchaseRepository.save(PurchaseBuilder.aPurchase()
                        .withBuyer(buyer).withTravelPackage(travelPackage).build());
            }
        }

        mockMvc.perform(get("/api/admin/metrics/top-agencies").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].agency.businessName").value("Agency6"))
                .andExpect(jsonPath("$[0].sellsCount").value(6))
                .andExpect(jsonPath("$[1].agency.businessName").value("Agency5"))
                .andExpect(jsonPath("$[1].sellsCount").value(5))
                .andExpect(jsonPath("$[2].agency.businessName").value("Agency4"))
                .andExpect(jsonPath("$[2].sellsCount").value(4))
                .andExpect(jsonPath("$[3].agency.businessName").value("Agency3"))
                .andExpect(jsonPath("$[3].sellsCount").value(3))
                .andExpect(jsonPath("$[4].agency.businessName").value("Agency2"))
                .andExpect(jsonPath("$[4].sellsCount").value(2));
    }

    @Test
    void getTopAgencies_ordersTiesByAgencyIdAndExcludesAgenciesWithoutSales() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City origin = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destination = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departure = flightRepository.save(FlightBuilder.aFlight()
                .withId(100L).withOriginCity(origin).withDestinationCity(destination).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight()
                .withId(101L).withOriginCity(destination).withDestinationCity(origin).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(destination).build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        List<Agency> agencies = new ArrayList<>();
        List<TravelPackage> packages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Agency agency = agencyRepository.save(AgencyBuilder.anAgency()
                    .withBusinessName("Agency" + i).withEmail("agency" + i + "@example.com")
                    .withTaxId("20-" + (12345670 + i) + "-3").build());
            agencies.add(agency);
            packages.add(travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                    .withHotel(hotel).withAgency(agency)
                    .withDepartureFlight(departure).withReturnFlight(returnFlight).build()));
        }
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(buyer).withTravelPackage(packages.get(1)).build());
        purchaseRepository.save(PurchaseBuilder.aPurchase()
                .withBuyer(buyer).withTravelPackage(packages.get(0)).build());

        mockMvc.perform(get("/api/admin/metrics/top-agencies").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].agency.id").value(agencies.get(0).getId()))
                .andExpect(jsonPath("$[0].sellsCount").value(1))
                .andExpect(jsonPath("$[1].agency.id").value(agencies.get(1).getId()))
                .andExpect(jsonPath("$[1].sellsCount").value(1));
    }

    @Test
    void getTopAgencies_returnsEmptyList_whenThereAreNoPurchases() throws Exception {
        agencyRepository.save(AgencyBuilder.anAgency().build());

        mockMvc.perform(get("/api/admin/metrics/top-agencies").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTopAgencies_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/top-agencies"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Acceso no autorizado")));
    }

    @Test
    void getTopAgencies_returns403_whenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/top-agencies").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Acceso denegado")));
        mockMvc.perform(get("/api/admin/metrics/top-agencies").with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Acceso denegado")));
    }

    @Test
    void getTopAgencies_isDocumentedInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-agencies'].get.responses['200']"
                        + ".content['application/json'].schema.type").value("array"))
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-agencies'].get.responses['200']"
                        + ".content['application/json'].schema.items['$ref']")
                        .value("#/components/schemas/AgenciesTopResponseDTO"))
                .andExpect(jsonPath("$.components.schemas.AgenciesTopResponseDTO.properties.agency['$ref']")
                        .value("#/components/schemas/SimpleAgencyDTO"))
                .andExpect(jsonPath("$.components.schemas.AgenciesTopResponseDTO.properties.sellsCount.type")
                        .value("integer"))
                .andExpect(jsonPath("$.components.schemas.SimpleAgencyDTO.properties.length()").value(3))
                .andExpect(jsonPath("$.components.schemas.SimpleAgencyDTO.properties.id.type").value("integer"))
                .andExpect(jsonPath("$.components.schemas.SimpleAgencyDTO.properties.businessName.type").value("string"))
                .andExpect(jsonPath("$.components.schemas.SimpleAgencyDTO.properties.email.type").value("string"))
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-agencies'].get.responses['401']"
                        + ".content['application/json'].schema['$ref']").value("#/components/schemas/ErrorDTO"))
                .andExpect(jsonPath("$.paths['/api/admin/metrics/top-agencies'].get.responses['403']"
                        + ".content['application/json'].schema['$ref']").value("#/components/schemas/ErrorDTO"))
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray());
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
