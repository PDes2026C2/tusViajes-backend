package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.CityBuilder;
import ar.edu.unq.tusViajes.builder.CountryBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.CountryRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class HotelControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void getAll_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_returns403_whenRoleIsBuyer() throws Exception {
        mockMvc.perform(get("/api/hotels").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_returns200AndListOfHotels_whenRoleIsAdmin() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().withIsoCode("AR").withName("Argentina").build());
        City city = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        hotelRepository.save(HotelBuilder.aHotel().withName("Hotel Central").withCity(city).build());

        mockMvc.perform(get("/api/hotels").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].name").value("Hotel Central"))
                .andExpect(jsonPath("$[0].city.name").value("Bariloche"));
    }

    @Test
    void getAll_returns200AndListOfHotels_whenRoleIsAgency() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().withIsoCode("AR").withName("Argentina").build());
        City city = cityRepository.save(CityBuilder.aCity().withName("Mar del Plata").withCountry(country).build());
        hotelRepository.save(HotelBuilder.aHotel().withName("Hotel Costa").withCity(city).build());

        mockMvc.perform(get("/api/hotels").with(user("agency").roles("AGENCY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty());
    }

    @Test
    void getById_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/hotels/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_returns403_whenRoleIsBuyer() throws Exception {
        mockMvc.perform(get("/api/hotels/1").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_returns200WhenExists_whenRoleIsAdmin() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().withIsoCode("AR").withName("Argentina").build());
        City city = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Hotel saved = hotelRepository.save(
                HotelBuilder.aHotel().withName("Hotel Central").withCity(city).build()
        );

        mockMvc.perform(get("/api/hotels/" + saved.getId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Hotel Central"))
                .andExpect(jsonPath("$.city.name").value("Bariloche"));
    }

    @Test
    void getById_returns200WhenExists_whenRoleIsAgency() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().withIsoCode("AR").withName("Argentina").build());
        City city = cityRepository.save(CityBuilder.aCity().withName("Bariloche").withCountry(country).build());
        Hotel saved = hotelRepository.save(
                HotelBuilder.aHotel().withName("Hotel Central").withCity(city).build()
        );

        mockMvc.perform(get("/api/hotels/" + saved.getId()).with(user("agency").roles("AGENCY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()));
    }

    @Test
    void getById_returns404WhenDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/hotels/99999").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns401_whenUnauthenticated() throws Exception {
        String json = """
                {
                    "name": "Hotel Nuevo",
                    "cityId": 99999
                }
                """;

        mockMvc.perform(post("/api/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_returns201AndLocationHeader() throws Exception {
        Country country = countryRepository.save(CountryBuilder.aCountry().withIsoCode("AR").withName("Argentina").build());
        City city = cityRepository.save(CityBuilder.aCity().withName("Mendoza").withCountry(country).build());
        String json = """
                {
                    "name": "Hotel Nuevo",
                    "cityId": %d
                }
                """.formatted(city.getId());

        mockMvc.perform(post("/api/hotels")
                        .with(user("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Hotel Nuevo"))
                .andExpect(jsonPath("$.city.name").value("Mendoza"));
    }
}