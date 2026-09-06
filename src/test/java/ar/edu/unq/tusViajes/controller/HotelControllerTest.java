package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.repository.HotelRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class HotelControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HotelRepository hotelRepository;

    @Test
    void listar_retorna200YListaDeHoteles() throws Exception {
        hotelRepository.save(HotelBuilder.aHotel().withNombre("Hotel Central").withDestino("Bariloche").build());

        mockMvc.perform(get("/api/hoteles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].nombre").value("Hotel Central"))
                .andExpect(jsonPath("$[0].destino").value("Bariloche"));
    }

    @Test
    void buscarPorId_retorna200CuandoExiste() throws Exception {
        Hotel guardado = hotelRepository.save(
                HotelBuilder.aHotel().withNombre("Hotel Central").withDestino("Bariloche").build()
        );

        mockMvc.perform(get("/api/hoteles/" + guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombre").value("Hotel Central"))
                .andExpect(jsonPath("$.destino").value("Bariloche"));
    }

    @Test
    void buscarPorId_retorna404CuandoNoExiste() throws Exception {
        mockMvc.perform(get("/api/hoteles/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crear_retorna201YLocationHeader() throws Exception {
        String json = """
                {
                    "nombre": "Hotel Nuevo",
                    "destino": "Mendoza"
                }
                """;

        mockMvc.perform(post("/api/hoteles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Hotel Nuevo"))
                .andExpect(jsonPath("$.destino").value("Mendoza"));
    }
}