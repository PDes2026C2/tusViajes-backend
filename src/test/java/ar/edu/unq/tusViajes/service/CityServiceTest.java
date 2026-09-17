package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.dto.CityDTO;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.CountryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static ar.edu.unq.tusViajes.builder.CityBuilder.aCity;
import static ar.edu.unq.tusViajes.builder.CountryBuilder.aCountry;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Transactional
class CityServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CityService cityService;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void getCities_returnsAllCities_whenIsoCodeIsNull() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").withName("España").build());

        cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        cityRepository.save(aCity().withName("Madrid").withCountry(spain).build());

        List<CityDTO> result = cityService.getCities(null);

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).extracting(CityDTO::name).contains("Buenos Aires", "Madrid");
    }

    @Test
    void getCities_returnsAllCities_whenIsoCodeIsBlank() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        cityRepository.save(aCity().withName("Rosario").withCountry(argentina).build());

        List<CityDTO> result = cityService.getCities("   ");

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(CityDTO::name).contains("Rosario");
    }

    @Test
    void getCities_returnsOnlyCitiesForGivenIsoCode() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").withName("España").build());

        cityRepository.save(aCity().withName("Córdoba").withCountry(argentina).build());
        cityRepository.save(aCity().withName("Mendoza").withCountry(argentina).build());
        cityRepository.save(aCity().withName("Barcelona").withCountry(spain).build());

        List<CityDTO> result = cityService.getCities("AR");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CityDTO::name).containsExactlyInAnyOrder("Córdoba", "Mendoza");
        assertThat(result).allMatch(dto -> dto.country().isoCode().equalsIgnoreCase("AR"));
    }

    @Test
    void getCities_returnsCitiesCaseInsensitively() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        List<CityDTO> result = cityService.getCities("ar");

        assertThat(result).extracting(CityDTO::name).contains("Bariloche");
    }

    @Test
    void getCities_returnsEmptyList_whenCountryIsoCodeHasNoCities() {
        List<CityDTO> result = cityService.getCities("ZZ");

        assertThat(result).isEmpty();
    }
}
