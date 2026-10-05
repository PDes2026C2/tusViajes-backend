package ar.edu.unq.tusViajes.repository.projection;

import ar.edu.unq.tusViajes.model.Buyer;

public interface BuyerPurchaseCount {
    Buyer getBuyer();
    Long getPurchaseCount();
}
