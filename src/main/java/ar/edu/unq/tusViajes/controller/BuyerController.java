package ar.edu.unq.tusViajes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
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
import ar.edu.unq.tusViajes.service.BuyerService;
import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<BuyerResponseDTO> register(@Valid @RequestBody BuyerRegistrationRequestDTO dto) {
        BuyerResponseDTO created = buyerService.register(dto);
        return ResponseEntity.created(URI.create("/api/buyers/" + created.id())).body(created);
    }

    @PostMapping("/{buyerId}/favorites/{travelPackageId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Long buyerId, @PathVariable Long travelPackageId) {
        buyerService.addFavorite(buyerId, travelPackageId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{buyerId}/favorites/{travelPackageId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long buyerId, @PathVariable Long travelPackageId) {
        buyerService.removeFavorite(buyerId, travelPackageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{buyerId}/favorites")
    public ResponseEntity<List<TravelPackageResponseDTO>> getFavorites(@PathVariable Long buyerId) {
        return ResponseEntity.ok(buyerService.getFavorites(buyerId));
    }
}
