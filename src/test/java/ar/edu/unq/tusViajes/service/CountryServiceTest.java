package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.dto.CountryDTO;
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

import static ar.edu.unq.tusViajes.builder.CountryBuilder.aCountry;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Transactional
class CountryServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CountryService countryService;

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void getCountries_returnsAllCountries() {
        countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        countryRepository.save(aCountry().withIsoCode("ES").withName("España").build());

        List<CountryDTO> result = countryService.getCountries();

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result).extracting(CountryDTO::name).contains("Argentina", "España");
        assertThat(result).extracting(CountryDTO::isoCode).contains("AR", "ES");
    }
}
