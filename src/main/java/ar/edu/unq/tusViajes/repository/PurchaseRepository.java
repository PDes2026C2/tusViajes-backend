package ar.edu.unq.tusViajes.repository;

import ar.edu.unq.tusViajes.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByBuyerId(Long buyerId);

    List<Purchase> findByTravelPackageAgencyId(Long agencyId);

    List<Purchase> findByTravelPackageId(Long travelPackageId);
}
