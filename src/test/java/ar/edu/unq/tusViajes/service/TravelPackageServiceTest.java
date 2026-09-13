package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
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

    @Test
    void getAll_returnsAllAvailableTravelPackages() {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().withHotel(hotel).withAgency(agency).build();
        travelPackageRepository.save(travelPackage);

        Page<TravelPackageResponseDTO> result = travelPackageService.search(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo(travelPackage.getName());
    }

    @Test
    void getById_returnsTravelPackageWhenExists() {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().withHotel(hotel).withAgency(agency).build();
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
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());

        TravelPackageRequestDTO dto = new TravelPackageRequestDTO(
                "Viaje a Cataratas", "All inclusive", 200000.0,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10),
                hotel.getId(), agency.getId()
        );

        TravelPackageResponseDTO result = travelPackageService.create(dto);

        assertThat(result.getName()).isEqualTo("Viaje a Cataratas");
        assertThat(result.getPrice()).isEqualTo(200000.0);
        
        assertThat(travelPackageRepository.existsById(result.getId())).isTrue();
    }
}
