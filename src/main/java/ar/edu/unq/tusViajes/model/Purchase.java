package ar.edu.unq.tusViajes.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchases")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "travel_package_id", nullable = false)
    private TravelPackage travelPackage;

    @Column(nullable = false)
    private Double price;

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;

    public Purchase(Buyer buyer, TravelPackage travelPackage, Double price) {
        this.buyer = buyer;
        this.travelPackage = travelPackage;
        this.price = price;
        this.purchasedAt = LocalDateTime.now();
    }
}