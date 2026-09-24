package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import ar.edu.unq.tusViajes.service.TravelPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping({"/api/travel-packages"})
@RequiredArgsConstructor
public class TravelPackageController {

    private final TravelPackageService travelPackageService;

    @GetMapping
    public ResponseEntity<Page<TravelPackageResponseDTO>> search(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(travelPackageService.search(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPackageResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(travelPackageService.getById(id));
    }

    @PostMapping
    public ResponseEntity<TravelPackageResponseDTO> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TravelPackageRequestDTO dto) {
        TravelPackageResponseDTO created = travelPackageService.create(userDetails.getId(), dto);
        return ResponseEntity.created(URI.create("/api/travel-packages/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TravelPackageResponseDTO> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id, @Valid @RequestBody TravelPackageRequestDTO dto) {
        return ResponseEntity.ok(travelPackageService.update(userDetails.getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        travelPackageService.delete(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
