package ar.edu.unq.tusViajes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.BuyerRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.BuyerResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import ar.edu.unq.tusViajes.service.BuyerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
@Slf4j
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

    /**
     * @deprecated Use POST /api/auth/register/buyer instead. This endpoint will be removed in a future release.
     * It is kept for backward compatibility and does not return authentication tokens.
     */
    @Deprecated(since = "2026-09", forRemoval = true)
    @PostMapping
    public ResponseEntity<BuyerResponseDTO> register(@Valid @RequestBody BuyerRegistrationRequestDTO dto) {
        log.warn("Deprecated POST /api/buyers called for email {}", dto.email());
        BuyerResponseDTO created = buyerService.register(dto);
        return ResponseEntity.created(URI.create("/api/buyers/" + created.id()))
                .header("Deprecation", "true")
                .header("Sunset", "2026-12-31")
                .body(created);
    }

    @PostMapping("/favorites/{travelPackageId}")
    public ResponseEntity<Void> addFavorite(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable Long travelPackageId) {
        buyerService.addFavorite(userDetails.getId(), travelPackageId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favorites/{travelPackageId}")
    public ResponseEntity<Void> removeFavorite(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable Long travelPackageId) {
        buyerService.removeFavorite(userDetails.getId(), travelPackageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<TravelPackageResponseDTO>> getFavorites(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(buyerService.getFavorites(userDetails.getId()));
    }
}
