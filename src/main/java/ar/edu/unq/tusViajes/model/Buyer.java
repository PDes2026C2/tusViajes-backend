package ar.edu.unq.tusViajes.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
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

    public Buyer(String firstName, String lastName, String email,
                 String passwordHash, String phoneNumber, String nationalId) {
        super(email, passwordHash);
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.nationalId = nationalId;
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
}
