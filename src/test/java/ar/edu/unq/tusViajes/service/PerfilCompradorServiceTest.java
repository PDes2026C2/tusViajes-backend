package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.PaqueteBuilder;
import ar.edu.unq.tusViajes.builder.PerfilCompradorBuilder;
import ar.edu.unq.tusViajes.controller.dto.request.PerfilCompradorRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PaqueteResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PerfilCompradorResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.Paquete;
import ar.edu.unq.tusViajes.model.PerfilComprador;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.repository.PaqueteRepository;
import ar.edu.unq.tusViajes.repository.PerfilCompradorRepository;
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
class PerfilCompradorServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PerfilCompradorService perfilCompradorService;

    @Autowired
    private PerfilCompradorRepository perfilCompradorRepository;

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Test
    void listar_retornaTodosLosCompradores() {
        PerfilComprador comprador = perfilCompradorRepository.save(PerfilCompradorBuilder.aPerfilComprador().build());

        List<PerfilCompradorResponseDTO> resultado = perfilCompradorService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).isEqualTo(comprador.getNombre());
        assertThat(resultado.get(0).email()).isEqualTo(comprador.getEmail());
    }

    @Test
    void buscarPorId_devuelveCompradorCuandoExiste() {
        PerfilComprador comprador = perfilCompradorRepository.save(PerfilCompradorBuilder.aPerfilComprador().build());

        PerfilCompradorResponseDTO resultado = perfilCompradorService.buscarPorId(comprador.getId());

        assertThat(resultado.nombre()).isEqualTo(comprador.getNombre());
        assertThat(resultado.email()).isEqualTo(comprador.getEmail());
        assertThat(resultado.dni()).isEqualTo(comprador.getDni());
    }

    @Test
    void buscarPorId_lanzaExcepcionCuandoNoExiste() {
        assertThatThrownBy(() -> perfilCompradorService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void registrar_guardaCompradorConPasswordHasheado() {
        PerfilCompradorRequestDTO dto = new PerfilCompradorRequestDTO(
                "Lucas", "Gomez", "lucas@example.com", "secret123", "1122334455", "37111222"
        );

        PerfilCompradorResponseDTO resultado = perfilCompradorService.registrar(dto);

        assertThat(resultado.nombre()).isEqualTo("Lucas");
        assertThat(resultado.email()).isEqualTo("lucas@example.com");
        assertThat(resultado.dni()).isEqualTo("37111222");

        PerfilComprador guardadoEnDb = perfilCompradorRepository.findById(resultado.id()).orElseThrow();
        assertThat(guardadoEnDb.getPasswordHash()).isNotEqualTo("secret123");
    }

    @Test
    void registrar_lanzaExcepcionSiEmailEstaDuplicado() {
        PerfilComprador compradorExistente = PerfilCompradorBuilder.aPerfilComprador()
                .withEmail("repetido@example.com")
                .build();
        perfilCompradorRepository.save(compradorExistente);

        PerfilCompradorRequestDTO dto = new PerfilCompradorRequestDTO(
                "Lucas", "Gomez", "repetido@example.com", "secret123", "1122334455", "37111222"
        );

        assertThatThrownBy(() -> perfilCompradorService.registrar(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("repetido@example.com");
    }

    @Test
    void agregarFavorito_asociaElPaqueteAlComprador() {
       
        PerfilComprador comprador = PerfilCompradorBuilder.aPerfilComprador().build();
        comprador = perfilCompradorRepository.save(comprador);

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        Paquete paquete = paqueteRepository.save(PaqueteBuilder.aPaquete().withHotel(hotel).withAgencia(agencia).build());

        perfilCompradorService.agregarFavorito(comprador.getId(), paquete.getId());

        PerfilComprador compradorActualizado = perfilCompradorRepository.findById(comprador.getId()).orElseThrow();
        assertThat(compradorActualizado.getPaquetesFavoritos()).hasSize(1);
        assertThat(compradorActualizado.getPaquetesFavoritos().iterator().next().getId()).isEqualTo(paquete.getId());
    }

    @Test
    void quitarFavorito_desasociaElPaqueteDelComprador() {
     
        PerfilComprador comprador = PerfilCompradorBuilder.aPerfilComprador().build();
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        Paquete paquete = paqueteRepository.save(PaqueteBuilder.aPaquete().withHotel(hotel).withAgencia(agencia).build());
        
        comprador.agregarFavorito(paquete);
        comprador = perfilCompradorRepository.save(comprador);

        perfilCompradorService.quitarFavorito(comprador.getId(), paquete.getId());

        PerfilComprador compradorActualizado = perfilCompradorRepository.findById(comprador.getId()).orElseThrow();
        assertThat(compradorActualizado.getPaquetesFavoritos()).isEmpty();
    }

    @Test
    void listarFavoritos_devuelveListaDePaquetesResponseDTO() {
       
        PerfilComprador comprador = PerfilCompradorBuilder.aPerfilComprador().build();
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        Paquete paquete = paqueteRepository.save(PaqueteBuilder.aPaquete().withNombre("Promo Bariloche").withHotel(hotel).withAgencia(agencia).build());
        
        comprador.agregarFavorito(paquete);
        perfilCompradorRepository.save(comprador);

        List<PaqueteResponseDTO> favoritos = perfilCompradorService.listarFavoritos(comprador.getId());

        assertThat(favoritos).hasSize(1);
        assertThat(favoritos.get(0).getNombre()).isEqualTo("Promo Bariloche");
    }

    @Test
    void agregarFavorito_lanzaExcepcionSiCompradorNoExiste() {
        assertThatThrownBy(() -> perfilCompradorService.agregarFavorito(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comprador");
    }
}