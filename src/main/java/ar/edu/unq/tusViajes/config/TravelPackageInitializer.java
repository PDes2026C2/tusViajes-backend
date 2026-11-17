package ar.edu.unq.tusViajes.config;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.FlightRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import ar.edu.unq.tusViajes.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("!test")
@Order(3)
@RequiredArgsConstructor
public class TravelPackageInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TravelPackageInitializer.class);

    private final TravelPackageRepository travelPackageRepository;
    private final AgencyRepository agencyRepository;
    private final HotelRepository hotelRepository;
    private final FlightRepository flightRepository;
    private final FlightService flightService;
    private final CityRepository cityRepository;

    @Override
    public void run(String... args) {
        if (travelPackageRepository.count() == 0) {
            seedTravelPackages();
        }
    }

    private void seedTravelPackages() {
        Agency agency = agencyRepository.findByEmail("agency@tusviajes.com")
                .or(() -> agencyRepository.findAll().stream().findFirst())
                .orElse(null);

        if (agency == null) {
            logger.warn("No agency found to assign to travel packages. Skipping package initialization.");
            return;
        }

        seedPackages(agency);

        logger.info("Travel packages successfully initialized (total: {})", travelPackageRepository.count());
    }

    private void seedPackages(Agency agency) {
        // Paquetes a Córdoba (Ciudad 2)
        createPackage(
                "Escapada a Córdoba Colonial",
                "Disfrutá de 5 días en Córdoba con vuelo directo por Aerolíneas Argentinas y estadía en Azur Real Hotel Boutique con spa y baños subterráneos.",
                185000.0,
                1L, "Aerolíneas Argentinas", 1L, 2L, LocalDateTime.of(2026, 12, 1, 8, 0), LocalDateTime.of(2026, 12, 1, 9, 30),
                2L, "Aerolíneas Argentinas", 2L, 1L, LocalDateTime.of(2026, 12, 5, 18, 0), LocalDateTime.of(2026, 12, 5, 19, 30),
                "Azur Real Hotel Boutique", 2L, agency
        );
        createPackage(
                "Córdoba Serrana y Relax",
                "Paquete completo de 6 días en Córdoba volando por LATAM con alojamiento en Azur Real Hotel Boutique y desayuno artesanal incluido.",
                195000.0,
                3L, "LATAM", 1L, 2L, LocalDateTime.of(2026, 12, 10, 10, 0), LocalDateTime.of(2026, 12, 10, 11, 30),
                4L, "LATAM", 2L, 1L, LocalDateTime.of(2026, 12, 15, 20, 0), LocalDateTime.of(2026, 12, 15, 21, 30),
                "Azur Real Hotel Boutique", 2L, agency
        );

        // Paquetes a Mendoza (Ciudad 4)
        createPackage(
                "Mendoza: Ruta del Vino y Cordillera",
                "Viví 9 días increíbles en Mendoza. Vuelo con Aerolíneas Argentinas y estadía en el exclusivo Park Hyatt Mendoza Hotel, Casino & Spa.",
                340000.0,
                5L, "Aerolíneas Argentinas", 1L, 4L, LocalDateTime.of(2026, 12, 2, 7, 0), LocalDateTime.of(2026, 12, 2, 9, 0),
                6L, "Aerolíneas Argentinas", 4L, 1L, LocalDateTime.of(2026, 12, 10, 19, 0), LocalDateTime.of(2026, 12, 10, 21, 0),
                "Park Hyatt Mendoza Hotel, Casino & Spa", 4L, agency
        );
        createPackage(
                "Experiencia Gourmet en Mendoza",
                "Semana inolvidable en Mendoza volando por LATAM con alojamiento en Sheraton Mendoza Hotel con vista panorámica y piscina cubierta.",
                310000.0,
                7L, "LATAM", 1L, 4L, LocalDateTime.of(2026, 12, 5, 9, 0), LocalDateTime.of(2026, 12, 5, 11, 0),
                8L, "LATAM", 4L, 1L, LocalDateTime.of(2026, 12, 12, 15, 0), LocalDateTime.of(2026, 12, 12, 17, 0),
                "Sheraton Mendoza Hotel", 4L, agency
        );

        // Paquetes a São Paulo (Ciudad 11)
        createPackage(
                "São Paulo Urbano y Diseño",
                "Descubrí la metrópolis de São Paulo volando con LATAM y alojándote en el icónico Hotel Unique con arquitectura moderna y terraza panorámica.",
                420000.0,
                9L, "LATAM", 1L, 11L, LocalDateTime.of(2026, 12, 3, 14, 0), LocalDateTime.of(2026, 12, 3, 16, 45),
                10L, "LATAM", 11L, 1L, LocalDateTime.of(2026, 12, 8, 9, 0), LocalDateTime.of(2026, 12, 8, 11, 45),
                "Hotel Unique", 11L, agency
        );
        createPackage(
                "Navidad en São Paulo",
                "Festejá las fiestas en São Paulo con vuelos de Aerolíneas Argentinas y estadía de lujo en Hotel Unique con Sky Restaurant & Bar.",
                460000.0,
                11L, "Aerolíneas Argentinas", 1L, 11L, LocalDateTime.of(2026, 12, 20, 16, 0), LocalDateTime.of(2026, 12, 20, 18, 45),
                12L, "Aerolíneas Argentinas", 11L, 1L, LocalDateTime.of(2026, 12, 25, 10, 0), LocalDateTime.of(2026, 12, 25, 12, 45),
                "Hotel Unique", 11L, agency
        );
        createPackage(
                "Desde Córdoba a São Paulo",
                "Vuelo directo desde Córdoba hacia São Paulo por Aerolíneas Argentinas y 7 noches en Hotel Unique con amenities de primer nivel.",
                480000.0,
                17L, "Aerolíneas Argentinas", 2L, 11L, LocalDateTime.of(2026, 12, 15, 13, 0), LocalDateTime.of(2026, 12, 15, 16, 0),
                18L, "Aerolíneas Argentinas", 11L, 2L, LocalDateTime.of(2026, 12, 22, 9, 0), LocalDateTime.of(2026, 12, 22, 12, 0),
                "Hotel Unique", 11L, agency
        );
        createPackage(
                "Córdoba a Brasil: São Paulo Express",
                "8 días inolvidables conectando Córdoba con São Paulo vía LATAM y alojándote en el vanguardista Hotel Unique.",
                495000.0,
                19L, "LATAM", 2L, 11L, LocalDateTime.of(2026, 12, 10, 13, 0), LocalDateTime.of(2026, 12, 10, 16, 0),
                20L, "LATAM", 11L, 2L, LocalDateTime.of(2026, 12, 18, 9, 0), LocalDateTime.of(2026, 12, 18, 12, 0),
                "Hotel Unique", 11L, agency
        );

        // Paquetes a Río de Janeiro (Ciudad 12)
        createPackage(
                "Escapada Carioca en Copacabana",
                "Fin de semana soñado en Río de Janeiro volando por LATAM con estadía en el legendario Copacabana Palace frente al mar.",
                520000.0,
                13L, "LATAM", 11L, 12L, LocalDateTime.of(2026, 12, 4, 8, 0), LocalDateTime.of(2026, 12, 4, 9, 15),
                14L, "LATAM", 12L, 11L, LocalDateTime.of(2026, 12, 7, 18, 0), LocalDateTime.of(2026, 12, 7, 19, 15),
                "Copacabana Palace, A Belmond Hotel", 12L, agency
        );
        createPackage(
                "Sol y Playas de Río de Janeiro",
                "6 días a puro sol en Río de Janeiro con vuelos LATAM y hospedaje en Miramar Hotel by Windsor con piscina infinita en el rooftop.",
                390000.0,
                15L, "LATAM", 11L, 12L, LocalDateTime.of(2026, 12, 15, 8, 0), LocalDateTime.of(2026, 12, 15, 9, 15),
                16L, "LATAM", 12L, 11L, LocalDateTime.of(2026, 12, 20, 18, 0), LocalDateTime.of(2026, 12, 20, 19, 15),
                "Miramar Hotel by Windsor", 12L, agency
        );

        // Paquetes a Madrid (Ciudad 31)
        createPackage(
                "Madrid Señorial y Cultura",
                "2 semanas explorando Madrid con vuelos transatlánticos en Iberia y alojamiento de lujo en The Westin Palace bajo su cúpula de cristal.",
                1450000.0,
                21L, "Iberia", 1L, 31L, LocalDateTime.of(2026, 12, 1, 22, 0), LocalDateTime.of(2026, 12, 2, 14, 0),
                22L, "Iberia", 31L, 1L, LocalDateTime.of(2026, 12, 15, 23, 50), LocalDateTime.of(2026, 12, 16, 8, 30),
                "The Westin Palace, Madrid", 31L, agency
        );
        createPackage(
                "Madrid Mágico con Aerolíneas Argentinas",
                "15 días en la capital española volando con Aerolíneas Argentinas y hospedaje en The Westin Palace en pleno barrio de Las Letras.",
                1390000.0,
                23L, "Aerolíneas Argentinas", 1L, 31L, LocalDateTime.of(2026, 12, 5, 23, 30), LocalDateTime.of(2026, 12, 6, 15, 30),
                24L, "Aerolíneas Argentinas", 31L, 1L, LocalDateTime.of(2026, 12, 20, 19, 0), LocalDateTime.of(2026, 12, 21, 4, 0),
                "The Westin Palace, Madrid", 31L, agency
        );
        createPackage(
                "São Paulo a Madrid: Europa Navideña",
                "Viví la magia de Europa estas fiestas. Vuelo directo LATAM desde São Paulo y 15 días en The Westin Palace de Madrid.",
                1550000.0,
                25L, "LATAM", 11L, 31L, LocalDateTime.of(2026, 12, 10, 20, 0), LocalDateTime.of(2026, 12, 11, 10, 30),
                26L, "LATAM", 31L, 11L, LocalDateTime.of(2026, 12, 25, 15, 0), LocalDateTime.of(2026, 12, 26, 1, 30),
                "The Westin Palace, Madrid", 31L, agency
        );
        createPackage(
                "Madrid Clásico desde Brasil con Iberia",
                "Paquete internacional volando con Iberia desde São Paulo hacia Madrid con estadía premium en The Westin Palace.",
                1490000.0,
                27L, "Iberia", 11L, 31L, LocalDateTime.of(2026, 12, 1, 20, 0), LocalDateTime.of(2026, 12, 2, 10, 30),
                28L, "Iberia", 31L, 11L, LocalDateTime.of(2026, 12, 15, 15, 0), LocalDateTime.of(2026, 12, 16, 1, 30),
                "The Westin Palace, Madrid", 31L, agency
        );

        // Paquetes a Nueva York (Ciudad 21)
        createPackage(
                "Navidad de Película en Nueva York",
                "Pasá dos semanas inolvidables en Nueva York durante las fiestas. Vuelo por American Airlines y estadía histórica en The Plaza frente a Central Park.",
                2100000.0,
                29L, "American Airlines", 1L, 21L, LocalDateTime.of(2026, 12, 12, 21, 0), LocalDateTime.of(2026, 12, 13, 7, 0),
                30L, "American Airlines", 21L, 1L, LocalDateTime.of(2026, 12, 26, 22, 0), LocalDateTime.of(2026, 12, 27, 9, 30),
                "The Plaza New York", 21L, agency
        );
        createPackage(
                "Otoño en Manhattan: Central Park y Más",
                "14 días en la Gran Manzana con Aerolíneas Argentinas y alojamiento en The Plaza New York con servicio de mayordomo y spa.",
                1980000.0,
                31L, "Aerolíneas Argentinas", 1L, 21L, LocalDateTime.of(2026, 11, 12, 21, 0), LocalDateTime.of(2026, 11, 13, 7, 0),
                32L, "Aerolíneas Argentinas", 21L, 1L, LocalDateTime.of(2026, 11, 26, 22, 0), LocalDateTime.of(2026, 11, 27, 9, 30),
                "The Plaza New York", 21L, agency
        );

        // Paquetes a París (Ciudad 61)
        createPackage(
                "París: Luces y Alta Cocina desde Nueva York",
                "Viaje transatlántico en Air France hacia París con 12 días en el mítico The Ritz Paris con spa Chanel y jardines privados.",
                2450000.0,
                37L, "Air France", 21L, 61L, LocalDateTime.of(2026, 12, 8, 17, 0), LocalDateTime.of(2026, 12, 9, 6, 30),
                38L, "Air France", 61L, 21L, LocalDateTime.of(2026, 12, 20, 14, 0), LocalDateTime.of(2026, 12, 20, 16, 30),
                "The Ritz Paris", 61L, agency
        );
        createPackage(
                "París Inolvidable con American Airlines",
                "12 días maravillosos en la Ciudad de la Luz con vuelos American Airlines y hospedaje de máximo lujo en The Ritz Paris.",
                2380000.0,
                39L, "American Airlines", 21L, 61L, LocalDateTime.of(2026, 12, 8, 17, 0), LocalDateTime.of(2026, 12, 9, 6, 30),
                40L, "American Airlines", 61L, 21L, LocalDateTime.of(2026, 12, 20, 14, 0), LocalDateTime.of(2026, 12, 20, 16, 30),
                "The Ritz Paris", 61L, agency
        );
        createPackage(
                "Escapada a París: Arte y Gastronomía",
                "4 días express en París volando por Air France desde Madrid y disfrutando del glamour incomparable de The Ritz Paris.",
                890000.0,
                41L, "Air France", 31L, 61L, LocalDateTime.of(2026, 10, 1, 9, 0), LocalDateTime.of(2026, 10, 1, 11, 15),
                42L, "Air France", 61L, 31L, LocalDateTime.of(2026, 10, 5, 18, 0), LocalDateTime.of(2026, 10, 5, 20, 15),
                "The Ritz Paris", 61L, agency
        );
        createPackage(
                "París de Otoño con Iberia",
                "Vuelo Iberia de Madrid a París y estadía en The Ritz Paris en Place Vendôme con spa y alta cocina.",
                920000.0,
                43L, "Iberia", 31L, 61L, LocalDateTime.of(2026, 11, 1, 9, 0), LocalDateTime.of(2026, 11, 1, 11, 15),
                44L, "Iberia", 61L, 31L, LocalDateTime.of(2026, 11, 5, 18, 0), LocalDateTime.of(2026, 11, 5, 20, 15),
                "The Ritz Paris", 61L, agency
        );

        // Paquetes a Roma (Ciudad 51)
        createPackage(
                "Roma Eterna: Historia y Jardines",
                "5 días en la Ciudad Eterna volando por Iberia con hospedaje en el emblemático Hotel de Russie junto a Piazza del Popolo.",
                850000.0,
                45L, "Iberia", 31L, 51L, LocalDateTime.of(2026, 10, 10, 10, 0), LocalDateTime.of(2026, 10, 10, 12, 30),
                46L, "Iberia", 51L, 31L, LocalDateTime.of(2026, 10, 15, 16, 0), LocalDateTime.of(2026, 10, 15, 18, 30),
                "Hotel de Russie", 51L, agency
        );
        createPackage(
                "Diciembre en Roma con Alitalia",
                "Descubrí Roma en vísperas navideñas con vuelo Alitalia desde Madrid y estadía de lujo en Hotel de Russie con jardín secreto y spa.",
                870000.0,
                47L, "Alitalia", 31L, 51L, LocalDateTime.of(2026, 12, 10, 10, 0), LocalDateTime.of(2026, 12, 10, 12, 30),
                48L, "Alitalia", 51L, 31L, LocalDateTime.of(2026, 12, 15, 16, 0), LocalDateTime.of(2026, 12, 15, 18, 30),
                "Hotel de Russie", 51L, agency
        );

        // Paquete a Buenos Aires (Ciudad 1)
        createPackage(
                "Buenos Aires Cultural: Teatro y Tango",
                "Escapada a Buenos Aires desde Córdoba combinando vuelos y alojamiento exclusivo en Alvear Palace Hotel con desayuno buffet.",
                210000.0,
                2L, "Aerolíneas Argentinas", 2L, 1L, LocalDateTime.of(2026, 12, 5, 18, 0), LocalDateTime.of(2026, 12, 5, 19, 30),
                3L, "LATAM", 1L, 2L, LocalDateTime.of(2026, 12, 10, 10, 0), LocalDateTime.of(2026, 12, 10, 11, 30),
                "Alvear Palace Hotel", 1L, agency
        );
    }

    private void createPackage(
            String name,
            String description,
            Double price,
            Long departureFlightId, String depAirline, Long depOriginId, Long depDestId, LocalDateTime depDeparture, LocalDateTime depArrival,
            Long returnFlightId, String retAirline, Long retOriginId, Long retDestId, LocalDateTime retDeparture, LocalDateTime retArrival,
            String preferredHotelName,
            Long destinationCityId,
            Agency agency
    ) {
        Flight departureFlight = resolveFlight(departureFlightId, depAirline, depOriginId, depDestId, depDeparture, depArrival);
        Flight returnFlight = resolveFlight(returnFlightId, retAirline, retOriginId, retDestId, retDeparture, retArrival);

        if (departureFlight == null || returnFlight == null) {
            logger.warn("Could not obtain flights {} and {} to create package {}", departureFlightId, returnFlightId, name);
            return;
        }

        Hotel hotel = hotelRepository.findAll().stream()
                .filter(h -> h.getName().equalsIgnoreCase(preferredHotelName))
                .findFirst()
                .orElseGet(() -> hotelRepository.findAll().stream()
                        .filter(h -> h.getCity() != null && h.getCity().getId().equals(destinationCityId))
                        .findFirst()
                        .orElse(null));

        if (hotel == null) {
            logger.warn("Could not find hotel for destination city {} to create package {}", destinationCityId, name);
            return;
        }

        TravelPackage travelPackage = new TravelPackage(
                name,
                description,
                price,
                departureFlight.getDepartureDate(),
                returnFlight.getArrivalDate(),
                hotel,
                agency,
                departureFlight,
                returnFlight
        );

        travelPackageRepository.save(travelPackage);
    }

    private Flight resolveFlight(Long flightId, String fallbackAirline, Long originCityId, Long destinationCityId,
                                 LocalDateTime fallbackDeparture, LocalDateTime fallbackArrival) {
        try {
            // Invoca al servicio de vuelos que se comunica con el servicio externo flights-backend (esquema flight)
            return flightService.getOrCreateFlight(flightId);
        } catch (Exception e) {
            logger.warn("Could not fetch flight {} from external flights-backend service: {}. Using fallback.", flightId, e.getMessage());
            return flightRepository.findById(flightId).orElseGet(() -> {
                City origin = cityRepository.findById(originCityId).orElse(null);
                City destination = cityRepository.findById(destinationCityId).orElse(null);
                if (origin != null && destination != null) {
                    return flightRepository.save(new Flight(flightId, fallbackAirline, origin, destination, fallbackDeparture, fallbackArrival));
                }
                return null;
            });
        }
    }
}
