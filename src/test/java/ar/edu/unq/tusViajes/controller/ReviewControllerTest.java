package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.*;
import ar.edu.unq.tusViajes.model.*;
import ar.edu.unq.tusViajes.repository.*;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerTest {

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
    private CountryRepository countryRepository;
    @Autowired
    private CityRepository cityRepository;

    @Test
    void create_returns201WithReview() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City originCity = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destinationCity = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departureFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(originCity).withDestinationCity(destinationCity).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(destinationCity).withDestinationCity(originCity).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(departureFlight)
                .withReturnFlight(returnFlight)
                .withEndDate(LocalDateTime.now().minusDays(7))
                .build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        buyer.buy(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/reviews/" + travelPackage.getId())
                        .with(user(new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(),
                                createAuthorityList("ROLE_BUYER"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":5,\"comment\":\"Excellent\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.score").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent"))
                .andExpect(jsonPath("$.buyerId").value(buyer.getId()));
    }

    @Test
    void create_returns400WhenScoreIsGreaterThan10() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City originCity = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destinationCity = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departureFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(originCity).withDestinationCity(destinationCity).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(destinationCity).withDestinationCity(originCity).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(departureFlight)
                .withReturnFlight(returnFlight)
                .withEndDate(LocalDateTime.now().minusDays(7))
                .build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        buyer.buy(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/reviews/" + travelPackage.getId())
                        .with(user(new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(),
                                createAuthorityList("ROLE_BUYER"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":11}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Invalid data"))
                .andExpect(jsonPath("$.errors.score").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void create_returns201WithScoreOnly() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City originCity = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destinationCity = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departureFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(originCity).withDestinationCity(destinationCity).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(destinationCity).withDestinationCity(originCity).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(departureFlight)
                .withReturnFlight(returnFlight)
                .withEndDate(LocalDateTime.now().minusDays(7))
                .build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        buyer.buy(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/reviews/" + travelPackage.getId())
                        .with(user(new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(),
                                createAuthorityList("ROLE_BUYER"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(4))
                .andExpect(jsonPath("$.comment").value(""));
    }

    @Test
    void create_returns400WhenScoreIsMissing() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City originCity = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destinationCity = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departureFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(originCity).withDestinationCity(destinationCity).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(destinationCity).withDestinationCity(originCity).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(departureFlight)
                .withReturnFlight(returnFlight)
                .withEndDate(LocalDateTime.now().minusDays(7))
                .build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        buyer.buy(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/reviews/" + travelPackage.getId())
                        .with(user(new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(),
                                createAuthorityList("ROLE_BUYER"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Excellent\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenScoreAndCommentAreMissing() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City originCity = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destinationCity = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departureFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(originCity).withDestinationCity(destinationCity).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(destinationCity).withDestinationCity(originCity).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(departureFlight)
                .withReturnFlight(returnFlight)
                .withEndDate(LocalDateTime.now().minusDays(7))
                .build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        buyer.buy(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/reviews/" + travelPackage.getId())
                        .with(user(new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(),
                                createAuthorityList("ROLE_BUYER"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns403WhenPackageIsNotAcquired() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City originCity = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destinationCity = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departureFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(originCity).withDestinationCity(destinationCity).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(destinationCity).withDestinationCity(originCity).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(departureFlight)
                .withReturnFlight(returnFlight)
                .withEndDate(LocalDateTime.now().minusDays(7))
                .build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());

        mockMvc.perform(post("/api/reviews/" + travelPackage.getId())
                        .with(user(new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(),
                                createAuthorityList("ROLE_BUYER"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":4}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_returns401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":4}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getByTravelPackageId_returnsReviews() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().build());
        City originCity = cityRepository.save(CityBuilder.aCity().withName("Buenos Aires").withCountry(country).build());
        City destinationCity = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Flight departureFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(originCity).withDestinationCity(destinationCity).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(destinationCity).withDestinationCity(originCity).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(departureFlight)
                .withReturnFlight(returnFlight)
                .withEndDate(LocalDateTime.now().minusDays(7))
                .build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        buyer.buy(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/reviews/" + travelPackage.getId())
                        .with(user(new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(),
                                createAuthorityList("ROLE_BUYER"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":3,\"comment\":\"Good\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/reviews/" + travelPackage.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].score").value(3))
                .andExpect(jsonPath("$.content[0].comment").value("Good"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getByTravelPackageId_returns401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getByTravelPackageId_returns403ForBuyer() throws Exception {
        mockMvc.perform(get("/api/reviews/1")
                        .with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getByTravelPackageId_returns403ForAgency() throws Exception {
        mockMvc.perform(get("/api/reviews/1")
                        .with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden());
    }
}