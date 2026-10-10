package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.FlightBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageFilterDTO;
import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.UpdateTravelPackageRequestDTO;
import ar.edu.unq.tusViajes.exception.InvalidTravelPackageException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.exception.UnauthorizedAgencyException;
import ar.edu.unq.tusViajes.model.*;
import ar.edu.unq.tusViajes.repository.*;
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
import static ar.edu.unq.tusViajes.builder.TravelPackageFilterDTOBuilder.aFilter;
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

        Page<TravelPackage> result = travelPackageService.search(TravelPackageFilterDTO.empty(), PageRequest.of(0, 10));

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

        TravelPackageRequestDTO dto = TravelPackageRequestDTO
                .builder()
                .name("Viaje a Cataratas")
                .description("All inclusive")
                .price(200000.0)
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(10))
                .hotelId(hotel.getId())
                .departureFlightId(depFlight.getId())
                .returnFlightId(retFlight.getId())
                .build();

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

        TravelPackageRequestDTO dto = TravelPackageRequestDTO
                .builder()
                .name("Viaje a Bariloche")
                .description("Desc")
                .price(200000.0)
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(10))
                .hotelId(hotelInMendoza.getId())
                .departureFlightId(depFlight.getId())
                .returnFlightId(retFlight.getId())
                .build();

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
                .id(saved.getId())
                .name("Updated Package")
                .description("Updated Description")
                .price(200000.0)
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(10))
                .hotelId(hotelInMendoza.getId())
                .departureFlightId(depFlight.getId())
                .returnFlightId(retFlight.getId())
                .build();

        assertThatThrownBy(() -> travelPackageService.update(agency.getId(), dto))
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

        Page<TravelPackage> result = travelPackageService.search(TravelPackageFilterDTO.empty(), PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(TravelPackage::getName)
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
                .id(saved.getId())
                .name("Update Name")
                .description("Desc")
                .price(200000.0)
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(10))
                .hotelId(hotel.getId())
                .departureFlightId(depFlight.getId())
                .returnFlightId(retFlight.getId())
                .build();

        assertThatThrownBy(() -> travelPackageService.update(otherAgency.getId(), dto))
                .isInstanceOf(UnauthorizedAgencyException.class);
    }

    @Test
    void search_filtersByOriginCountryIso() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        Country brasil = countryRepository.save(aCountry().withIsoCode("BR").withName("Brasil").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City saoPaulo = cityRepository.save(aCity().withName("Sao Paulo").withCountry(brasil).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("origincountry@test.com").withTaxId("30-70000001-1").build());

        Flight depFlightAR = flightRepository.save(FlightBuilder.aFlight().withId(301L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlightAR = flightRepository.save(FlightBuilder.aFlight().withId(302L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        Flight depFlightBR = flightRepository.save(FlightBuilder.aFlight().withId(303L).withOriginCity(saoPaulo).withDestinationCity(bariloche).build());
        Flight retFlightBR = flightRepository.save(FlightBuilder.aFlight().withId(304L).withOriginCity(bariloche).withDestinationCity(saoPaulo).build());

        TravelPackage packageAR = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Paquete Salida Argentina")
                .withHotel(hotel).withAgency(agency).withDepartureFlight(depFlightAR).withReturnFlight(retFlightAR).build());

        TravelPackage packageBR = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Paquete Salida Brasil")
                .withHotel(hotel).withAgency(agency).withDepartureFlight(depFlightBR).withReturnFlight(retFlightBR).build());

        TravelPackageFilterDTO filterAR = aFilter().withOriginCountryIso("AR").build();
        Page<TravelPackage> resultAR = travelPackageService.search(filterAR, PageRequest.of(0, 10));
        assertThat(resultAR.getContent()).extracting(TravelPackage::getName)
                .contains(packageAR.getName())
                .doesNotContain(packageBR.getName());

        TravelPackageFilterDTO filterBRCaseInsensitive = aFilter().withOriginCountryIso("br").build();
        Page<TravelPackage> resultBR = travelPackageService.search(filterBRCaseInsensitive, PageRequest.of(0, 10));
        assertThat(resultBR.getContent()).extracting(TravelPackage::getName)
                .contains(packageBR.getName())
                .doesNotContain(packageAR.getName());
    }

    @Test
    void search_filtersByDestinationCountryIso() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        Country brasil = countryRepository.save(aCountry().withIsoCode("BR").withName("Brasil").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());
        City rioDeJaneiro = cityRepository.save(aCity().withName("Rio de Janeiro").withCountry(brasil).build());

        Hotel hotelAR = hotelRepository.save(HotelBuilder.aHotel().withName("Hotel BRC").withCity(bariloche).build());
        Hotel hotelBR = hotelRepository.save(HotelBuilder.aHotel().withName("Hotel RIO").withCity(rioDeJaneiro).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("destcountry@test.com").withTaxId("30-70000002-2").build());

        Flight depFlightAR = flightRepository.save(FlightBuilder.aFlight().withId(305L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlightAR = flightRepository.save(FlightBuilder.aFlight().withId(306L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        Flight depFlightBR = flightRepository.save(FlightBuilder.aFlight().withId(307L).withOriginCity(buenosAires).withDestinationCity(rioDeJaneiro).build());
        Flight retFlightBR = flightRepository.save(FlightBuilder.aFlight().withId(308L).withOriginCity(rioDeJaneiro).withDestinationCity(buenosAires).build());

        TravelPackage packageAR = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Destino Bariloche AR")
                .withHotel(hotelAR).withAgency(agency).withDepartureFlight(depFlightAR).withReturnFlight(retFlightAR).build());

        TravelPackage packageBR = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Destino Rio BR")
                .withHotel(hotelBR).withAgency(agency).withDepartureFlight(depFlightBR).withReturnFlight(retFlightBR).build());

        TravelPackageFilterDTO filterAR = aFilter().withDestinationCountryIso("AR").build();
        Page<TravelPackage> resultAR = travelPackageService.search(filterAR, PageRequest.of(0, 10));
        assertThat(resultAR.getContent()).extracting(TravelPackage::getName)
                .contains(packageAR.getName())
                .doesNotContain(packageBR.getName());

        TravelPackageFilterDTO filterBR = aFilter().withDestinationCountryIso("br").build();
        Page<TravelPackage> resultBR = travelPackageService.search(filterBR, PageRequest.of(0, 10));
        assertThat(resultBR.getContent()).extracting(TravelPackage::getName)
                .contains(packageBR.getName())
                .doesNotContain(packageAR.getName());
    }

    @Test
    void search_filtersByOriginCityId_whenSameCountry() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City cordoba = cityRepository.save(aCity().withName("Cordoba").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("origincity@test.com").withTaxId("30-70000003-3").build());

        Flight depFlightBUE = flightRepository.save(FlightBuilder.aFlight().withId(309L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlightBUE = flightRepository.save(FlightBuilder.aFlight().withId(310L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        Flight depFlightCOR = flightRepository.save(FlightBuilder.aFlight().withId(311L).withOriginCity(cordoba).withDestinationCity(bariloche).build());
        Flight retFlightCOR = flightRepository.save(FlightBuilder.aFlight().withId(312L).withOriginCity(bariloche).withDestinationCity(cordoba).build());

        TravelPackage packageBUE = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Desde Buenos Aires")
                .withHotel(hotel).withAgency(agency).withDepartureFlight(depFlightBUE).withReturnFlight(retFlightBUE).build());

        TravelPackage packageCOR = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Desde Cordoba")
                .withHotel(hotel).withAgency(agency).withDepartureFlight(depFlightCOR).withReturnFlight(retFlightCOR).build());

        TravelPackageFilterDTO filterBUE = aFilter().withOriginCityId(buenosAires.getId()).build();
        Page<TravelPackage> resultBUE = travelPackageService.search(filterBUE, PageRequest.of(0, 10));
        assertThat(resultBUE.getContent()).extracting(TravelPackage::getName)
                .contains(packageBUE.getName())
                .doesNotContain(packageCOR.getName());

        TravelPackageFilterDTO filterCOR = aFilter().withOriginCityId(cordoba.getId()).build();
        Page<TravelPackage> resultCOR = travelPackageService.search(filterCOR, PageRequest.of(0, 10));
        assertThat(resultCOR.getContent()).extracting(TravelPackage::getName)
                .contains(packageCOR.getName())
                .doesNotContain(packageBUE.getName());
    }

    @Test
    void search_filtersByDestinationCityId_whenSameCountry() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());
        City mendoza = cityRepository.save(aCity().withName("Mendoza").withCountry(argentina).build());

        Hotel hotelBRC = hotelRepository.save(HotelBuilder.aHotel().withName("Hotel BRC").withCity(bariloche).build());
        Hotel hotelMDZ = hotelRepository.save(HotelBuilder.aHotel().withName("Hotel MDZ").withCity(mendoza).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("destcity@test.com").withTaxId("30-70000004-4").build());

        Flight depFlightBRC = flightRepository.save(FlightBuilder.aFlight().withId(313L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlightBRC = flightRepository.save(FlightBuilder.aFlight().withId(314L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        Flight depFlightMDZ = flightRepository.save(FlightBuilder.aFlight().withId(315L).withOriginCity(buenosAires).withDestinationCity(mendoza).build());
        Flight retFlightMDZ = flightRepository.save(FlightBuilder.aFlight().withId(316L).withOriginCity(mendoza).withDestinationCity(buenosAires).build());

        TravelPackage packageBRC = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Destino Bariloche City")
                .withHotel(hotelBRC).withAgency(agency).withDepartureFlight(depFlightBRC).withReturnFlight(retFlightBRC).build());

        TravelPackage packageMDZ = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Destino Mendoza City")
                .withHotel(hotelMDZ).withAgency(agency).withDepartureFlight(depFlightMDZ).withReturnFlight(retFlightMDZ).build());

        TravelPackageFilterDTO filterBRC = aFilter().withDestinationCityId(bariloche.getId()).build();
        Page<TravelPackage> resultBRC = travelPackageService.search(filterBRC, PageRequest.of(0, 10));
        assertThat(resultBRC.getContent()).extracting(TravelPackage::getName)
                .contains(packageBRC.getName())
                .doesNotContain(packageMDZ.getName());

        TravelPackageFilterDTO filterMDZ = aFilter().withDestinationCityId(mendoza.getId()).build();
        Page<TravelPackage> resultMDZ = travelPackageService.search(filterMDZ, PageRequest.of(0, 10));
        assertThat(resultMDZ.getContent()).extracting(TravelPackage::getName)
                .contains(packageMDZ.getName())
                .doesNotContain(packageBRC.getName());
    }

    @Test
    void search_filtersByDepartureDateRange() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("depdates@test.com").withTaxId("30-70000005-5").build());

        Flight depFlight1 = flightRepository.save(FlightBuilder.aFlight().withId(317L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight1 = flightRepository.save(FlightBuilder.aFlight().withId(318L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        Flight depFlight2 = flightRepository.save(FlightBuilder.aFlight().withId(319L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight2 = flightRepository.save(FlightBuilder.aFlight().withId(320L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage packageNov = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Paquete Noviembre")
                .withStartDate(LocalDateTime.of(2026, 11, 1, 10, 0))
                .withEndDate(LocalDateTime.of(2026, 11, 10, 10, 0))
                .withHotel(hotel).withAgency(agency).withDepartureFlight(depFlight1).withReturnFlight(retFlight1).build());

        TravelPackage packageDec = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Paquete Diciembre")
                .withStartDate(LocalDateTime.of(2026, 12, 1, 10, 0))
                .withEndDate(LocalDateTime.of(2026, 12, 10, 10, 0))
                .withHotel(hotel).withAgency(agency).withDepartureFlight(depFlight2).withReturnFlight(retFlight2).build());

        TravelPackageFilterDTO filterNov = aFilter()
                .withDepartureFrom(LocalDateTime.of(2026, 10, 25, 0, 0))
                .withDepartureTo(LocalDateTime.of(2026, 11, 5, 23, 59))
                .build();
        Page<TravelPackage> resultNov = travelPackageService.search(filterNov, PageRequest.of(0, 10));
        assertThat(resultNov.getContent()).extracting(TravelPackage::getName)
                .contains(packageNov.getName())
                .doesNotContain(packageDec.getName());

        TravelPackageFilterDTO filterDec = aFilter()
                .withDepartureFrom(LocalDateTime.of(2026, 11, 20, 0, 0))
                .withDepartureTo(LocalDateTime.of(2026, 12, 5, 23, 59))
                .build();
        Page<TravelPackage> resultDec = travelPackageService.search(filterDec, PageRequest.of(0, 10));
        assertThat(resultDec.getContent()).extracting(TravelPackage::getName)
                .contains(packageDec.getName())
                .doesNotContain(packageNov.getName());
    }

    @Test
    void search_filtersByArrivalDateRange() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("arrdates@test.com").withTaxId("30-70000006-6").build());

        Flight depFlight1 = flightRepository.save(FlightBuilder.aFlight().withId(321L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight1 = flightRepository.save(FlightBuilder.aFlight().withId(322L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        Flight depFlight2 = flightRepository.save(FlightBuilder.aFlight().withId(323L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight2 = flightRepository.save(FlightBuilder.aFlight().withId(324L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        TravelPackage packageEarly = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Regreso Temprano")
                .withStartDate(LocalDateTime.of(2026, 11, 1, 10, 0))
                .withEndDate(LocalDateTime.of(2026, 11, 10, 10, 0))
                .withHotel(hotel).withAgency(agency).withDepartureFlight(depFlight1).withReturnFlight(retFlight1).build());

        TravelPackage packageLate = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Regreso Tardio")
                .withStartDate(LocalDateTime.of(2026, 11, 1, 10, 0))
                .withEndDate(LocalDateTime.of(2026, 11, 28, 10, 0))
                .withHotel(hotel).withAgency(agency).withDepartureFlight(depFlight2).withReturnFlight(retFlight2).build());

        TravelPackageFilterDTO filterEarly = aFilter()
                .withArrivalFrom(LocalDateTime.of(2026, 11, 5, 0, 0))
                .withArrivalTo(LocalDateTime.of(2026, 11, 15, 23, 59))
                .build();
        Page<TravelPackage> resultEarly = travelPackageService.search(filterEarly, PageRequest.of(0, 10));
        assertThat(resultEarly.getContent()).extracting(TravelPackage::getName)
                .contains(packageEarly.getName())
                .doesNotContain(packageLate.getName());

        TravelPackageFilterDTO filterLate = aFilter()
                .withArrivalFrom(LocalDateTime.of(2026, 11, 20, 0, 0))
                .withArrivalTo(LocalDateTime.of(2026, 11, 30, 23, 59))
                .build();
        Page<TravelPackage> resultLate = travelPackageService.search(filterLate, PageRequest.of(0, 10));
        assertThat(resultLate.getContent()).extracting(TravelPackage::getName)
                .contains(packageLate.getName())
                .doesNotContain(packageEarly.getName());
    }

    @Test
    void search_combinesMultipleFilters() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        Country brasil = countryRepository.save(aCountry().withIsoCode("BR").withName("Brasil").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City saoPaulo = cityRepository.save(aCity().withName("Sao Paulo").withCountry(brasil).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());
        City rioDeJaneiro = cityRepository.save(aCity().withName("Rio de Janeiro").withCountry(brasil).build());

        Hotel hotelAR = hotelRepository.save(HotelBuilder.aHotel().withName("Hotel AR").withCity(bariloche).build());
        Hotel hotelBR = hotelRepository.save(HotelBuilder.aHotel().withName("Hotel BR").withCity(rioDeJaneiro).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().withEmail("combofilter@test.com").withTaxId("30-70000007-7").build());

        Flight dep1 = flightRepository.save(FlightBuilder.aFlight().withId(325L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight ret1 = flightRepository.save(FlightBuilder.aFlight().withId(326L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        Flight dep2 = flightRepository.save(FlightBuilder.aFlight().withId(327L).withOriginCity(buenosAires).withDestinationCity(rioDeJaneiro).build());
        Flight ret2 = flightRepository.save(FlightBuilder.aFlight().withId(328L).withOriginCity(rioDeJaneiro).withDestinationCity(buenosAires).build());

        Flight dep3 = flightRepository.save(FlightBuilder.aFlight().withId(329L).withOriginCity(saoPaulo).withDestinationCity(bariloche).build());
        Flight ret3 = flightRepository.save(FlightBuilder.aFlight().withId(330L).withOriginCity(bariloche).withDestinationCity(saoPaulo).build());

        TravelPackage match = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Paquete Match")
                .withStartDate(LocalDateTime.of(2026, 11, 1, 10, 0))
                .withEndDate(LocalDateTime.of(2026, 11, 10, 10, 0))
                .withHotel(hotelAR).withAgency(agency).withDepartureFlight(dep1).withReturnFlight(ret1).build());

        TravelPackage differentDestination = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Paquete Diff Dest")
                .withStartDate(LocalDateTime.of(2026, 11, 1, 10, 0))
                .withEndDate(LocalDateTime.of(2026, 11, 10, 10, 0))
                .withHotel(hotelBR).withAgency(agency).withDepartureFlight(dep2).withReturnFlight(ret2).build());

        TravelPackage differentOriginAndDate = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Paquete Diff Origin")
                .withStartDate(LocalDateTime.of(2026, 12, 1, 10, 0))
                .withEndDate(LocalDateTime.of(2026, 12, 10, 10, 0))
                .withHotel(hotelAR).withAgency(agency).withDepartureFlight(dep3).withReturnFlight(ret3).build());

        TravelPackageFilterDTO filter = aFilter()
                .withOriginCountryIso("AR")
                .withDestinationCountryIso("AR")
                .withDestinationCityId(bariloche.getId())
                .withDepartureFrom(LocalDateTime.of(2026, 10, 20, 0, 0))
                .withDepartureTo(LocalDateTime.of(2026, 11, 15, 23, 59))
                .build();

        Page<TravelPackage> result = travelPackageService.search(filter, PageRequest.of(0, 10));
        assertThat(result.getContent()).extracting(TravelPackage::getName)
                .containsExactly(match.getName());
    }
}
