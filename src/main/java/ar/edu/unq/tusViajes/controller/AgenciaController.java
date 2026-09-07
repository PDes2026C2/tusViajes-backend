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

import ar.edu.unq.tusViajes.controller.dto.request.UpdateAgenciaRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AgenciaResponseDTO;
import ar.edu.unq.tusViajes.service.AgenciaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/agencias")
@RequiredArgsConstructor
public class AgenciaController {
 
    private final AgenciaService agenciaService;
 
    @GetMapping
    public ResponseEntity<List<AgenciaResponseDTO>> listar() {
        return ResponseEntity.ok(agenciaService.listar());
    }
 
    @GetMapping("/{id}")
    public ResponseEntity<AgenciaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(agenciaService.buscarPorId(id));
    }
 
    @PutMapping("/{id}")
    public ResponseEntity<AgenciaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody UpdateAgenciaRequestDTO dto) {
        return ResponseEntity.ok(agenciaService.actualizar(id, dto));
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        agenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
