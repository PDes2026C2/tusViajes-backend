package ar.edu.unq.tusViajes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.response.AgenciaResponseDTO;
import ar.edu.unq.tusViajes.service.AgenciaService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/agencias")
@RequiredArgsConstructor
public class AdminAgenciaController {

    private final AgenciaService agenciaService;

    @GetMapping("/pendientes")
    public List<AgenciaResponseDTO> listarPendientes() {
        return agenciaService.listarPendientes();
    }

    @PostMapping("/{id}/autorizar")
    public ResponseEntity<AgenciaResponseDTO> autorizar(@PathVariable Long id) {
        return ResponseEntity.ok(agenciaService.autorizar(id));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<AgenciaResponseDTO> rechazar(@PathVariable Long id) {
        return ResponseEntity.ok(agenciaService.rechazar(id));
    }
}
