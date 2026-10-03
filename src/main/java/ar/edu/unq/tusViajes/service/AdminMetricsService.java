package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.model.BuyersTop;
import ar.edu.unq.tusViajes.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMetricsService {

    private final PurchaseRepository purchaseRepository;

    @Transactional(readOnly = true)
    public List<BuyersTop> getTopBuyers() {
        return purchaseRepository.findTopBuyers(PageRequest.of(0, 5))
                .stream()
                .map(result -> new BuyersTop(result.getBuyer(), result.getPurchaseCount()))
                .toList();
    }
}
