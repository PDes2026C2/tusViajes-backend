package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.CountryRepository;
import ar.edu.unq.tusViajes.adapters.dto.CityDTO;
import ar.edu.unq.tusViajes.adapters.dto.CountryDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightFilterDTO;
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

import static ar.edu.unq.tusViajes.builder.CityBuilder.aCity;
import static ar.edu.unq.tusViajes.builder.CountryBuilder.aCountry;
import static ar.edu.unq.tusViajes.builder.FlightBuilder.aFlight;
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

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @MockitoBean
    private FlightsApiService flightsApiService;

    @Test
    void getAll_returnsAllFlightsFromDatabase() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Flight flight1 = flightRepository.save(aFlight().withId(1L).withAirline("Aerolíneas").withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight flight2 = flightRepository.save(aFlight().withId(2L).withAirline("Flybondi").withOriginCity(buenosAires).withDestinationCity(bariloche).build());

        List<FlightResponseDTO> result = flightService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(FlightResponseDTO::airline).containsExactlyInAnyOrder("Aerolíneas", "Flybondi");
    }

    @Test
    void getById_returnsFlightWhenExists() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Flight flight = flightRepository.save(aFlight().withId(10L).withAirline("JetSMART").withOriginCity(buenosAires).withDestinationCity(bariloche).build());

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
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Flight flight = flightRepository.save(aFlight().withId(20L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());

        Flight result = flightService.getEntityById(20L);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getAirline()).isEqualTo(flight.getAirline());
    }

    @Test
    void create_savesAndReturnsCreatedFlight() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        Country brasil = countryRepository.save(aCountry().withIsoCode("BR").withName("Brasil").build());
        City rio = cityRepository.save(aCity().withName("Rio de Janeiro").withCountry(brasil).build());

        CityDTO originCity = CityDTO.from(buenosAires);
        CityDTO destCity = CityDTO.from(rio);

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
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Flight saved = flightRepository.save(aFlight().withId(50L).withAirline("Aerolíneas").withOriginCity(buenosAires).withDestinationCity(bariloche).build());

        Flight result = flightService.getOrCreateFlight(50L);

        assertThat(result.getId()).isEqualTo(50L);
        assertThat(result.getAirline()).isEqualTo("Aerolíneas");
        verifyNoInteractions(flightsApiService);
    }

    @Test
    void getOrCreateFlight_fetchesFromApiAndSavesWhenNotInRepository() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        Country spain = countryRepository.save(aCountry().withIsoCode("ES").withName("España").build());

        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City madrid = cityRepository.save(aCity().withName("Madrid").withCountry(spain).build());
        LocalDateTime departure = LocalDateTime.now().plusDays(7);
        LocalDateTime arrival = LocalDateTime.now().plusDays(7).plusHours(2);

        FlightDTO apiFlight = new FlightDTO(60L, "Flybondi", CityDTO.from(buenosAires), CityDTO.from(madrid), departure, arrival);
        when(flightsApiService.getFlight(60L)).thenReturn(apiFlight);

        Flight result = flightService.getOrCreateFlight(60L);

        assertThat(result.getId()).isEqualTo(60L);
        assertThat(result.getAirline()).isEqualTo("Flybondi");
        assertThat(flightRepository.existsById(60L)).isTrue();
        verify(flightsApiService).getFlight(60L);
    }

    @Test
    void getAvailableFlights_returnsFlightsFromFlightsApiService() {
        FlightDTO flight = new FlightDTO(70L, "Aerolíneas", null, null, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(2));
        when(flightsApiService.searchFlights()).thenReturn(List.of(flight));

        List<FlightDTO> result = flightService.getAvailableFlights();

        assertThat(result).containsExactly(flight);
        verify(flightsApiService).searchFlights();
    }

    @Test
    void getAvailableFlights_withFilterAndPagination_delegatesToFlightsApiService() {
        FlightFilterDTO filter = new FlightFilterDTO("Flybondi", null, null, null, null, 1L, "AR", 2L, "ES");
        FlightDTO flight = new FlightDTO(80L, "Flybondi", null, null, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(3).plusHours(4));
        when(flightsApiService.searchFlights(filter, 0, 10)).thenReturn(List.of(flight));

        List<FlightDTO> result = flightService.getAvailableFlights(filter, 0, 10);

        assertThat(result).containsExactly(flight);
        verify(flightsApiService).searchFlights(filter, 0, 10);
    }

    @Test
    void getAvailableFlight_returnsFlightFromFlightsApiService() {
        FlightDTO flight = new FlightDTO(90L, "Iberia", null, null, LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(10).plusHours(12));
        when(flightsApiService.getFlight(90L)).thenReturn(flight);

        FlightDTO result = flightService.getAvailableFlight(90L);

        assertThat(result).isEqualTo(flight);
        verify(flightsApiService).getFlight(90L);
    }
}
