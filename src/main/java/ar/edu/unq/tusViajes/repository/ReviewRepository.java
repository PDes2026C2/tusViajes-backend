package ar.edu.unq.tusViajes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unq.tusViajes.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByBuyerIdAndTravelPackageId(Long buyerId, Long travelPackageId);

    List<Review> findByTravelPackageIdOrderByCreatedAtDesc(Long travelPackageId);
}