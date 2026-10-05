package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.model.AgenciesTop;
import ar.edu.unq.tusViajes.model.BuyersTop;
import ar.edu.unq.tusViajes.model.DestinationsTop;
import ar.edu.unq.tusViajes.model.RatedDestinationsTop;
import ar.edu.unq.tusViajes.repository.PurchaseRepository;
import ar.edu.unq.tusViajes.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMetricsService {

    private final PurchaseRepository purchaseRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<BuyersTop> getTopBuyers() {
        return purchaseRepository.findTopBuyers(PageRequest.of(0, 5))
                .stream()
                .map(result -> new BuyersTop(result.getBuyer(), result.getPurchaseCount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DestinationsTop> getTopDestinations() {
        return purchaseRepository.findTopDestinations(PageRequest.of(0, 5))
                .stream()
                .map(result -> new DestinationsTop(result.getCity(), result.getSalesCount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RatedDestinationsTop> getTopRatedDestinations() {
        return reviewRepository.findTopRatedDestinations(PageRequest.of(0, 5))
                .stream()
                .map(result -> new RatedDestinationsTop(result.getCity(), result.getStars()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AgenciesTop> getTopAgencies() {
        return purchaseRepository.findTopAgencies(PageRequest.of(0, 5))
                .stream()
                .map(result -> new AgenciesTop(result.getAgency(), result.getSellsCount()))
                .toList();
    }
}
