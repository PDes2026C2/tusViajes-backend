package ar.edu.unq.tusViajes.config;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.FlightRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import ar.edu.unq.tusViajes.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TravelPackageInitializerTest {

    @Mock
    private TravelPackageRepository travelPackageRepository;

    @Mock
    private AgencyRepository agencyRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightService flightService;

    @Mock
    private CityRepository cityRepository;

    private TravelPackageInitializer travelPackageInitializer;

    private Agency agency;
    private City buenosAires;
    private City cordoba;
    private Hotel hotelCordoba;

    @BeforeEach
    void setUp() {
        travelPackageInitializer = new TravelPackageInitializer(
                travelPackageRepository,
                agencyRepository,
                hotelRepository,
                flightRepository,
                flightService,
                cityRepository
        );

        Country argentina = new Country("AR", "Argentina");
        buenosAires = new City(1L, "Buenos Aires", argentina);
        cordoba = new City(2L, "Córdoba", argentina);

        agency = new Agency(
                "agency@tusviajes.com",
                "encodedPassword",
                "Agency",
                "30-40663891-2",
                AgencyStatus.AUTHORIZED
        );

        hotelCordoba = new Hotel("Azur Real Hotel Boutique", cordoba, "https://example.com/photo.jpg", "WiFi, Spa");
    }

    @Test
    void run_createsTravelPackages_usingFlightService_whenNoPackagesExist() {
        when(travelPackageRepository.count()).thenReturn(0L);
        when(agencyRepository.findByEmail("agency@tusviajes.com")).thenReturn(Optional.of(agency));

        Flight flight1 = new Flight(1L, "Aerolíneas Argentinas", buenosAires, cordoba,
                LocalDateTime.of(2026, 12, 1, 8, 0), LocalDateTime.of(2026, 12, 1, 9, 30));
        Flight flight2 = new Flight(2L, "Aerolíneas Argentinas", cordoba, buenosAires,
                LocalDateTime.of(2026, 12, 5, 18, 0), LocalDateTime.of(2026, 12, 5, 19, 30));

        when(flightService.getOrCreateFlight(1L)).thenReturn(flight1);
        when(flightService.getOrCreateFlight(2L)).thenReturn(flight2);

        when(hotelRepository.findAll()).thenReturn(List.of(hotelCordoba));

        travelPackageInitializer.run();

        verify(flightService, atLeastOnce()).getOrCreateFlight(anyLong());
        verify(travelPackageRepository, atLeastOnce()).save(any(TravelPackage.class));
    }

    @Test
    void run_doesNotCreatePackages_whenPackagesAlreadyExist() {
        when(travelPackageRepository.count()).thenReturn(5L);

        travelPackageInitializer.run();

        verify(travelPackageRepository, never()).save(any());
        verify(flightService, never()).getOrCreateFlight(anyLong());
    }

    @Test
    void run_doesNotCreatePackages_whenAgencyNotFound() {
        when(travelPackageRepository.count()).thenReturn(0L);
        when(agencyRepository.findByEmail("agency@tusviajes.com")).thenReturn(Optional.empty());
        when(agencyRepository.findAll()).thenReturn(List.of());

        travelPackageInitializer.run();

        verify(travelPackageRepository, never()).save(any());
        verify(flightService, never()).getOrCreateFlight(anyLong());
    }
}
