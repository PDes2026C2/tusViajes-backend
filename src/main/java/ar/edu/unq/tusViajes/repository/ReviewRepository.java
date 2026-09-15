package ar.edu.unq.tusViajes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unq.tusViajes.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBuyerIdAndTravelPackageId(Long buyerId, Long travelPackageId);

    Page<Review> findByTravelPackageIdOrderByCreatedAtDesc(Long travelPackageId, Pageable pageable);
}