package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.Purchase;
import ar.edu.unq.tusViajes.model.TravelPackage;

public class PurchaseBuilder {

    private Buyer buyer = BuyerBuilder.aBuyer().build();
    private TravelPackage travelPackage = new TravelPackage();
    private Double price = 150000.0;

    public static PurchaseBuilder aPurchase() {
        return new PurchaseBuilder();
    }

    public PurchaseBuilder withBuyer(Buyer buyer) {
        this.buyer = buyer;
        return this;
    }

    public PurchaseBuilder withTravelPackage(TravelPackage  travelPackage) {
        this.travelPackage = travelPackage;
        return this;
    }

    public PurchaseBuilder withPrice(Double price) {
        this.price = price;
        return this;
    }

    public Purchase build() {
        Double effectivePrice = price != null ? price : (travelPackage != null ? travelPackage.getPrice() : null);
        return new Purchase(buyer, travelPackage, effectivePrice);
    }
}
