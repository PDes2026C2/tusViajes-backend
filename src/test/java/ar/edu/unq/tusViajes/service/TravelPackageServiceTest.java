package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.FlightBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.exception.InvalidTravelPackageException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.CountryRepository;
import ar.edu.unq.tusViajes.repository.FlightRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static ar.edu.unq.tusViajes.builder.CityBuilder.aCity;
import static ar.edu.unq.tusViajes.builder.CountryBuilder.aCountry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Transactional 
class TravelPackageServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TravelPackageService travelPackageService;

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
    void getAll_returnsAllAvailableTravelPackages() {
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
        travelPackageRepository.save(travelPackage);

        Page<TravelPackageResponseDTO> result = travelPackageService.search(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(travelPackage.getName());
    }

    @Test
    void getById_returnsTravelPackageWhenExists() {
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

        TravelPackageResponseDTO result = travelPackageService.getById(saved.getId());

        assertThat(result.getName()).isEqualTo(travelPackage.getName());
        assertThat(result.getPrice()).isEqualTo(travelPackage.getPrice());
    }

    @Test
    void getById_throwsExceptionWhenDoesNotExist() {
        assertThatThrownBy(() -> travelPackageService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesAndReturnsTravelPackageWithHotelAndAgency() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackageRequestDTO dto = new TravelPackageRequestDTO(
                "Viaje a Cataratas", "All inclusive", 200000.0,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10),
                hotel.getId(),
                depFlight.getId(), retFlight.getId()
        );

        TravelPackageResponseDTO result = travelPackageService.create(agency.getId(), dto);

        assertThat(result.getName()).isEqualTo("Viaje a Cataratas");
        assertThat(result.getPrice()).isEqualTo(200000.0);
        
        assertThat(travelPackageRepository.existsById(result.getId())).isTrue();
    }

    @Test
    void create_throwsWhenHotelCityDoesNotMatchFlightDestination() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());
        City mendoza = cityRepository.save(aCity().withName("Mendoza").withCountry(argentina).build());

        Hotel hotelInMendoza = hotelRepository.save(HotelBuilder.aHotel().withCity(mendoza).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(10L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(11L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackageRequestDTO dto = new TravelPackageRequestDTO(
                "Viaje a Bariloche", "Desc", 200000.0,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10),
                hotelInMendoza.getId(),
                depFlight.getId(), retFlight.getId()
        );

        assertThatThrownBy(() -> travelPackageService.create(agency.getId(), dto))
                .isInstanceOf(InvalidTravelPackageException.class)
                .hasMessageContaining("La ciudad del hotel debe coincidir");
    }

    @Test
    void update_throwsWhenHotelCityDoesNotMatchFlightDestination() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());
        City mendoza = cityRepository.save(aCity().withName("Mendoza").withCountry(argentina).build());

        Hotel hotelInBariloche = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Hotel hotelInMendoza = hotelRepository.save(HotelBuilder.aHotel().withCity(mendoza).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(20L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(21L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage saved = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotelInBariloche).withAgency(agency).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        TravelPackageRequestDTO dto = new TravelPackageRequestDTO(
                "Update", "Desc", 200000.0,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10),
                hotelInMendoza.getId(),
                depFlight.getId(), retFlight.getId()
        );

        assertThatThrownBy(() -> travelPackageService.update(agency.getId(), saved.getId(), dto))
                .isInstanceOf(InvalidTravelPackageException.class)
                .hasMessageContaining("La ciudad del hotel debe coincidir");
    }

    @Test
    void create_usesPrincipalAsOwner() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency owner = agencyRepository.save(AgencyBuilder.anAgency().withEmail("owner62@example.com").withTaxId("20-62000001-1").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(50L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(51L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackageRequestDTO dto = new TravelPackageRequestDTO(
                "Spoof attempt", "Desc", 100000.0,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10),
                hotel.getId(),
                depFlight.getId(), retFlight.getId()
        );

        TravelPackageResponseDTO result = travelPackageService.create(owner.getId(), dto);

        assertThat(result.getAgency().id()).isEqualTo(owner.getId());
    }

    @Test
    void update_throwsWhenNotOwner() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency owner = agencyRepository.save(AgencyBuilder.anAgency().withEmail("owner63@example.com").withTaxId("20-63000001-1").build());
        Agency other = agencyRepository.save(AgencyBuilder.anAgency().withEmail("other63@example.com").withTaxId("20-63000002-2").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(60L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(61L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage saved = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel).withAgency(owner).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        TravelPackageRequestDTO dto = new TravelPackageRequestDTO(
                "Hijack", "Desc", 100000.0,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10),
                hotel.getId(),
                depFlight.getId(), retFlight.getId()
        );

        assertThatThrownBy(() -> travelPackageService.update(other.getId(), saved.getId(), dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Paquete de viaje con id");
    }

    @Test
    void delete_throwsWhenNotOwner() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency owner = agencyRepository.save(AgencyBuilder.anAgency().withEmail("owner64@example.com").withTaxId("20-64000001-1").build());
        Agency other = agencyRepository.save(AgencyBuilder.anAgency().withEmail("other64@example.com").withTaxId("20-64000002-2").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(70L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(71L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage saved = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel).withAgency(owner).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        assertThatThrownBy(() -> travelPackageService.delete(other.getId(), saved.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Paquete de viaje con id");
        assertThat(travelPackageRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    void searchMine_returnsOnlyOwnPackages() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency mine = agencyRepository.save(AgencyBuilder.anAgency().withEmail("mine62@example.com").withTaxId("20-62000003-3").build());
        Agency other = agencyRepository.save(AgencyBuilder.anAgency().withEmail("theirs62@example.com").withTaxId("20-62000004-4").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(80L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(81L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Mine").withHotel(hotel).withAgency(mine).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());
        travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Theirs").withHotel(hotel).withAgency(other).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        Page<TravelPackageResponseDTO> result = travelPackageService.searchMine(mine.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Mine");
    }
}
