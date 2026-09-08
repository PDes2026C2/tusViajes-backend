package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.builder.CompradorBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.PaqueteBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.RegistroCompradorRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.CompradorResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PaqueteResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.Comprador;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.Paquete;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import ar.edu.unq.tusViajes.repository.CompradorRepository;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Transactional 
class CompradorServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CompradorService compradorService;

    @Autowired
    private CompradorRepository compradorRepository;

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Test
    void listar_retornaTodosLosCompradores() {
        Comprador comprador = compradorRepository.save(CompradorBuilder.aComprador().build());

        List<CompradorResponseDTO> resultado = compradorService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().nombre()).isEqualTo(comprador.getNombre());
        assertThat(resultado.getFirst().email()).isEqualTo(comprador.getEmail());
    }

    @Test
    void buscarPorId_devuelveCompradorCuandoExiste() {
        Comprador comprador = compradorRepository.save(CompradorBuilder.aComprador().build());

        CompradorResponseDTO resultado = compradorService.buscarPorId(comprador.getId());

        assertThat(resultado.nombre()).isEqualTo(comprador.getNombre());
        assertThat(resultado.email()).isEqualTo(comprador.getEmail());
        assertThat(resultado.dni()).isEqualTo(comprador.getDni());
    }

    @Test
    void buscarPorId_lanzaExcepcionCuandoNoExiste() {
        assertThatThrownBy(() -> compradorService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void registrar_guardaCompradorConPasswordHasheado() {
        RegistroCompradorRequestDTO dto = new RegistroCompradorRequestDTO(
                "Lucas", "Gomez", "lucas@example.com", "secret123", "1122334455", "37111222"
        );

        CompradorResponseDTO resultado = compradorService.registrar(dto);

        assertThat(resultado.nombre()).isEqualTo("Lucas");
        assertThat(resultado.email()).isEqualTo("lucas@example.com");
        assertThat(resultado.dni()).isEqualTo("37111222");

        Comprador guardadoEnDb = compradorRepository.findById(resultado.id()).orElseThrow();
        assertThat(guardadoEnDb.getPasswordHash()).isNotEqualTo("secret123");
    }

    @Test
    void registrar_lanzaExcepcionSiEmailEstaDuplicado() {
        Comprador compradorExistente = CompradorBuilder.aComprador()
                .withEmail("repetido@example.com")
                .build();
        compradorRepository.save(compradorExistente);

        RegistroCompradorRequestDTO dto = new RegistroCompradorRequestDTO(
                "Lucas", "Gomez", "repetido@example.com", "secret123", "1122334455", "37111222"
        );

        assertThatThrownBy(() -> compradorService.registrar(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("repetido@example.com");
    }

    @Test
    void agregarFavorito_asociaElPaqueteAlComprador() {
        Comprador comprador = compradorRepository.save(CompradorBuilder.aComprador().build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        Paquete paquete = paqueteRepository.save(PaqueteBuilder.aPaquete().withHotel(hotel).withAgencia(agencia).build());

        compradorService.agregarFavorito(comprador.getId(), paquete.getId());

        Comprador compradorActualizado = compradorRepository.findById(comprador.getId()).orElseThrow();
        assertThat(compradorActualizado.getPaquetesFavoritos()).hasSize(1);
        assertThat(compradorActualizado.getPaquetesFavoritos().iterator().next().getId()).isEqualTo(paquete.getId());
    }

    @Test
    void quitarFavorito_desasociaElPaqueteDelComprador() {
        Comprador comprador = compradorRepository.save(CompradorBuilder.aComprador().build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        Paquete paquete = paqueteRepository.save(PaqueteBuilder.aPaquete().withHotel(hotel).withAgencia(agencia).build());

        comprador.agregarFavorito(paquete);
        comprador = compradorRepository.save(comprador);

        compradorService.quitarFavorito(comprador.getId(), paquete.getId());

        Comprador compradorActualizado = compradorRepository.findById(comprador.getId()).orElseThrow();
        assertThat(compradorActualizado.getPaquetesFavoritos()).isEmpty();
    }

    @Test
    void listarFavoritos_devuelveListaDePaquetesResponseDTO() {
        Comprador comprador = compradorRepository.save(CompradorBuilder.aComprador().build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        Paquete paquete = paqueteRepository.save(PaqueteBuilder.aPaquete().withNombre("Promo Bariloche").withHotel(hotel).withAgencia(agencia).build());

        comprador.agregarFavorito(paquete);
        compradorRepository.save(comprador);

        List<PaqueteResponseDTO> favoritos = compradorService.listarFavoritos(comprador.getId());

        assertThat(favoritos).hasSize(1);
        assertThat(favoritos.get(0).getNombre()).isEqualTo("Promo Bariloche");
    }

    @Test
    void agregarFavorito_lanzaExcepcionSiCompradorNoExiste() {
        assertThatThrownBy(() -> compradorService.agregarFavorito(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comprador");
    }
}
