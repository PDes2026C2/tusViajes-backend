package ar.edu.unq.tusViajes.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<Page<ReviewResponseDTO>> getByTravelPackageId(
            @PathVariable Long travelPackageId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getByTravelPackageId(travelPackageId, pageable));
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