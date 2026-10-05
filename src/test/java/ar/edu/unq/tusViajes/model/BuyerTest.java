package ar.edu.unq.tusViajes.model;

import ar.edu.unq.tusViajes.builder.TravelPackageBuilder;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.PackageAlreadyStartedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void buy_createsAndAddsPurchaseToPurchasedCollection() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withId(10L)
                .withPrice(150000.0)
                .build();

        Purchase purchase = buyer.buy(travelPackage);

        assertThat(purchase).isNotNull();
        assertThat(purchase.getBuyer()).isEqualTo(buyer);
        assertThat(purchase.getTravelPackage()).isEqualTo(travelPackage);
        assertThat(purchase.getPrice()).isEqualTo(150000.0);
        assertThat(buyer.getTravelPackagesPurchased()).contains(purchase);
        assertThat(buyer.hasAcquired(travelPackage)).isTrue();
    }


    @Test
    void hasAcquired_returnsFalseWhenPackageNotPurchasedOrNull() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage().withId(10L).build();

        assertThat(buyer.hasAcquired(null)).isFalse();
        assertThat(buyer.hasAcquired(travelPackage)).isFalse();
    }

    @Test
    void ensureCanBuy_succeedsWhenPackageIsValid() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withId(10L)
                .withStartDate(LocalDateTime.now().plusDays(5))
                .build();

        assertThatCode(() -> buyer.ensureCanBuy(travelPackage))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureCanBuy_throwsWhenTravelPackageIsNull() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");

        assertThatThrownBy(() -> buyer.ensureCanBuy(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El paquete de viaje no puede ser nulo");
    }

    @Test
    void ensureCanBuy_throwsWhenTravelPackageHasStarted() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withId(10L)
                .withStartDate(LocalDateTime.now().minusDays(1))
                .build();

        assertThatThrownBy(() -> buyer.ensureCanBuy(travelPackage))
                .isInstanceOf(PackageAlreadyStartedException.class)
                .hasMessageContaining("No se puede comprar un paquete de viaje que ya ha comenzado");
    }

    @Test
    void ensureCanBuy_throwsWhenTravelPackageAlreadyAcquired() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withId(10L)
                .withStartDate(LocalDateTime.now().plusDays(5))
                .build();

        buyer.buy(travelPackage);

        assertThatThrownBy(() -> buyer.ensureCanBuy(travelPackage))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El comprador ya adquirió este paquete de viaje");
    }

    @Test
    void buy_throwsWhenTravelPackageHasStarted() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withId(10L)
                .withStartDate(LocalDateTime.now().minusDays(1))
                .build();

        assertThatThrownBy(() -> buyer.buy(travelPackage))
                .isInstanceOf(PackageAlreadyStartedException.class)
                .hasMessageContaining("No se puede comprar un paquete de viaje que ya ha comenzado");
    }

    @Test
    void buy_throwsWhenTravelPackageAlreadyAcquired() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withId(10L)
                .withStartDate(LocalDateTime.now().plusDays(5))
                .build();

        buyer.buy(travelPackage);

        assertThatThrownBy(() -> buyer.buy(travelPackage))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("El comprador ya adquirió este paquete de viaje");
    }

    @Test
    void removePurchase_removesPurchaseFromCollection() {
        Buyer buyer = new Buyer("Maria", "Lopez", "maria@example.com",
                "hash123", "11-9999-8888", "40123456");
        TravelPackage travelPackage = TravelPackageBuilder.aTravelPackage()
                .withId(10L)
                .withStartDate(LocalDateTime.now().plusDays(5))
                .build();

        Purchase purchase = buyer.buy(travelPackage);
        assertThat(buyer.getTravelPackagesPurchased()).hasSize(1);
        assertThat(buyer.hasAcquired(travelPackage)).isTrue();

        buyer.removePurchase(purchase);

        assertThat(buyer.getTravelPackagesPurchased()).isEmpty();
        assertThat(buyer.hasAcquired(travelPackage)).isFalse();
    }
}
