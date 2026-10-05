package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.UpdateTravelPackageRequestDTO;
import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.CountryRepository;
import ar.edu.unq.tusViajes.builder.AgencyBuilder;
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
import ar.edu.unq.tusViajes.repository.FlightRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDateTime;

import static ar.edu.unq.tusViajes.builder.CityBuilder.aCity;
import static ar.edu.unq.tusViajes.builder.CountryBuilder.aCountry;
import static ar.edu.unq.tusViajes.util.TestSecurityUtils.withCustomUserDetails;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());


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
        TravelPackageRequestDTO dto = TravelPackageRequestDTO
                .builder()
                .name("Bariloche 7d")
                .description("Desc")
                .price(150000.0)
                .startDate(LocalDateTime.of(2026, 10, 1, 10, 0))
                .endDate(LocalDateTime.of(2026, 10, 8, 10, 0))
                .hotelId(1L)
                .departureFlightId(1L)
                .returnFlightId(2L)
                .build();

        String json = mapper.writeValueAsString(dto);

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

        TravelPackageRequestDTO dto = TravelPackageRequestDTO
                .builder()
                .name("Bariloche 7d")
                .description("Desc")
                .price(150000.0)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(20))
                .hotelId(hotel.getId())
                .departureFlightId(depFlight.getId())
                .returnFlightId(retFlight.getId())
                .build();

        String json = mapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/travel-packages")
                        .with(withCustomUserDetails(agency))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Bariloche 7d"));
    }

    @Test
    void update_returns401_whenUnauthenticated() throws Exception {
        UpdateTravelPackageRequestDTO dto = UpdateTravelPackageRequestDTO.builder()
                .id(1L)
                .name("Bariloche 10d")
                .description("Desc")
                .price(200000.0)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(20))
                .hotelId(1L)
                .departureFlightId(1L)
                .returnFlightId(2L)
                .build();

        String json = mapper.writeValueAsString(dto);

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

        UpdateTravelPackageRequestDTO updateDto = UpdateTravelPackageRequestDTO.builder()
                .id(saved.getId())
                .name("Bariloche 10d")
                .description("Extended desc")
                .price(200000.0)
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate().plusDays(3))
                .hotelId(hotel.getId())
                .departureFlightId(depFlight.getId())
                .returnFlightId(retFlight.getId())
                .build();

        String json = mapper.writeValueAsString(updateDto);

        mockMvc.perform(put("/api/travel-packages/" + saved.getId())
                        .with(withCustomUserDetails(agency))
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

        mockMvc.perform(delete("/api/travel-packages/" + saved.getId())
                        .with(withCustomUserDetails(agency)))
                .andExpect(status().isNoContent());

        TravelPackage inDb = travelPackageRepository.findById(saved.getId()).orElseThrow();
        assertThat(inDb.isActive()).isFalse();
        assertThat(travelPackageRepository.existsById(saved.getId())).isTrue();
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

        TravelPackageRequestDTO dto = TravelPackageRequestDTO.builder()
                .name("Viaje invalido")
                .description("Hotel en otra ciudad")
                .price(150000.0)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(20))
                .hotelId(hotelInMendoza.getId())
                .departureFlightId(depFlight.getId())
                .returnFlightId(retFlight.getId())
                .build();

        String json = mapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/travel-packages")
                        .with(withCustomUserDetails(agency))
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

        UpdateTravelPackageRequestDTO dto = UpdateTravelPackageRequestDTO.builder()
                .id(saved.getId())
                .name("Update invalido")
                .description("Hotel mismatch")
                .price(200000.0)
                .startDate(LocalDateTime.of(2026, 11, 1, 10, 0))
                .endDate(LocalDateTime.of(2026, 11, 10, 10, 0))
                .hotelId(hotelInMendoza.getId())
                .departureFlightId(depFlight.getId())
                .returnFlightId(retFlight.getId())
                .build();

        String json = mapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/travel-packages/" + saved.getId())
                        .with(withCustomUserDetails(agency))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("La ciudad del hotel debe coincidir")));
    }

    @Test
    void create_returns403_whenRoleIsNotAgency() throws Exception {
        TravelPackageRequestDTO dto = TravelPackageRequestDTO.builder()
                .name("Bariloche 7d")
                .description("Desc")
                .price(150000.0)
                .startDate(LocalDateTime.of(2026, 10, 1, 10, 0))
                .endDate(LocalDateTime.of(2026, 10, 8, 10, 0))
                .hotelId(1L)
                .departureFlightId(1L)
                .returnFlightId(2L)
                .build();

        String json = mapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/travel-packages")
                        .with(user("buyer").roles("BUYER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_returns403_whenRoleIsNotAgency() throws Exception {
        UpdateTravelPackageRequestDTO dto = UpdateTravelPackageRequestDTO.builder()
                .id(1L)
                .name("Bariloche 10d")
                .description("Desc")
                .price(200000.0)
                .startDate(LocalDateTime.of(2026, 10, 1, 10, 0))
                .endDate(LocalDateTime.of(2026, 10, 10, 10, 0))
                .hotelId(1L)
                .departureFlightId(1L)
                .returnFlightId(2L)
                .build();

        String json = mapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/travel-packages/1")
                        .with(user("buyer").roles("BUYER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_returns403_whenRoleIsNotAgency() throws Exception {
        mockMvc.perform(delete("/api/travel-packages/1")
                        .with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_softDeletesPackageSoItIsNotReturnedInSearch() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(100L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(101L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withName("Bariloche Soft Delete Test")
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(depFlight)
                .withReturnFlight(retFlight)
                .build();
        TravelPackage saved = travelPackageRepository.save(travelPackage);

        mockMvc.perform(delete("/api/travel-packages/" + saved.getId())
                        .with(withCustomUserDetails(agency)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/travel-packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}

