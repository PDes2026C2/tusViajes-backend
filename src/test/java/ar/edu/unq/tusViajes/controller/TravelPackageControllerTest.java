package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.CityBuilder;
import ar.edu.unq.tusViajes.builder.CountryBuilder;
import ar.edu.unq.tusViajes.builder.FlightBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.CountryRepository;
import ar.edu.unq.tusViajes.repository.FlightRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static ar.edu.unq.tusViajes.builder.CityBuilder.aCity;
import static ar.edu.unq.tusViajes.builder.CountryBuilder.aCountry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class TravelPackageControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    void getAll_returns200AndListOfTravelPackages() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());
        
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withName("Bariloche 7d")
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(depFlight)
                .withReturnFlight(retFlight)
                .build();
        travelPackageRepository.save(travelPackage);

        mockMvc.perform(get("/api/travel-packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content[0].name").value("Bariloche 7d"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getById_returns200WhenExists() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());
        
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withName("Bariloche 7d")
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(depFlight)
                .withReturnFlight(retFlight)
                .build();
        TravelPackage saved = travelPackageRepository.save(travelPackage);

        mockMvc.perform(get("/api/travel-packages/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Bariloche 7d"));
    }

    @Test
    void getById_returns404WhenDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/travel-packages/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns401_whenUnauthenticated() throws Exception {
        String json = """
                {
                    "name": "Bariloche 7d",
                    "description": "Desc",
                    "price": 150000.0,
                    "startDate": "2026-10-01T10:00:00",
                    "endDate": "2026-10-08T10:00:00",
                    "hotelId": 1,
                    "departureFlightId": 1,
                    "returnFlightId": 2
                }
                """;

        mockMvc.perform(post("/api/travel-packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_returns201AndLocationHeader() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        String json = """
                {
                    "name": "Bariloche 7d",
                    "description": "Desc",
                    "price": 150000.0,
                    "startDate": "2026-10-01T10:00:00",
                    "endDate": "2026-10-08T10:00:00",
                    "hotelId": %d,
                    "departureFlightId": %d,
                    "returnFlightId": %d
                }
                """.formatted(hotel.getId(), depFlight.getId(), retFlight.getId());

        mockMvc.perform(post("/api/travel-packages")
                        .with(user(new CustomUserDetails(agency.getId(), agency.getEmail(), agency.getPasswordHash(), createAuthorityList("ROLE_AGENCY"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Bariloche 7d"));
    }

    @Test
    void update_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(put("/api/travel-packages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_returns200_whenAuthenticated() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(depFlight)
                .withReturnFlight(retFlight)
                .build();
        TravelPackage saved = travelPackageRepository.save(travelPackage);

        String json = """
                {
                    "name": "Bariloche 10d",
                    "description": "Extended desc",
                    "price": 200000.0,
                    "startDate": "2026-11-01T10:00:00",
                    "endDate": "2026-11-10T10:00:00",
                    "hotelId": %d,
                    "departureFlightId": %d,
                    "returnFlightId": %d
                }
                """.formatted(hotel.getId(), depFlight.getId(), retFlight.getId());

        mockMvc.perform(put("/api/travel-packages/" + saved.getId())
                        .with(user(new CustomUserDetails(agency.getId(), agency.getEmail(), agency.getPasswordHash(), createAuthorityList("ROLE_AGENCY"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bariloche 10d"));
    }

    @Test
    void delete_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/travel-packages/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_returns204NoContent() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());
        
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(depFlight)
                .withReturnFlight(retFlight)
                .build();
        TravelPackage saved = travelPackageRepository.save(travelPackage);

        mockMvc.perform(delete("/api/travel-packages/" + saved.getId()).with(user(new CustomUserDetails(agency.getId(), agency.getEmail(), agency.getPasswordHash(), createAuthorityList("ROLE_AGENCY"), true))))
                .andExpect(status().isNoContent());

        assertThat(travelPackageRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void create_returns400_whenHotelCityDoesNotMatchFlightDestination() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());
        City mendoza = cityRepository.save(aCity().withName("Mendoza").withCountry(argentina).build());

        Hotel hotelInMendoza = hotelRepository.save(HotelBuilder.aHotel().withCity(mendoza).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(30L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(31L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        String json = """
                {
                    "name": "Viaje invalido",
                    "description": "Hotel en otra ciudad",
                    "price": 150000.0,
                    "startDate": "2026-10-01T10:00:00",
                    "endDate": "2026-10-08T10:00:00",
                    "hotelId": %d,
                    "departureFlightId": %d,
                    "returnFlightId": %d
                }
                """.formatted(hotelInMendoza.getId(), depFlight.getId(), retFlight.getId());

        mockMvc.perform(post("/api/travel-packages")
                        .with(user(new CustomUserDetails(agency.getId(), agency.getEmail(), agency.getPasswordHash(), createAuthorityList("ROLE_AGENCY"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("La ciudad del hotel debe coincidir")));
    }

    @Test
    void update_returns400_whenHotelCityDoesNotMatchFlightDestination() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());
        City mendoza = cityRepository.save(aCity().withName("Mendoza").withCountry(argentina).build());

        Hotel hotelInBariloche = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Hotel hotelInMendoza = hotelRepository.save(HotelBuilder.aHotel().withCity(mendoza).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(40L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(41L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage saved = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotelInBariloche).withAgency(agency).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        String json = """
                {
                    "name": "Update invalido",
                    "description": "Hotel mismatch",
                    "price": 200000.0,
                    "startDate": "2026-11-01T10:00:00",
                    "endDate": "2026-11-10T10:00:00",
                    "hotelId": %d,
                    "departureFlightId": %d,
                    "returnFlightId": %d
                }
                """.formatted(hotelInMendoza.getId(), depFlight.getId(), retFlight.getId());

        mockMvc.perform(put("/api/travel-packages/" + saved.getId())
                        .with(user(new CustomUserDetails(agency.getId(), agency.getEmail(), agency.getPasswordHash(), createAuthorityList("ROLE_AGENCY"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("La ciudad del hotel debe coincidir")));
    }

    @Test
    void create_returns403_whenBuyer() throws Exception {
        String json = """
                {
                    "name": "Bariloche 7d",
                    "description": "Desc",
                    "price": 150000.0,
                    "startDate": "2026-10-01T10:00:00",
                    "endDate": "2026-10-08T10:00:00",
                    "hotelId": 1,
                    "departureFlightId": 1,
                    "returnFlightId": 2
                }
                """;

        mockMvc.perform(post("/api/travel-packages")
                        .with(user("buyer").roles("BUYER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_usesPrincipalAsOwner() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency owner = agencyRepository.save(AgencyBuilder.anAgency().withEmail("owner@example.com").withTaxId("20-11111111-1").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(90L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(91L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        String json = """
                {
                    "name": "Spoof",
                    "description": "Desc",
                    "price": 150000.0,
                    "startDate": "2026-10-01T10:00:00",
                    "endDate": "2026-10-08T10:00:00",
                    "hotelId": %d,
                    "departureFlightId": %d,
                    "returnFlightId": %d
                }
                """.formatted(hotel.getId(), depFlight.getId(), retFlight.getId());

        mockMvc.perform(post("/api/travel-packages")
                        .with(user(new CustomUserDetails(owner.getId(), owner.getEmail(), owner.getPasswordHash(), createAuthorityList("ROLE_AGENCY"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.agency.id").value(owner.getId()));
    }

    @Test
    void update_returns404_whenNotOwner() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency owner = agencyRepository.save(AgencyBuilder.anAgency().withEmail("owner2@example.com").withTaxId("20-33333333-3").build());
        Agency other = agencyRepository.save(AgencyBuilder.anAgency().withEmail("other2@example.com").withTaxId("20-44444444-4").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(92L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(93L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage saved = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel).withAgency(owner).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        String json = """
                {
                    "name": "Hijack",
                    "description": "Desc",
                    "price": 150000.0,
                    "startDate": "2026-10-01T10:00:00",
                    "endDate": "2026-10-08T10:00:00",
                    "hotelId": %d,
                    "departureFlightId": %d,
                    "returnFlightId": %d
                }
                """.formatted(hotel.getId(), depFlight.getId(), retFlight.getId());

        mockMvc.perform(put("/api/travel-packages/" + saved.getId())
                        .with(user(new CustomUserDetails(other.getId(), other.getEmail(), other.getPasswordHash(), createAuthorityList("ROLE_AGENCY"), true)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returns404_whenNotOwner() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency owner = agencyRepository.save(AgencyBuilder.anAgency().withEmail("owner3@example.com").withTaxId("20-55555555-5").build());
        Agency other = agencyRepository.save(AgencyBuilder.anAgency().withEmail("other3@example.com").withTaxId("20-66666666-6").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(94L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(95L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage saved = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel).withAgency(owner).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        mockMvc.perform(delete("/api/travel-packages/" + saved.getId())
                        .with(user(new CustomUserDetails(other.getId(), other.getEmail(), other.getPasswordHash(), createAuthorityList("ROLE_AGENCY"), true))))
                .andExpect(status().isNotFound());

        assertThat(travelPackageRepository.existsById(saved.getId())).isTrue();
    }

}

