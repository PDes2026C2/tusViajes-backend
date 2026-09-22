package ar.edu.unq.tusViajes.model;

import java.util.HashSet;
import java.util.Set;

import ar.edu.unq.tusViajes.exception.ReviewNotAllowedException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "buyers")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Buyer extends User {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "national_id", unique = true, nullable = false, length = 8)
    private String nationalId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "buyer_favorites",
            joinColumns = @JoinColumn(name = "buyer_id"),
            inverseJoinColumns = @JoinColumn(name = "travel_package_id")
    )
    private Set<TravelPackage> favoriteTravelPackages = new HashSet<>();

    @OneToMany(mappedBy = "buyer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Purchase> travelPackagesPurchased = new HashSet<>();

    public Buyer(String firstName, String lastName, String email,
                 String passwordHash, String phoneNumber, String nationalId) {
        super(email, passwordHash);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.nationalId = nationalId;
    }

    public void buy(TravelPackage travelPackage) {
        if (travelPackage == null || travelPackage.getPrice() == null) {
            throw new IllegalArgumentException("El paquete de viaje y su precio no pueden ser nulos");
        }
        Purchase purchase = new Purchase(this, travelPackage, travelPackage.getPrice());
        this.travelPackagesPurchased.add(purchase);
    }

    public void addPurchase(Purchase purchase) {
        this.travelPackagesPurchased.add(purchase);
    }

    public void addFavorite(TravelPackage travelPackage) {
        this.favoriteTravelPackages.add(travelPackage);
    }

    public void removeFavorite(TravelPackage travelPackage) {
        this.favoriteTravelPackages.remove(travelPackage);
    }

    @Override
    public Role getRole() {
        return Role.BUYER;
    }

    @Override
    public String getVisualIdentifier() {
        return firstName + " " + lastName;
    }

    public boolean hasAcquired(TravelPackage travelPackage) {
        if (travelPackage == null || travelPackage.getId() == null) return false;
        return this.travelPackagesPurchased.stream()
                .anyMatch(purchase -> purchase.getTravelPackage() != null
                        && travelPackage.getId().equals(purchase.getTravelPackage().getId()));
    }


    public void ensureCanReview(TravelPackage travelPackage) {
        if (!hasAcquired(travelPackage)) {
            throw new ReviewNotAllowedException("Buyer didn't purchase this travel package yet");
        }
        if (!travelPackage.hasEnded()) {
            throw new ReviewNotAllowedException("Can't review a travel package before end date.");
        }
    }

}
