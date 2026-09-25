package ar.edu.unq.tusViajes.repository;

import ar.edu.unq.tusViajes.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByBuyerId(Long buyerId);

    Page<Purchase> findByBuyerId(Long buyerId, Pageable pageable);

    List<Purchase> findByTravelPackageAgencyId(Long agencyId);

    Page<Purchase> findByTravelPackageAgencyId(Long agencyId, Pageable pageable);

    List<Purchase> findByTravelPackageId(Long travelPackageId);
}
