package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.FlightBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.UpdateTravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.exception.InvalidTravelPackageException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.exception.UnauthorizedAgencyException;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
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

        Page<TravelPackage> result = travelPackageService.search(PageRequest.of(0, 10));

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

        TravelPackage result = travelPackageService.getById(saved.getId());

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

        TravelPackage result = travelPackageService.create(dto, agency.getId());

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

        assertThatThrownBy(() -> travelPackageService.create(dto, agency.getId()))
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

        UpdateTravelPackageRequestDTO dto = UpdateTravelPackageRequestDTO
                .builder()
                .name("Updated Package")
                .description("Updated Description")
                .price(200000.0)
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(10))
                .hotelId(hotelInMendoza.getId())
                .departureFlightId(depFlight.getId())
                .returnFlightId(retFlight.getId())
                .build();

        assertThatThrownBy(() -> travelPackageService.update(saved.getId(), agency.getId(), dto))
                .isInstanceOf(InvalidTravelPackageException.class)
                .hasMessageContaining("La ciudad del hotel debe coincidir");
    }

    @Test
    void delete_deactivatesTravelPackageInsteadOfHardDeleting() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("softdel@agency.com").withTaxId("30-55555555-5").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(80L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(81L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage saved = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel).withAgency(agency).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        travelPackageService.delete(saved.getId(), agency.getId());

        TravelPackage inDb = travelPackageRepository.findById(saved.getId()).orElseThrow();
        assertThat(inDb.isActive()).isFalse();
        assertThat(travelPackageRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    void search_returnsOnlyActiveTravelPackages() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("searchact@agency.com").withTaxId("30-66666666-6").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(90L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(91L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage activePackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Active Package")
                .withHotel(hotel).withAgency(agency).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        TravelPackage inactivePackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Inactive Package")
                .withActive(false)
                .withHotel(hotel).withAgency(agency).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        Page<TravelPackageResponseDTO> result = travelPackageService.search(PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(TravelPackageResponseDTO::getName)
                .contains(activePackage.getName())
                .doesNotContain(inactivePackage.getName());
    }

    @Test
    void delete_throwsWhenTravelPackageDoesNotBelongToAgency() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency ownerAgency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("owner@agency.com").withTaxId("30-11111111-1").build());
        Agency otherAgency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("other@agency.com").withTaxId("30-22222222-2").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(60L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(61L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage saved = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel).withAgency(ownerAgency).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        assertThatThrownBy(() -> travelPackageService.delete(saved.getId(), otherAgency.getId()))
                .isInstanceOf(UnauthorizedAgencyException.class);
    }

    @Test
    void update_throwsWhenTravelPackageDoesNotBelongToAgency() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency ownerAgency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("owner2@agency.com").withTaxId("30-33333333-3").build());
        Agency otherAgency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("other2@agency.com").withTaxId("30-44444444-4").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(70L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(71L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage saved = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel).withAgency(ownerAgency).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        UpdateTravelPackageRequestDTO dto = UpdateTravelPackageRequestDTO
                .builder()
                .name("Update Name")
                .description("Desc")
                .price(200000.0)
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(10))
                .hotelId(hotel.getId())
                .departureFlightId(depFlight.getId())
                .returnFlightId(retFlight.getId())
                .build();

        assertThatThrownBy(() -> travelPackageService.update(saved.getId(), otherAgency.getId(), dto))
                .isInstanceOf(UnauthorizedAgencyException.class);
    }
}
