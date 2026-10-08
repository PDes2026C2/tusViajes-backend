package ar.edu.unq.tusViajes.config;

import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@Order(2)
@RequiredArgsConstructor
public class HotelInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(HotelInitializer.class);

    private final HotelRepository hotelRepository;
    private final CityRepository cityRepository;

    @Override
    public void run(String... args) {
        if (hotelRepository.count() == 0) {
            seedHotels();
        }
    }

    private void seedHotels() {
        cityRepository.findById(1L).ifPresent(c -> {
            hotelRepository.save(new Hotel("Alvear Palace Hotel", c, "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80", "WiFi, Spa, Piscina, Desayuno buffet, Restaurante"));
            hotelRepository.save(new Hotel("Hilton Buenos Aires", c, "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=80", "WiFi, Gimnasio, Piscina climatizada, Room service"));
            hotelRepository.save(new Hotel("Palacio Duhau - Park Hyatt", c, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80", "WiFi, Spa, Jardines, Bar, Desayuno gourmet"));
        });
        cityRepository.findById(2L).ifPresent(c ->
            hotelRepository.save(new Hotel("Azur Real Hotel Boutique", c, "https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=800&q=80", "WiFi, Baños subterráneos, Spa, Desayuno artesanal"))
        );
        cityRepository.findById(3L).ifPresent(c -> {
            hotelRepository.save(new Hotel("Llao Llao Resort, Golf-Spa", c, "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80", "WiFi, Campo de Golf, Spa, Vista al lago, Piscina"));
            hotelRepository.save(new Hotel("Panamericano Bariloche", c, "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=800&q=80", "WiFi, Spa, Piscina climatizada, Casino"));
        });
        cityRepository.findById(4L).ifPresent(c -> {
            hotelRepository.save(new Hotel("Park Hyatt Mendoza Hotel, Casino & Spa", c, "https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80", "WiFi, Casino, Cava de vinos, Spa, Piscina"));
            hotelRepository.save(new Hotel("Sheraton Mendoza Hotel", c, "https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=800&q=80", "WiFi, Vista panorámica, Piscina cubierta, Restaurante gourmet"));
        });
        cityRepository.findById(6L).ifPresent(c ->
            hotelRepository.save(new Hotel("Hotel Alejandro I", c, "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?auto=format&fit=crop&w=800&q=80", "WiFi, Piscina, Gimnasio, Restaurante regional"))
        );
        cityRepository.findById(11L).ifPresent(c ->
            hotelRepository.save(new Hotel("Hotel Unique", c, "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=800&q=80", "WiFi, Sky restaurant & bar, Piscina roja, Arquitectura moderna"))
        );
        cityRepository.findById(12L).ifPresent(c -> {
            hotelRepository.save(new Hotel("Copacabana Palace, A Belmond Hotel", c, "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?auto=format&fit=crop&w=800&q=80", "WiFi, Frente al mar, Piscina olímpica, Spa, Tenis"));
            hotelRepository.save(new Hotel("Miramar Hotel by Windsor", c, "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80", "WiFi, Rooftop pool, Vista a Copacabana, Pet-friendly"));
        });
        cityRepository.findById(13L).ifPresent(c ->
            hotelRepository.save(new Hotel("Costão do Santinho Resort", c, "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=800&q=80", "All inclusive, Piscinas, Acceso a la playa, Spa, Shows"))
        );
        cityRepository.findById(21L).ifPresent(c ->
            hotelRepository.save(new Hotel("The Plaza New York", c, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80", "WiFi, Frente a Central Park, Servicio de mayordomo, Spa"))
        );
        cityRepository.findById(24L).ifPresent(c ->
            hotelRepository.save(new Hotel("1 Hotel South Beach", c, "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=80", "WiFi, Acceso directo a la playa, 4 piscinas, Spa orgánico"))
        );
        cityRepository.findById(31L).ifPresent(c ->
            hotelRepository.save(new Hotel("The Westin Palace, Madrid", c, "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=800&q=80", "WiFi, Cúpula de cristal, Restaurante, Gimnasio"))
        );
        cityRepository.findById(32L).ifPresent(c ->
            hotelRepository.save(new Hotel("W Barcelona", c, "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=800&q=80", "WiFi, Frente al mar, Infinity pool, Wet deck bar"))
        );
        cityRepository.findById(51L).ifPresent(c ->
            hotelRepository.save(new Hotel("Hotel de Russie", c, "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80", "WiFi, Jardín secreto, Spa, Vista a Plaza del Popolo"))
        );
        cityRepository.findById(61L).ifPresent(c ->
            hotelRepository.save(new Hotel("The Ritz Paris", c, "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=800&q=80", "WiFi, Spa Chanel, Jardines privados, Alta cocina"))
        );

        logger.info("Hotels successfully initialized (total: {})", hotelRepository.count());
    }
}
