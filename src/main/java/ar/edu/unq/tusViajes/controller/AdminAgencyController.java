package ar.edu.unq.tusViajes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.response.AgencyResponseDTO;
import ar.edu.unq.tusViajes.service.AgencyService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/agencies")
@RequiredArgsConstructor
public class AdminAgencyController {

    private final AgencyService agencyService;

    @GetMapping("/pending")
    public ResponseEntity<List<AgencyResponseDTO>> getPending() {
        return ResponseEntity.ok(agencyService.getPending());
    }

    @PostMapping("/{id}/authorize")
    public ResponseEntity<AgencyResponseDTO> authorize(@PathVariable Long id) {
        return ResponseEntity.ok(agencyService.authorize(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AgencyResponseDTO> reject(@PathVariable Long id) {
        return ResponseEntity.ok(agencyService.reject(id));
    }
}
