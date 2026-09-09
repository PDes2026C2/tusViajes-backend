package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.BuyerBuilder;
import ar.edu.unq.tusViajes.builder.CustomUserDetailsBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.repository.BuyerRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
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
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class BuyerControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private TravelPackageRepository travelPackageRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    @Test
    void getAll_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/buyers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_returns200AndListOfBuyers() throws Exception {
        buyerRepository.save(
                BuyerBuilder.aBuyer()
                        .withFirstName("Lucas")
                        .withEmail("lucas@example.com")
                        .build()
        );

        mockMvc.perform(get("/api/buyers").with(user("lucas").roles("BUYER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNotEmpty())
                .andExpect(jsonPath("$[0].firstName").value("Lucas"));
    }

    @Test
    void getById_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/buyers/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_returns200WhenExists() throws Exception {
        Buyer saved = buyerRepository.save(
                BuyerBuilder.aBuyer()
                        .withNationalId("38123456")
                        .build()
        );

        mockMvc.perform(get("/api/buyers/" + saved.getId()).with(user("lucas").roles("BUYER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.nationalId").value("38123456"));
    }

    @Test
    void getById_returns404WhenDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/buyers/99999").with(user("lucas").roles("BUYER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void register_returns201AndLocationHeader() throws Exception {
        String json = """
                {
                    "firstName": "Lucas",
                    "lastName": "Gomez",
                    "email": "lucas@example.com",
                    "password": "secretPassword123",
                    "phoneNumber": "11223344",
                    "nationalId": "38123456"
                }
                """;

        mockMvc.perform(post("/api/buyers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nationalId").value("38123456"));
    }

    @Test
    void addFavorite_returns401_whenUnauthenticated() throws Exception {
                mockMvc.perform(post("/api/buyers/favorites/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addFavorite_returns200Ok() throws Exception {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer()
                        .withFirstName("Lucas")
                        .withEmail("lucas@example.com")
                        .build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage().withHotel(hotel).withAgency(agency).build());

        mockMvc.perform(post("/api/buyers/favorites/" + travelPackage.getId())
                        .with(user(CustomUserDetailsBuilder.aUserDetails()
                                .withId(buyer.getId())
                                .withEmail(buyer.getEmail())
                                .withAuthorities(createAuthorityList("ROLE_BUYER"))
                                .build())))
                .andExpect(status().isOk());

        Buyer updatedBuyer = buyerRepository.findById(buyer.getId()).orElseThrow();
        assertThat(updatedBuyer.getFavoriteTravelPackages()).hasSize(1);
    }

    @Test
    void removeFavorite_returns401_whenUnauthenticated() throws Exception {
                mockMvc.perform(delete("/api/buyers/favorites/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void removeFavorite_returns204NoContent() throws Exception {
        Buyer buyer = BuyerBuilder.aBuyer()
                        .withFirstName("Lucas")
                        .withEmail("lucas@example.com")
                        .build();
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage().withHotel(hotel).withAgency(agency).build());

        buyer.addFavorite(travelPackage);
        buyer = buyerRepository.save(buyer);

        mockMvc.perform(delete("/api/buyers/favorites/" + travelPackage.getId())
                        .with(user(CustomUserDetailsBuilder.aUserDetails()
                                .withId(buyer.getId())
                                .withEmail(buyer.getEmail())
                                .withAuthorities(createAuthorityList("ROLE_BUYER"))
                                .build())))
                .andExpect(status().isNoContent());

        Buyer updatedBuyer = buyerRepository.findById(buyer.getId()).orElseThrow();
        assertThat(updatedBuyer.getFavoriteTravelPackages()).isEmpty();
    }

    @Test
    void getFavorites_returns401_whenUnauthenticated() throws Exception {
                mockMvc.perform(get("/api/buyers/favorites"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getFavorites_returns200AndListOfPackages() throws Exception {
        Buyer buyer = BuyerBuilder.aBuyer()
                        .withFirstName("Lucas")
                        .withEmail("lucas@example.com")
                        .build();
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        TravelPackage travelPackage = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage().withName("Ushuaia Invierno").withHotel(hotel).withAgency(agency).build());

        buyer.addFavorite(travelPackage);
        buyer = buyerRepository.save(buyer);

        mockMvc.perform(get("/api/buyers/favorites")
                        .with(user(CustomUserDetailsBuilder.aUserDetails()
                                .withId(buyer.getId())
                                .withEmail(buyer.getEmail())
                                .withAuthorities(createAuthorityList("ROLE_BUYER"))
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(travelPackage.getId()))
                .andExpect(jsonPath("$[0].name").value("Ushuaia Invierno"));
    }
}
