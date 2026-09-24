package ar.edu.unq.tusViajes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.UpdateAgencyRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AgencyResponseDTO;
import ar.edu.unq.tusViajes.service.AgencyService;

import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import ar.edu.unq.tusViajes.service.TravelPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/agencies")
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;
    private final TravelPackageService travelPackageService;

    @GetMapping
    public ResponseEntity<List<AgencyResponseDTO>> getAll() {
        return ResponseEntity.ok(agencyService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgencyResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(agencyService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgencyResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateAgencyRequestDTO dto) {
        return ResponseEntity.ok(agencyService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        agencyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/packages")
    public ResponseEntity<Page<TravelPackageResponseDTO>> getMyPackages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(travelPackageService.searchMine(userDetails.getId(), pageable));
    }
}
