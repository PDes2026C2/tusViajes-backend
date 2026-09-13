package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.service.TravelPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TravelPackageResponseDTO> create(@Valid @RequestBody TravelPackageRequestDTO dto) {
        TravelPackageResponseDTO created = travelPackageService.create(dto);
        return ResponseEntity.created(URI.create("/api/travel-packages/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TravelPackageResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TravelPackageRequestDTO dto) {
        return ResponseEntity.ok(travelPackageService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        travelPackageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
