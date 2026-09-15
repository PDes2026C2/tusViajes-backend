package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.CountryRepository;
import ar.edu.unq.tusViajes.builder.*;
import ar.edu.unq.tusViajes.controller.dto.request.BuyerRegistrationRequestDTO;
import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.BuyerBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.controller.dto.response.BuyerResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
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

import java.util.List;

import static ar.edu.unq.tusViajes.builder.CityBuilder.aCity;
import static ar.edu.unq.tusViajes.builder.CountryBuilder.aCountry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Transactional
class BuyerServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BuyerService buyerService;

    @Autowired
    private BuyerRepository buyerRepository;

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
    void getAll_returnsAllBuyers() {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());

        List<BuyerResponseDTO> result = buyerService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().firstName()).isEqualTo(buyer.getFirstName());
        assertThat(result.getFirst().email()).isEqualTo(buyer.getEmail());
    }

    @Test
    void getById_returnsBuyerWhenExists() {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());

        BuyerResponseDTO result = buyerService.getById(buyer.getId());

        assertThat(result.firstName()).isEqualTo(buyer.getFirstName());
        assertThat(result.email()).isEqualTo(buyer.getEmail());
        assertThat(result.nationalId()).isEqualTo(buyer.getNationalId());
    }

    @Test
    void getById_throwsExceptionWhenDoesNotExist() {
        assertThatThrownBy(() -> buyerService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addFavorite_associatesPackageToBuyer() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight departureFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage().withHotel(hotel).withAgency(agency).withDepartureFlight(departureFlight).withReturnFlight(returnFlight).build());

        buyerService.addFavorite(buyer.getId(), travelPackage.getId());

        Buyer updatedBuyer = buyerRepository.findById(buyer.getId()).orElseThrow();
        assertThat(updatedBuyer.getFavoriteTravelPackages()).hasSize(1);
        assertThat(updatedBuyer.getFavoriteTravelPackages().iterator().next().getId()).isEqualTo(travelPackage.getId());
    }

    @Test
    void removeFavorite_disassociatesPackageFromBuyer() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight departureFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage().withHotel(hotel).withAgency(agency).withDepartureFlight(departureFlight).withReturnFlight(returnFlight).build());

        buyer.addFavorite(travelPackage);
        buyer = buyerRepository.save(buyer);

        buyerService.removeFavorite(buyer.getId(), travelPackage.getId());

        Buyer updatedBuyer = buyerRepository.findById(buyer.getId()).orElseThrow();
        assertThat(updatedBuyer.getFavoriteTravelPackages()).isEmpty();
    }

    @Test
    void getFavorites_returnsListOfTravelPackageResponseDTOs() {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight departureFlight = flightRepository.save(FlightBuilder.aFlight().withId(1L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight returnFlight = flightRepository.save(FlightBuilder.aFlight().withId(2L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage().withName("Promo Bariloche").withHotel(hotel).withAgency(agency).withDepartureFlight(departureFlight).withReturnFlight(returnFlight).build());

        buyer.addFavorite(travelPackage);
        buyerRepository.save(buyer);

        List<TravelPackageResponseDTO> favorites = buyerService.getFavorites(buyer.getId());

        assertThat(favorites).hasSize(1);
        assertThat(favorites.get(0).getName()).isEqualTo("Promo Bariloche");
    }

    @Test
    void addFavorite_throwsExceptionIfBuyerDoesNotExist() {
        assertThatThrownBy(() -> buyerService.addFavorite(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Buyer");
    }
}
