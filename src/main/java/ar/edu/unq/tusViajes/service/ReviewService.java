package ar.edu.unq.tusViajes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.ReviewRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ReviewResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.ReviewNotAllowedException;
import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.Review;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.ReviewRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BuyerService buyerService;
    private final TravelPackageService travelPackageService;

    public ReviewService(ReviewRepository reviewRepository,
                         EntityValidator entityValidator,
                         BuyerService buyerService,
                         TravelPackageService travelPackageService) {
        this.reviewRepository = reviewRepository;
        this.buyerService = buyerService;
        this.travelPackageService = travelPackageService;
    }

    @Transactional
    public ReviewResponseDTO create(Long buyerId, Long travelPackageId, ReviewRequestDTO dto) {
        if (reviewRepository.existsByBuyerIdAndTravelPackageId(buyerId, travelPackageId)) {
            throw new DuplicateResourceException("Buyer already reviewed this travel package");
        }

        Buyer buyer = buyerService.getEntityById(buyerId);
        TravelPackage travelPackage = travelPackageService.getEntityById(travelPackageId);
        ensureFavorite(buyer, travelPackage);
        Review review = new Review(dto.score(), dto.comment(), buyer, travelPackage);
        return ReviewResponseDTO.from(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponseDTO update(Long buyerId, Long travelPackageId, ReviewRequestDTO dto) {
        Buyer buyer = buyerService.getEntityById(buyerId);
        TravelPackage travelPackage = travelPackageService.getEntityById(travelPackageId);
        ensureFavorite(buyer, travelPackage);
        Review review = reviewRepository.findByBuyerIdAndTravelPackageId(buyerId, travelPackageId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found for buyer and travel package"));
        review.update(dto.score(), dto.comment());
        return ReviewResponseDTO.from(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getByTravelPackageId(Long travelPackageId) {
        travelPackageService.getEntityById(travelPackageId);
        return reviewRepository.findByTravelPackageIdOrderByCreatedAtDesc(travelPackageId).stream()
                .map(ReviewResponseDTO::from)
                .collect(Collectors.toList());
    }

    private void ensureFavorite(Buyer buyer, TravelPackage travelPackage) {
        if (!buyer.getFavoriteTravelPackages().contains(travelPackage)) {
            throw new ReviewNotAllowedException();
        }
    }
}