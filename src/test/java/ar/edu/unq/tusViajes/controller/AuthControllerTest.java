package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AdminBuilder;
import ar.edu.unq.tusViajes.builder.AgenciaBuilder;
import ar.edu.unq.tusViajes.model.EstadoAgencia;
import ar.edu.unq.tusViajes.repository.AdminRepository;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void login_retorna200YToken_cuandoAdminEsValido() throws Exception {
        adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword123"))
                .build());

        String json = """
                {
                    "email": "admin@test.com",
                    "password": "secretPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));
    }

    @Test
    void login_retorna403_cuandoAgenciaEstaPendiente() throws Exception {
        agenciaRepository.save(AgenciaBuilder.anAgencia()
                .withEmail("pendiente@agencia.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword123"))
                .withEstado(EstadoAgencia.PENDIENTE)
                .build());

        String json = """
                {
                    "email": "pendiente@agencia.com",
                    "password": "secretPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("La agencia se encuentra pendiente de autorizacion por un administrador"));
    }

    @Test
    void login_retorna200YToken_cuandoAgenciaEstaAutorizada() throws Exception {
        agenciaRepository.save(AgenciaBuilder.anAgencia()
                .withEmail("autorizada@agencia.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword123"))
                .withEstado(EstadoAgencia.AUTORIZADA)
                .build());

        String json = """
                {
                    "email": "autorizada@agencia.com",
                    "password": "secretPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("AGENCIA"));
    }

    @Test
    void login_retorna401_cuandoPasswordIncorrecto() throws Exception {
        adminRepository.save(AdminBuilder.anAdmin()
                .withEmail("admin@test.com")
                .withPasswordHash(passwordEncoder.encode("secretPassword123"))
                .build());

        String json = """
                {
                    "email": "admin@test.com",
                    "password": "wrongPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrarAgencia_retorna201YEstadoPendiente() throws Exception {
        String json = """
                {
                    "razonSocial": "Nueva Agencia SA",
                    "cuit": "30-55667788-9",
                    "email": "nueva@agencia.com",
                    "password": "secretPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/registro/agencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.razonSocial").value("Nueva Agencia SA"))
                .andExpect(jsonPath("$.mensaje").value("Propuesta de registro recibida. Pendiente de autorizacion por un administrador."));
    }

    @Test
    void registrarComprador_retorna201YDatosComprador() throws Exception {
        String json = """
                {
                    "nombre": "Agustin",
                    "apellido": "Perez",
                    "email": "agustin@comprador.com",
                    "password": "secretPassword123",
                    "telefono": "1122334455",
                    "dni": "39123456"
                }
                """;

        mockMvc.perform(post("/api/auth/registro/comprador")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nombre").value("Agustin"))
                .andExpect(jsonPath("$.email").value("agustin@comprador.com"));
    }
}
