package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.builder.*;
import ar.edu.unq.tusViajes.controller.dto.request.ReviewRequestDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.ReviewNotAllowedException;
import ar.edu.unq.tusViajes.model.*;
import ar.edu.unq.tusViajes.repository.*;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static ar.edu.unq.tusViajes.builder.CityBuilder.aCity;
import static ar.edu.unq.tusViajes.builder.CountryBuilder.aCountry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Transactional
class ReviewServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private TravelPackageRepository travelPackageRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CountryRepository countryRepository;

    private TravelPackage createAndSaveTravelPackage(LocalDateTime start, LocalDateTime end) {
        Country country = countryRepository.save(aCountry().withIsoCode("AR").withName("Argentina").build());
        City bue = cityRepository.save(aCity().withName("Buenos Aires").withCountry(country).build());
        City brc = cityRepository.save(aCity().withName("Bariloche").withCountry(country).build());
        Hotel hotel = hotelRepository.save(HotelBuilder.aHotel().withCity(brc).build());
        Agency agency = agencyRepository.save(AgencyBuilder.anAgency().build());
        Flight dep = flightRepository.save(FlightBuilder.aFlight().withId(501L).withOriginCity(bue).withDestinationCity(brc).build());
        Flight ret = flightRepository.save(FlightBuilder.aFlight().withId(502L).withOriginCity(brc).withDestinationCity(bue).build());

        return travelPackageRepository.save(TravelPackageBuilder.aTravelPackage()
                .withStartDate(start)
                .withEndDate(end)
                .withHotel(hotel)
                .withAgency(agency)
                .withDepartureFlight(dep)
                .withReturnFlight(ret)
                .build());
    }

    @Test
    void getByBuyerId_returnsOnlyReviewsOfGivenBuyer() {
        TravelPackage travelPackage = createAndSaveTravelPackage(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));

        Buyer buyer1 = buyerRepository.save(BuyerBuilder.aBuyer().withEmail("buyer1@service.com").withNationalId("50111111").build());
        Buyer buyer2 = buyerRepository.save(BuyerBuilder.aBuyer().withEmail("buyer2@service.com").withNationalId("50222222").build());

        Review review1 = reviewRepository.save(ReviewBuilder.aReview().withBuyer(buyer1).withTravelPackage(travelPackage).withScore(9).withComment("Excelente").build());
        reviewRepository.save(ReviewBuilder.aReview().withBuyer(buyer2).withTravelPackage(travelPackage).withScore(6).withComment("Bueno").build());

        Page<Review> result = reviewService.getByBuyerId(buyer1.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(review1.getId());
        assertThat(result.getContent().get(0).getBuyer().getId()).isEqualTo(buyer1.getId());
        assertThat(result.getContent().get(0).getComment()).isEqualTo("Excelente");
    }

    @Test
    void getByBuyerId_returnsEmptyPage_whenBuyerHasNoReviews() {
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().withEmail("buyer_empty@service.com").withNationalId("50333333").build());

        Page<Review> result = reviewService.getByBuyerId(buyer.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getByTravelPackageId_returnsReviewsForPackage() {
        TravelPackage travelPackage = createAndSaveTravelPackage(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().withEmail("buyer_tp@service.com").withNationalId("50444444").build());

        Review review = reviewRepository.save(ReviewBuilder.aReview().withBuyer(buyer).withTravelPackage(travelPackage).withScore(8).withComment("Muy buen viaje").build());

        Page<Review> result = reviewService.getByTravelPackageId(travelPackage.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(review.getId());
        assertThat(result.getContent().get(0).getTravelPackage().getId()).isEqualTo(travelPackage.getId());
    }

    @Test
    void create_savesReviewSuccessfully() {
        TravelPackage travelPackage = createAndSaveTravelPackage(LocalDateTime.now().minusDays(15), LocalDateTime.now().minusDays(5));
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().withEmail("buyer_create@service.com").withNationalId("50555555").build());

        buyer.addPurchase(new Purchase(buyer, travelPackage, travelPackage.getPrice()));
        buyerRepository.save(buyer);

        CustomUserDetails userDetails = new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(), createAuthorityList("ROLE_BUYER"), true);
        ReviewRequestDTO dto = new ReviewRequestDTO(10, "Experiencia inolvidable");

        Review created = reviewService.create(userDetails, travelPackage.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getScore()).isEqualTo(10);
        assertThat(created.getComment()).isEqualTo("Experiencia inolvidable");
        assertThat(created.getBuyer().getId()).isEqualTo(buyer.getId());
        assertThat(created.getTravelPackage().getId()).isEqualTo(travelPackage.getId());
    }

    @Test
    void create_throwsDuplicateResourceException_whenReviewAlreadyExists() {
        TravelPackage travelPackage = createAndSaveTravelPackage(LocalDateTime.now().minusDays(15), LocalDateTime.now().minusDays(5));
        Buyer buyer = buyerRepository.save(BuyerBuilder.aBuyer().withEmail("buyer_dup@service.com").withNationalId("50666666").build());

        buyer.addPurchase(new Purchase(buyer, travelPackage, travelPackage.getPrice()));
        buyerRepository.save(buyer);

        reviewRepository.save(ReviewBuilder.aReview().withBuyer(buyer).withTravelPackage(travelPackage).withScore(7).withComment("Primera reseña").build());

        CustomUserDetails userDetails = new CustomUserDetails(buyer.getId(), buyer.getEmail(), buyer.getPasswordHash(), createAuthorityList("ROLE_BUYER"), true);
        ReviewRequestDTO dto = new ReviewRequestDTO(8, "Segunda reseña");

        assertThatThrownBy(() -> reviewService.create(userDetails, travelPackage.getId(), dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("El comprador ya opinó sobre este paquete de viaje");
    }
}
