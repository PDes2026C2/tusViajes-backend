package ar.edu.unq.tusViajes.repository;

import ar.edu.unq.tusViajes.model.Purchase;
import ar.edu.unq.tusViajes.repository.projection.AgencySellsCount;
import ar.edu.unq.tusViajes.repository.projection.BuyerPurchaseCount;
import ar.edu.unq.tusViajes.repository.projection.CitySalesCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByBuyerId(Long buyerId);

    Page<Purchase> findByBuyerId(Long buyerId, Pageable pageable);

    List<Purchase> findByTravelPackageAgencyId(Long agencyId);

    Page<Purchase> findByTravelPackageAgencyId(Long agencyId, Pageable pageable);

    List<Purchase> findByTravelPackageId(Long travelPackageId);

    @Query("""
            SELECT b AS buyer, COUNT(p) AS purchaseCount
            FROM Purchase p JOIN p.buyer b
            GROUP BY b
            ORDER BY COUNT(p) DESC, b.id ASC
            """)
    List<BuyerPurchaseCount> findTopBuyers(Pageable pageable);

    @Query("""
            SELECT c AS city, COUNT(p) AS salesCount
            FROM Purchase p JOIN p.travelPackage.hotel.city c
            GROUP BY c
            ORDER BY COUNT(p) DESC, c.id ASC
            """)
    List<CitySalesCount> findTopDestinations(Pageable pageable);

    @Query("""
            SELECT a AS agency, COUNT(p) AS sellsCount
            FROM Purchase p JOIN p.travelPackage.agency a
            GROUP BY a
            ORDER BY COUNT(p) DESC, a.id ASC
            """)
    List<AgencySellsCount> findTopAgencies(Pageable pageable);
}
