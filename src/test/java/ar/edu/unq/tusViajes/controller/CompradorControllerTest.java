package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.builder.CompradorBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.PaqueteBuilder;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class CompradorControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompradorRepository compradorRepository;

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Test
    void listar_retorna401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/compradores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listar_retorna200YListaDeCompradores() throws Exception {
        compradorRepository.save(
                CompradorBuilder.aComprador()
                        .withNombre("Lucas")
                        .withEmail("lucas@example.com")
                        .build()
        );

        mockMvc.perform(get("/api/compradores").with(user("lucas").roles("COMPRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].nombre").value("Lucas"));
    }

    @Test
    void buscarPorId_retorna401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/compradores/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buscarPorId_retorna200CuandoExiste() throws Exception {
        Comprador guardado = compradorRepository.save(
                CompradorBuilder.aComprador()
                        .withDni("38123456")
                        .build()
        );

        mockMvc.perform(get("/api/compradores/" + guardado.getId()).with(user("lucas").roles("COMPRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.dni").value("38123456"));
    }

    @Test
    void buscarPorId_retorna404CuandoNoExiste() throws Exception {
        mockMvc.perform(get("/api/compradores/99999").with(user("lucas").roles("COMPRADOR")))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrar_retorna201YLocationHeader() throws Exception {
        String json = """
                {
                    "nombre": "Lucas",
                    "apellido": "Gomez",
                    "email": "lucas@example.com",
                    "password": "secretPassword123",
                    "telefono": "11223344",
                    "dni": "38123456"
                }
                """;

        mockMvc.perform(post("/api/compradores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.dni").value("38123456"));
    }

    @Test
    void agregarFavorito_retorna401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(post("/api/compradores/1/favoritos/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void agregarFavorito_retorna200Ok() throws Exception {
        Comprador comprador = compradorRepository.save(CompradorBuilder.aComprador()
                        .withNombre("Lucas")
                        .withEmail("lucas@example.com")
                        .build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        Paquete paquete = paqueteRepository.save(PaqueteBuilder.aPaquete().withHotel(hotel).withAgencia(agencia).build());

        mockMvc.perform(post("/api/compradores/" + comprador.getId() + "/favoritos/" + paquete.getId())
                        .with(user("lucas").roles("COMPRADOR")))
                .andExpect(status().isOk());

        Comprador compradorActualizado = compradorRepository.findById(comprador.getId()).orElseThrow();
        assertThat(compradorActualizado.getPaquetesFavoritos()).hasSize(1);
    }

    @Test
    void quitarFavorito_retorna401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(delete("/api/compradores/1/favoritos/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void quitarFavorito_retorna204NoContent() throws Exception {
        Comprador comprador = CompradorBuilder.aComprador()
                        .withNombre("Lucas")
                        .withEmail("lucas@example.com")
                        .build();
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        Paquete paquete = paqueteRepository.save(PaqueteBuilder.aPaquete().withHotel(hotel).withAgencia(agencia).build());

        comprador.agregarFavorito(paquete);
        comprador = compradorRepository.save(comprador);

        mockMvc.perform(delete("/api/compradores/" + comprador.getId() + "/favoritos/" + paquete.getId())
                        .with(user("lucas").roles("COMPRADOR")))
                .andExpect(status().isNoContent());

        Comprador compradorActualizado = compradorRepository.findById(comprador.getId()).orElseThrow();
        assertThat(compradorActualizado.getPaquetesFavoritos()).isEmpty();
    }

    @Test
    void listarFavoritos_retorna401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/compradores/1/favoritos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarFavoritos_retorna200YListaDePaquetes() throws Exception {
        Comprador comprador = CompradorBuilder.aComprador()
                        .withNombre("Lucas")
                        .withEmail("lucas@example.com")
                        .build();
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        Paquete paquete = paqueteRepository.save(PaqueteBuilder.aPaquete().withNombre("Ushuaia Invierno").withHotel(hotel).withAgencia(agencia).build());

        comprador.agregarFavorito(paquete);
        comprador = compradorRepository.save(comprador);

        mockMvc.perform(get("/api/compradores/" + comprador.getId() + "/favoritos")
                        .with(user("lucas").roles("COMPRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(paquete.getId()))
                .andExpect(jsonPath("$[0].nombre").value("Ushuaia Invierno"));
    }
}
