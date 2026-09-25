package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.builder.AgencyBuilder;
import ar.edu.unq.tusViajes.builder.BuyerBuilder;
import ar.edu.unq.tusViajes.builder.CityBuilder;
import ar.edu.unq.tusViajes.builder.CountryBuilder;
import ar.edu.unq.tusViajes.builder.FlightBuilder;
import ar.edu.unq.tusViajes.builder.HotelBuilder;
import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.repository.BuyerRepository;
import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.CountryRepository;
import ar.edu.unq.tusViajes.repository.FlightRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.repository.PurchaseRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import ar.edu.unq.tusViajes.security.CustomUserDetails;

import static ar.edu.unq.tusViajes.builder.CityBuilder.aCity;
import static ar.edu.unq.tusViajes.builder.CountryBuilder.aCountry;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
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
class AgencyControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private TravelPackageRepository travelPackageRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Test
    void getAll_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/agencies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAll_returns403_whenRoleIsAgency() throws Exception {
        mockMvc.perform(get("/api/agencies").with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_returns403_whenRoleIsBuyer() throws Exception {
        mockMvc.perform(get("/api/agencies").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAll_returns200AndList_whenRoleIsAdmin() throws Exception {
        agencyRepository.save(AgencyBuilder.anAgency().withBusinessName("South Travel").build());

        mockMvc.perform(get("/api/agencies").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].businessName").value("South Travel"));
    }

    @Test
    void getById_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/agencies/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_returns403_whenRoleIsAgency() throws Exception {
        mockMvc.perform(get("/api/agencies/1").with(user("agency").roles("AGENCY")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_returns403_whenRoleIsBuyer() throws Exception {
        mockMvc.perform(get("/api/agencies/1").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getById_returns200WhenExists_whenRoleIsAdmin() throws Exception {
        Agency saved = agencyRepository.save(
                AgencyBuilder.anAgency().withBusinessName("South Travel").withTaxId("30-12345678-9").build()
        );

        mockMvc.perform(get("/api/agencies/" + saved.getId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.businessName").value("South Travel"));
    }

    @Test
    void getById_returns404WhenDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/agencies/99999").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_returns401_whenUnauthenticated() throws Exception {
        String json = """
                {
                    "businessName": "Updated SA"
                }
                """;

        mockMvc.perform(put("/api/agencies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void update_modifiesBusinessNameAndReturns200() throws Exception {
        Agency saved = agencyRepository.save(AgencyBuilder.anAgency().withBusinessName("Original SA").build());

        String json = """
                {
                    "businessName": "Updated SA"
                }
                """;

        mockMvc.perform(put("/api/agencies/" + saved.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.businessName").value("Updated SA"));
    }

    @Test
    void delete_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(delete("/api/agencies/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void delete_returns204NoContent() throws Exception {
        Agency saved = agencyRepository.save(AgencyBuilder.anAgency().build());

        mockMvc.perform(delete("/api/agencies/" + saved.getId()).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        assertThat(agencyRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void getMyPackages_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/agencies/me/packages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyPackages_returns403_whenBuyer() throws Exception {
        mockMvc.perform(get("/api/agencies/me/packages").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyPackages_returns403_whenAdmin() throws Exception {
        mockMvc.perform(get("/api/agencies/me/packages").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyPackages_returnsOnlyOwnPackages() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());

        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency mine = agencyRepository.save(AgencyBuilder.anAgency().withEmail("mine@example.com").withTaxId("20-77777777-7").build());
        Agency other = agencyRepository.save(AgencyBuilder.anAgency().withEmail("theirs@example.com").withTaxId("20-88888888-8").build());
        Flight depFlight = flightRepository.save(FlightBuilder.aFlight().withId(96L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight retFlight = flightRepository.save(FlightBuilder.aFlight().withId(97L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());

        travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Mine").withHotel(hotel).withAgency(mine).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());
        travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withName("Theirs").withHotel(hotel).withAgency(other).withDepartureFlight(depFlight).withReturnFlight(retFlight).build());

        mockMvc.perform(get("/api/agencies/me/packages")
                        .with(user(new CustomUserDetails(mine.getId(), mine.getEmail(), mine.getPasswordHash(), createAuthorityList("ROLE_AGENCY"), true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Mine"));
    }

    @Test
    void getMySales_returns401_whenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/agencies/me/sales"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMySales_returns403_whenNotAgency() throws Exception {
        mockMvc.perform(get("/api/agencies/me/sales").with(user("buyer").roles("BUYER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/agencies/me/sales").with(user("admin").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMySales_returnsOnlyOwnSales() throws Exception {
        Country argentina = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City buenosAires = cityRepository.save(aCity().withName("Buenos Aires").withCountry(argentina).build());
        City bariloche = cityRepository.save(aCity().withName("Bariloche").withCountry(argentina).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(bariloche).build());
        Agency mine = agencyRepository.save(AgencyBuilder.anAgency().withEmail("mine-sales@example.com").withTaxId("20-99999991-1").build());
        Agency other = agencyRepository.save(AgencyBuilder.anAgency().withEmail("other-sales@example.com").withTaxId("20-99999992-2").build());
        Flight dep = flightRepository.save(FlightBuilder.aFlight().withId(120L).withOriginCity(buenosAires).withDestinationCity(bariloche).build());
        Flight ret = flightRepository.save(FlightBuilder.aFlight().withId(121L).withOriginCity(bariloche).withDestinationCity(buenosAires).build());
        TravelPackage mineTp = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage().withHotel(hotel).withAgency(mine).withDepartureFlight(dep).withReturnFlight(ret).build());
        TravelPackage otherTp = travelPackageRepository.save(TravelPackageBuilder.aTravelPackage().withHotel(hotel).withAgency(other).withDepartureFlight(dep).withReturnFlight(ret).build());
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().withEmail("buyer-sales@example.com").withNationalId("40222222").build());
        buyer.buy(mineTp);
        buyerRepository.save(buyer);
        Buyer buyer2 = buyerRepository.save(BuyerBuilder.aBuyer().withEmail("buyer2-sales@example.com").withNationalId("40222223").build());
        buyer2.buy(otherTp);
        buyerRepository.save(buyer2);

        mockMvc.perform(get("/api/agencies/me/sales")
                        .with(user(new CustomUserDetails(mine.getId(), mine.getEmail(), mine.getPasswordHash(), createAuthorityList("ROLE_AGENCY"), true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].travelPackage.id").value(mineTp.getId()));
    }

    @Test
    void getMySales_returnsEmptyWhenNoSales() throws Exception {
        Agency mine = agencyRepository.save(AgencyBuilder.anAgency().withEmail("empty-sales@example.com").withTaxId("20-99999993-3").build());

        mockMvc.perform(get("/api/agencies/me/sales")
                        .with(user(new CustomUserDetails(mine.getId(), mine.getEmail(), mine.getPasswordHash(), createAuthorityList("ROLE_AGENCY"), true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
