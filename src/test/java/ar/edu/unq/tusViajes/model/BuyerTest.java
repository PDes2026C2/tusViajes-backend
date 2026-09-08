package ar.edu.unq.tusViajes.model;

import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuyerTest {

    @Test
    void createBuyer_assignsBasicAndSpecificFields() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");

        assertThat(buyer.getFirstName()).isEqualTo("Maria");
        assertThat(buyer.getLastName()).isEqualTo("Lopez");
        assertThat(buyer.getEmail()).isEqualTo("maria@example.com");
        assertThat(buyer.getPhoneNumber()).isEqualTo("11-9999-8888");
        assertThat(buyer.getNationalId()).isEqualTo("40123456");
        assertThat(buyer.getRole()).isEqualTo(Role.BUYER);
        assertThat(buyer.isActive()).isTrue();
    }

    @Test
    void addFavorite_addsPackageToCollection() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().withName("Trip to Mendoza").build();

        buyer.addFavorite(travelPackage);

        assertThat(buyer.getFavoriteTravelPackages()).hasSize(1);
        assertThat(buyer.getFavoriteTravelPackages()).contains(travelPackage);
    }

    @Test
    void addFavorite_doesNotAddDuplicateIfSamePackageProvided() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().build();

        buyer.addFavorite(travelPackage);
        buyer.addFavorite(travelPackage);

        assertThat(buyer.getFavoriteTravelPackages()).hasSize(1);
    }

    @Test
    void removeFavorite_removesPackageFromCollection() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().build();
        buyer.addFavorite(travelPackage);

        buyer.removeFavorite(travelPackage);

        assertThat(buyer.getFavoriteTravelPackages()).isEmpty();
    }
}
