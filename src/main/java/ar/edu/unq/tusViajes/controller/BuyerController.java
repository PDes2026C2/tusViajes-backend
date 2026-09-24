package ar.edu.unq.tusViajes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.response.BuyerResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import ar.edu.unq.tusViajes.service.BuyerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    @GetMapping
    public ResponseEntity<List<BuyerResponseDTO>> getAll() {
        return ResponseEntity.ok(buyerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuyerResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(buyerService.getById(id));
    }

    @PostMapping("/me/favorites/{travelPackageId}")
    public ResponseEntity<Void> addFavorite(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable Long travelPackageId) {
        buyerService.addFavorite(userDetails.getId(), travelPackageId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me/favorites/{travelPackageId}")
    public ResponseEntity<Void> removeFavorite(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable Long travelPackageId) {
        buyerService.removeFavorite(userDetails.getId(), travelPackageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/favorites")
    public ResponseEntity<List<TravelPackageResponseDTO>> getFavorites(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(buyerService.getFavorites(userDetails.getId()));
    }
}
