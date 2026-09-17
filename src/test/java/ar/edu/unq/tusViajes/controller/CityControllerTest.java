package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.CountryRepository;
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

import static ar.edu.unq.tusViajes.builder.CityBuilder.aCity;
import static ar.edu.unq.tusViajes.builder.CountryBuilder.aCountry;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CityControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void getCities_returns200AndAllCities_whenNoIsoCodeProvided() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").withName("España").build());

        cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        cityRepository.save(aCity().withName("Madrid").withCountry(spain).build());

        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getCities_returns200AndFilteredCities_whenIsoCodeProvided() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").withName("España").build());

        cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        cityRepository.save(aCity().withName("Córdoba").withCountry(argentina).build());
        cityRepository.save(aCity().withName("Madrid").withCountry(spain).build());

        mockMvc.perform(get("/api/cities").param("isoCode", "AR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].country.isoCode").value("AR"))
                .andExpect(jsonPath("$[1].country.isoCode").value("AR"));
    }

    @Test
    void getCities_isCaseInsensitiveForIsoCode() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        cityRepository.save(aCity().withName("Rosario").withCountry(argentina).build());

        mockMvc.perform(get("/api/cities").param("isoCode", "ar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Rosario"))
                .andExpect(jsonPath("$[0].country.isoCode").value("AR"));
    }

    @Test
    void getCities_returnsEmptyList_whenCountryHasNoCities() throws Exception {
        mockMvc.perform(get("/api/cities").param("isoCode", "ZZ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getCities_worksWithApiCitiesPrefix() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        cityRepository.save(aCity().withName("Mendoza").withCountry(argentina).build());

        mockMvc.perform(get("/api/cities").param("isoCode", "AR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Mendoza"));
    }

    @Test
    void getCities_allowsAuthenticatedUsers() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        cityRepository.save(aCity().withName("Salta").withCountry(argentina).build());

        mockMvc.perform(get("/api/cities").with(user("buyer").roles("BUYER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Salta"));
    }
}
