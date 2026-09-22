package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.controller.dto.response.PurchaseResponseDTO;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import ar.edu.unq.tusViajes.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping("/{travelPackageId}")
    public ResponseEntity<PurchaseResponseDTO> purchase(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long travelPackageId) {
        PurchaseResponseDTO created = purchaseService.purchase(userDetails.getId(), travelPackageId);
        return ResponseEntity.created(URI.create("/api/purchases/" + created.id())).body(created);
    }
}
