package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.BuyerBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.BuyerRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.BuyerResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.repository.BuyerRepository;
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

import java.util.List;

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
    void register_savesBuyerWithHashedPassword() {
        BuyerRegistrationRequestDTO dto = new BuyerRegistrationRequestDTO(
                "Lucas", "Gomez", "lucas@example.com", "secret123", "1122334455", "37111222"
        );

        BuyerResponseDTO result = buyerService.register(dto);

        assertThat(result.firstName()).isEqualTo("Lucas");
        assertThat(result.email()).isEqualTo("lucas@example.com");
        assertThat(result.nationalId()).isEqualTo("37111222");

        Buyer savedInDb = buyerRepository.findById(result.id()).orElseThrow();
        assertThat(savedInDb.getPasswordHash()).isNotEqualTo("secret123");
    }

    @Test
    void register_throwsExceptionIfEmailIsDuplicated() {
        Buyer existingBuyer = BuyerBuilder.aBuyer()
                .withEmail("repetido@example.com")
                .build();
        buyerRepository.save(existingBuyer);

        BuyerRegistrationRequestDTO dto = new BuyerRegistrationRequestDTO(
                "Lucas", "Gomez", "repetido@example.com", "secret123", "1122334455", "37111222"
        );

        assertThatThrownBy(() -> buyerService.register(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("repetido@example.com");
    }

    @Test
    void addFavorite_associatesPackageToBuyer() {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage().withHotel(hotel).withAgency(agency).build());

        buyerService.addFavorite(buyer.getId(), travelPackage.getId());

        Buyer updatedBuyer = buyerRepository.findById(buyer.getId()).orElseThrow();
        assertThat(updatedBuyer.getFavoriteTravelPackages()).hasSize(1);
        assertThat(updatedBuyer.getFavoriteTravelPackages().iterator().next().getId()).isEqualTo(travelPackage.getId());
    }

    @Test
    void removeFavorite_disassociatesPackageFromBuyer() {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage().withHotel(hotel).withAgency(agency).build());

        buyer.addFavorite(travelPackage);
        buyer = buyerRepository.save(buyer);

        buyerService.removeFavorite(buyer.getId(), travelPackage.getId());

        Buyer updatedBuyer = buyerRepository.findById(buyer.getId()).orElseThrow();
        assertThat(updatedBuyer.getFavoriteTravelPackages()).isEmpty();
    }

    @Test
    void getFavorites_returnsListOfTravelPackageResponseDTOs() {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage().withName("Promo Bariloche").withHotel(hotel).withAgency(agency).build());

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
