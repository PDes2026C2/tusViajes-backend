package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.PaqueteBuilder;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.Paquete;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.repository.PaqueteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class PaqueteControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Test
    void listar_retorna200YListaDePaquetes() throws Exception {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        
        Paquete paquete = PaqueteBuilder.aPaquete()
                .withNombre("Bariloche 7d")
                .withHotel(hotel)
                .withAgencia(agencia)
                .build();
        paqueteRepository.save(paquete);

        mockMvc.perform(get("/api/paquetes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].nombre").value("Bariloche 7d"));
    }

    @Test
    void buscarPorId_retorna200CuandoExiste() throws Exception {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        
        Paquete paquete = PaqueteBuilder.aPaquete()
                .withNombre("Bariloche 7d")
                .withHotel(hotel)
                .withAgencia(agencia)
                .build();
        Paquete guardado = paqueteRepository.save(paquete);

        mockMvc.perform(get("/api/paquetes/" + guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombre").value("Bariloche 7d"));
    }

    @Test
    void buscarPorId_retorna404CuandoNoExiste() throws Exception {
        mockMvc.perform(get("/api/paquetes/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_retorna201YLocationHeader() throws Exception {
        
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());

        String json = """
                {
                    "nombre": "Bariloche 7d",
                    "descripcion": "Desc",
                    "precio": 150000.0,
                    "fechaInicio": "2026-10-01T10:00:00",
                    "fechaFin": "2026-10-08T10:00:00",
                    "hotelId": %d,
                    "agenciaId": %d
                }
                """.formatted(hotel.getId(), agencia.getId());

        mockMvc.perform(post("/api/paquetes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Bariloche 7d"));
    }

    @Test
    void eliminar_retorna204NoContent() throws Exception {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agencia agencia = agenciaRepository.save(AgenciaBuilder.anAgencia().build());
        
        Paquete paquete = PaqueteBuilder.aPaquete()
                .withHotel(hotel)
                .withAgencia(agencia)
                .build();
        Paquete guardado = paqueteRepository.save(paquete);

        mockMvc.perform(delete("/api/paquetes/" + guardado.getId()))
                .andExpect(status().isNoContent());

        
        assertThat(paqueteRepository.existsById(guardado.getId())).isFalse();
    }
}