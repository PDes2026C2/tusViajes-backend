package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Purchase;
import ar.edu.unq.tusViajes.model.TravelPackage;

public class PurchaseBuilder {

    private TravelPackage travelPackage = new TravelPackage();

    public static PurchaseBuilder aPurchase() {
        return new PurchaseBuilder();
    }

    public PurchaseBuilder withTravelPackage(TravelPackage  travelPackage) {
        this.travelPackage = travelPackage;
        return this;
    }

    public Purchase build() {
        return new Purchase(travelPackage);
    }
}
