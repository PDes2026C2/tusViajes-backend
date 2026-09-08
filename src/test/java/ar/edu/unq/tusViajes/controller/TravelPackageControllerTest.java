package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class TravelPackageControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TravelPackageRepository travelPackageRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    @Test
    void getAll_returns200AndListOfTravelPackages() throws Exception {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withName("Bariloche 7d")
                .withHotel(hotel)
                .withAgency(agency)
                .build();
        travelPackageRepository.save(travelPackage);

        mockMvc.perform(get("/api/travel-packages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].name").value("Bariloche 7d"));
    }

    @Test
    void getById_returns200WhenExists() throws Exception {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withName("Bariloche 7d")
                .withHotel(hotel)
                .withAgency(agency)
                .build();
        TravelPackage saved = travelPackageRepository.save(travelPackage);

        mockMvc.perform(get("/api/travel-packages/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Bariloche 7d"));
    }

    @Test
    void getById_returns404WhenDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/travel-packages/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns401_whenUnauthenticated() throws Exception {
        String json = """
                {
                    "name": "Bariloche 7d",
                    "description": "Desc",
                    "price": 150000.0,
                    "startDate": "2026-10-01T10:00:00",
                    "endDate": "2026-10-08T10:00:00",
                    "hotelId": 1,
                    "agencyId": 1
                }
                """;

        mockMvc.perform(post("/api/travel-packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_returns201AndLocationHeader() throws Exception {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());

        String json = """
                {
                    "name": "Bariloche 7d",
                    "description": "Desc",
                    "price": 150000.0,
                    "startDate": "2026-10-01T10:00:00",
                    "endDate": "2026-10-08T10:00:00",
                    "hotelId": %d,
                    "agencyId": %d
                }
                """.formatted(hotel.getId(), agency.getId());

        mockMvc.perform(post("/api/travel-packages")
                        .with(user("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Bariloche 7d"));
    }

    @Test
    void update_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(put("/api/travel-packages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_returns200_whenAuthenticated() throws Exception {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());

        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .build();
        TravelPackage saved = travelPackageRepository.save(travelPackage);

        String json = """
                {
                    "name": "Bariloche 10d",
                    "description": "Extended desc",
                    "price": 200000.0,
                    "startDate": "2026-11-01T10:00:00",
                    "endDate": "2026-11-10T10:00:00",
                    "hotelId": %d,
                    "agencyId": %d
                }
                """.formatted(hotel.getId(), agency.getId());

        mockMvc.perform(put("/api/travel-packages/" + saved.getId())
                        .with(user("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bariloche 10d"));
    }

    @Test
    void delete_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/travel-packages/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_returns204NoContent() throws Exception {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .build();
        TravelPackage saved = travelPackageRepository.save(travelPackage);

        mockMvc.perform(delete("/api/travel-packages/" + saved.getId()).with(user("user")))
                .andExpect(status().isNoContent());

        assertThat(travelPackageRepository.existsById(saved.getId())).isFalse();
    }
}
