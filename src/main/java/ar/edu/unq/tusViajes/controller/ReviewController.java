package ar.edu.unq.tusViajes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.ReviewRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ReviewResponseDTO;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import ar.edu.unq.tusViajes.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/travel-packages/{travelPackageId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getByTravelPackageId(@PathVariable Long travelPackageId) {
        return ResponseEntity.ok(reviewService.getByTravelPackageId(travelPackageId));
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long travelPackageId,
            @Valid @RequestBody ReviewRequestDTO dto) {
        ReviewResponseDTO created = reviewService.create(userDetails.getId(), travelPackageId, dto);
        return ResponseEntity.created(URI.create("/api/travel-packages/" + travelPackageId + "/reviews/" + created.id()))
                .body(created);
    }

}