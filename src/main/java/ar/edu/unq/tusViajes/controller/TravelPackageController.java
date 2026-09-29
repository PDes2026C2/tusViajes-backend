package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.UpdateTravelPackageRequestDTO;
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
        return ResponseEntity.ok(travelPackageService.search(pageable).map(TravelPackageResponseDTO::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPackageResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(TravelPackageResponseDTO.from(travelPackageService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<TravelPackageResponseDTO> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TravelPackageRequestDTO dto) {
        TravelPackageResponseDTO created = TravelPackageResponseDTO.from(travelPackageService.create(dto, userDetails.getId()));
        return ResponseEntity.created(URI.create("/api/travel-packages/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TravelPackageResponseDTO> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateTravelPackageRequestDTO dto) {
        return ResponseEntity.ok(TravelPackageResponseDTO.from(travelPackageService.update(userDetails.getId(), dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        travelPackageService.delete(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
