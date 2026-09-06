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

@Testcontainers
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
    void listar_retornaTodosLosHotelesDeLaBaseDeDatos() {
        Hotel guardado = hotelRepository.save(HotelBuilder.aHotel().build());

        List<HotelResponseDTO> resultado = hotelService.listar();

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).nombre()).isEqualTo(guardado.getNombre());
    }

    @Test
    void buscarPorId_devuelveElHotelCuandoExisteEnPostgres() {
        Hotel guardado = hotelRepository.save(
                HotelBuilder.aHotel().withNombre("Hotel Central").withDestino("Bariloche").build()
        );

        HotelResponseDTO resultado = hotelService.buscarPorId(guardado.getId());

        assertThat(resultado.nombre()).isEqualTo("Hotel Central");
        assertThat(resultado.destino()).isEqualTo("Bariloche");
    }

    @Test
    void buscarPorId_lanzaExcepcionCuandoNoExisteEnPostgres() {
        assertThatThrownBy(() -> hotelService.buscarPorId(99999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void crear_guardaYDevuelveElHotelCreado() {
        HotelRequestDTO dto = new HotelRequestDTO("Hotel Nuevo", "Mendoza", null, null);

        HotelResponseDTO resultado = hotelService.crear(dto);

        assertThat(resultado.id()).isNotNull();
        assertThat(resultado.nombre()).isEqualTo("Hotel Nuevo");
        assertThat(resultado.destino()).isEqualTo("Mendoza");

        // Verificamos que realmente se haya persistido en el repositorio real
        assertThat(hotelRepository.existsById(resultado.id())).isTrue();
    }
}