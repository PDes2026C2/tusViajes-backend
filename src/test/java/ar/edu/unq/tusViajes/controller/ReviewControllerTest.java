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
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerTest {

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
    void create_returns201WithReview() throws Exception {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        TravelPackage travelPackage = saveTravelPackage();
        buyer.addFavorite(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/travel-packages/" + travelPackage.getId() + "/reviews")
                        .with(user(CustomUserDetailsBuilder.aUserDetails()
                                .withId(buyer.getId())
                                .withEmail(buyer.getEmail())
                                .withAuthorities(org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_BUYER"))
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":9,\"comment\":\"Excellent\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.score").value(9))
                .andExpect(jsonPath("$.comment").value("Excellent"))
                .andExpect(jsonPath("$.buyerId").value(buyer.getId()));
    }

    @Test
    void create_returns400WhenScoreIsOutOfRange() throws Exception {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        TravelPackage travelPackage = saveTravelPackage();
        buyer.addFavorite(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/travel-packages/" + travelPackage.getId() + "/reviews")
                        .with(user(CustomUserDetailsBuilder.aUserDetails()
                                .withId(buyer.getId())
                                .withAuthorities(org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_BUYER"))
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":11}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns201WithScoreOnly() throws Exception {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        TravelPackage travelPackage = saveTravelPackage();
        buyer.addFavorite(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/travel-packages/" + travelPackage.getId() + "/reviews")
                        .with(user(CustomUserDetailsBuilder.aUserDetails()
                                .withId(buyer.getId())
                                .withAuthorities(org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_BUYER"))
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":8}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(8))
                .andExpect(jsonPath("$.comment").doesNotExist());
    }

    @Test
    void create_returns201WithCommentOnly() throws Exception {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        TravelPackage travelPackage = saveTravelPackage();
        buyer.addFavorite(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/travel-packages/" + travelPackage.getId() + "/reviews")
                        .with(user(CustomUserDetailsBuilder.aUserDetails()
                                .withId(buyer.getId())
                                .withAuthorities(org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_BUYER"))
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Excellent\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").doesNotExist())
                .andExpect(jsonPath("$.comment").value("Excellent"));
    }

    @Test
    void create_returns400WhenScoreAndCommentAreMissing() throws Exception {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        TravelPackage travelPackage = saveTravelPackage();
        buyer.addFavorite(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/travel-packages/" + travelPackage.getId() + "/reviews")
                        .with(user(CustomUserDetailsBuilder.aUserDetails()
                                .withId(buyer.getId())
                                .withAuthorities(org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_BUYER"))
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns403WhenPackageIsNotFavorite() throws Exception {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        TravelPackage travelPackage = saveTravelPackage();

        mockMvc.perform(post("/api/travel-packages/" + travelPackage.getId() + "/reviews")
                        .with(user(CustomUserDetailsBuilder.aUserDetails()
                                .withId(buyer.getId())
                                .withAuthorities(org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_BUYER"))
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":8}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_returns401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/travel-packages/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":8}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getByTravelPackageId_returnsReviews() throws Exception {
        TravelPackage travelPackage = saveTravelPackage();
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
                buyer.addFavorite(travelPackage);
                buyerRepository.save(buyer);

        mockMvc.perform(post("/api/travel-packages/" + travelPackage.getId() + "/reviews")
                        .with(user(CustomUserDetailsBuilder.aUserDetails()
                                .withId(buyer.getId())
                                .withAuthorities(org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_BUYER"))
                                .build()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":7,\"comment\":\"Good\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/travel-packages/" + travelPackage.getId() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].score").value(7))
                .andExpect(jsonPath("$[0].comment").value("Good"));
    }

    @Test
    void update_returns200AndUpdatesReview() throws Exception {
        TravelPackage travelPackage = saveTravelPackage();
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().build());
        buyer.addFavorite(travelPackage);
        buyerRepository.save(buyer);

        mockMvc.perform(post("/api/travel-packages/" + travelPackage.getId() + "/reviews")
                        .with(user(CustomUserDetailsBuilder.aUserDetails().withId(buyer.getId())
                                .withAuthorities(org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_BUYER")).build()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"score\":7}"))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/travel-packages/" + travelPackage.getId() + "/reviews")
                        .with(user(CustomUserDetailsBuilder.aUserDetails().withId(buyer.getId())
                                .withAuthorities(org.springframework.security.core.authority.AuthorityUtils.createAuthorityList("ROLE_BUYER")).build()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Updated comment\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").doesNotExist())
                .andExpect(jsonPath("$.comment").value("Updated comment"));
    }

    private TravelPackage saveTravelPackage() {
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        return travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withHotel(hotel)
                .withAgency(agency)
                .build());
    }
}