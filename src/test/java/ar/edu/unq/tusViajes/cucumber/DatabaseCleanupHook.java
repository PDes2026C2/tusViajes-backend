package ar.edu.unq.tusViajes.cucumber;

import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.repository.*;
import ar.edu.unq.tusViajes.service.FlightsApiService;
import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import org.mockito.Mockito;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@RequiredArgsConstructor
public class DatabaseCleanupHook {

    private final ReviewRepository reviewRepository;
    private final PurchaseRepository purchaseRepository;
    private final BuyerRepository buyerRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final FlightRepository flightRepository;
    private final HotelRepository hotelRepository;
    private final AgencyRepository agencyRepository;
    private final AdminRepository adminRepository;
    private final CityRepository cityRepository;
    private final CountryRepository countryRepository;
    private final FlightsApiService flightsApiService;
    private final TransactionTemplate transactionTemplate;

    @Before
    public void cleanDatabaseAndMocks() {
        Mockito.reset(flightsApiService);

        transactionTemplate.executeWithoutResult(status -> {
            reviewRepository.deleteAllInBatch();
            purchaseRepository.deleteAllInBatch();

            List<Buyer> buyers = buyerRepository.findAll();
            for (Buyer buyer : buyers) {
                buyer.getFavoriteTravelPackages().clear();
                buyer.getTravelPackagesPurchased().clear();
            }
            buyerRepository.saveAllAndFlush(buyers);
            buyerRepository.deleteAll();

            travelPackageRepository.deleteAllInBatch();
            flightRepository.deleteAllInBatch();
            hotelRepository.deleteAllInBatch();
            agencyRepository.deleteAll();
            adminRepository.deleteAll();
            cityRepository.deleteAllInBatch();
            countryRepository.deleteAllInBatch();
        });
    }
}
