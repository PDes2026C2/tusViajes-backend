package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.HotelRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.HotelResponseDTO;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.repository.HotelRepository;
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
class HotelServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private HotelService hotelService;

    @Autowired
    private HotelRepository hotelRepository;

    @Test
    void getAll_returnsAllHotelsFromDatabase() {
        Hotel saved = hotelRepository.save(HotelBuilder.aHotel().build());

        List<HotelResponseDTO> result = hotelService.getAll();

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).name()).isEqualTo(saved.getName());
    }

    @Test
    void getById_returnsHotelWhenExists() {
        Hotel saved = hotelRepository.save(
                HotelBuilder.aHotel().withName("Hotel Central").withDestination("Bariloche").build()
        );

        HotelResponseDTO result = hotelService.getById(saved.getId());

        assertThat(result.name()).isEqualTo("Hotel Central");
        assertThat(result.destination()).isEqualTo("Bariloche");
    }

    @Test
    void getById_throwsExceptionWhenDoesNotExist() {
        assertThatThrownBy(() -> hotelService.getById(99999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_savesAndReturnsCreatedHotel() {
        HotelRequestDTO dto = new HotelRequestDTO("Hotel Nuevo", "Mendoza", null, null);

        HotelResponseDTO result = hotelService.create(dto);

        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Hotel Nuevo");
        assertThat(result.destination()).isEqualTo("Mendoza");

        assertThat(hotelRepository.existsById(result.id())).isTrue();
    }
}