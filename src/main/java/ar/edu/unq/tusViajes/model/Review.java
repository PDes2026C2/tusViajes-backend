package ar.edu.unq.tusViajes.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"buyer_id", "travel_package_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Integer score;

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "travel_package_id", nullable = false)
    private TravelPackage travelPackage;

    public Review(Integer score, String comment, Buyer buyer, TravelPackage travelPackage) {
        this.score = score;
        this.comment = normalizeComment(comment);
        this.createdAt = LocalDateTime.now();
        this.buyer = buyer;
        this.travelPackage = travelPackage;
    }

    private String normalizeComment(String comment) {
        return comment == null ? "" : comment;
    }
}