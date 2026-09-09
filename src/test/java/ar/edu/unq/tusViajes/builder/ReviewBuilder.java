package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.Review;
import ar.edu.unq.tusViajes.model.TravelPackage;

public class ReviewBuilder {

    private Integer score = 8;
    private String comment = "Great trip";
    private Buyer buyer;
    private TravelPackage travelPackage;

    public static ReviewBuilder aReview() {
        return new ReviewBuilder();
    }

    public ReviewBuilder withScore(Integer score) {
        this.score = score;
        return this;
    }

    public ReviewBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public ReviewBuilder withBuyer(Buyer buyer) {
        this.buyer = buyer;
        return this;
    }

    public ReviewBuilder withTravelPackage(TravelPackage travelPackage) {
        this.travelPackage = travelPackage;
        return this;
    }

    public Review build() {
        return new Review(score, comment, buyer, travelPackage);
    }
}