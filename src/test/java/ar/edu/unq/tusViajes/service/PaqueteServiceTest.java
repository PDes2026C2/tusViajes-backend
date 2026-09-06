package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.PaqueteBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.PaqueteRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PaqueteResponseDTO;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.Paquete;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.repository.PaqueteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Transactional 
class PaqueteServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PaqueteService paqueteService;

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Test
    void listar_retornaTodosLosPaquetesDisponibles() {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        
        Paquete paquete = PaqueteBuilder.aPaquete().withHotel(hotel).withAgencia(agencia).build();
        paqueteRepository.save(paquete);

        List<PaqueteResponseDTO> resultado = paqueteService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo(paquete.getNombre());
    }

    @Test
    void buscarPorId_devuelvePaqueteCuandoExiste() {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        
        Paquete paquete = PaqueteBuilder.aPaquete().withHotel(hotel).withAgencia(agencia).build();
        Paquete guardado = paqueteRepository.save(paquete);

        PaqueteResponseDTO resultado = paqueteService.buscarPorId(guardado.getId());

        assertThat(resultado.getNombre()).isEqualTo(paquete.getNombre());
        assertThat(resultado.getPrecio()).isEqualTo(paquete.getPrecio());
    }

    @Test
    void buscarPorId_lanzaExcepcionCuandoNoExiste() {
        assertThatThrownBy(() -> paqueteService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void crear_guardaYDevuelvePaqueteConHotelYAgencia() {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());

        PaqueteRequestDTO dto = new PaqueteRequestDTO(
                "Viaje a Cataratas", "Todo incluido", 200000.0,
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10),
                hotel.getId(), agencia.getId()
        );

        PaqueteResponseDTO resultado = paqueteService.crear(dto);

        assertThat(resultado.getNombre()).isEqualTo("Viaje a Cataratas");
        assertThat(resultado.getPrecio()).isEqualTo(200000.0);
        
        assertThat(paqueteRepository.existsById(resultado.getId())).isTrue();
    }
}