package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.dto.CityDTO;
import ar.edu.unq.tusViajes.adapters.dto.CountryDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.builder.FlightBuilder;
import ar.edu.unq.tusViajes.controller.dto.response.FlightResponseDTO;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Transactional
class FlightServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    @MockitoBean
    private FlightsApiService flightsApiService;

    @Test
    void getAll_returnsAllFlightsFromDatabase() {
        Flight flight1 = flightRepository.save(FlightBuilder.aFlight().withId(1L).withAirline("Aerolíneas").build());
        Flight flight2 = flightRepository.save(FlightBuilder.aFlight().withId(2L).withAirline("Flybondi").build());

        List<FlightResponseDTO> result = flightService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(FlightResponseDTO::airline).containsExactlyInAnyOrder("Aerolíneas", "Flybondi");
    }

    @Test
    void getById_returnsFlightWhenExists() {
        Flight flight = flightRepository.save(FlightBuilder.aFlight().withId(10L).withAirline("JetSMART").build());

        FlightResponseDTO result = flightService.getById(10L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.airline()).isEqualTo("JetSMART");
    }

    @Test
    void getById_throwsExceptionWhenDoesNotExist() {
        assertThatThrownBy(() -> flightService.getById(99999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99999");
    }

    @Test
    void getEntityById_returnsFlightEntity() {
        Flight flight = flightRepository.save(FlightBuilder.aFlight().withId(20L).build());

        Flight result = flightService.getEntityById(20L);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getAirline()).isEqualTo(flight.getAirline());
    }

    @Test
    void create_savesAndReturnsCreatedFlight() {
        CountryDTO originCountry = new CountryDTO("AR", "Argentina");
        CityDTO originCity = new CityDTO(1L, "Buenos Aires", originCountry);
        CountryDTO destCountry = new CountryDTO("BR", "Brasil");
        CityDTO destCity = new CityDTO(2L, "Rio de Janeiro", destCountry);
        LocalDateTime departure = LocalDateTime.now().plusDays(5);
        LocalDateTime arrival = LocalDateTime.now().plusDays(5).plusHours(3);

        FlightDTO dto = new FlightDTO(30L, "LATAM", originCity, destCity, departure, arrival);

        FlightResponseDTO result = flightService.create(dto);

        assertThat(result.id()).isEqualTo(30L);
        assertThat(result.airline()).isEqualTo("LATAM");
        assertThat(flightRepository.existsById(30L)).isTrue();
    }

    @Test
    void toEntity_convertsFlightDTOCorrectly() {
        CountryDTO originCountry = new CountryDTO("AR", "Argentina");
        CityDTO originCity = new CityDTO(1L, "Buenos Aires", originCountry);
        CountryDTO destCountry = new CountryDTO("ES", "España");
        CityDTO destCity = new CityDTO(2L, "Madrid", destCountry);
        LocalDateTime departure = LocalDateTime.now().plusDays(15);
        LocalDateTime arrival = LocalDateTime.now().plusDays(15).plusHours(12);

        FlightDTO dto = new FlightDTO(40L, "Iberia", originCity, destCity, departure, arrival);

        Flight entity = flightService.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(40L);
        assertThat(entity.getAirline()).isEqualTo("Iberia");
        assertThat(entity.getOriginCity().getName()).isEqualTo("Buenos Aires");
        assertThat(entity.getDestinationCity().getName()).isEqualTo("Madrid");
    }

    @Test
    void getOrCreateFlight_returnsExistingFlightWhenAlreadyInRepository() {
        Flight saved = flightRepository.save(FlightBuilder.aFlight().withId(50L).withAirline("Aerolíneas").build());

        Flight result = flightService.getOrCreateFlight(50L);

        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.getAirline()).isEqualTo("Aerolíneas");
        verifyNoInteractions(flightsApiService);
    }

    @Test
    void getOrCreateFlight_fetchesFromApiAndSavesWhenNotInRepository() {
        CountryDTO originCountry = new CountryDTO("AR", "Argentina");
        CityDTO originCity = new CityDTO(1L, "Buenos Aires", originCountry);
        CountryDTO destCountry = new CountryDTO("AR", "Argentina");
        CityDTO destCity = new CityDTO(2L, "Bariloche", destCountry);
        LocalDateTime departure = LocalDateTime.now().plusDays(7);
        LocalDateTime arrival = LocalDateTime.now().plusDays(7).plusHours(2);

        FlightDTO apiFlight = new FlightDTO(60L, "Flybondi", originCity, destCity, departure, arrival);
        when(flightsApiService.getFlight(60L)).thenReturn(apiFlight);

        Flight result = flightService.getOrCreateFlight(60L);

        assertThat(result.getId()).isEqualTo(60L);
        assertThat(result.getAirline()).isEqualTo("Flybondi");
        assertThat(flightRepository.existsById(60L)).isTrue();
        verify(flightsApiService).getFlight(60L);
    }
}
