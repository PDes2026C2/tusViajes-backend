package ar.edu.unq.tusViajes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ar.edu.unq.tusViajes.model.Review;
import ar.edu.unq.tusViajes.repository.projection.CityAverageRating;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBuyerIdAndTravelPackageId(Long buyerId, Long travelPackageId);

    Page<Review> findByTravelPackageIdOrderByCreatedAtDesc(Long travelPackageId, Pageable pageable);

    @Query("""
            SELECT c AS city, AVG(r.score) AS stars
            FROM Review r JOIN r.travelPackage.hotel.city c
            WHERE r.score IS NOT NULL
            GROUP BY c
            ORDER BY AVG(r.score) DESC, c.id ASC
            """)
    List<CityAverageRating> findTopRatedDestinations(Pageable pageable);
}
