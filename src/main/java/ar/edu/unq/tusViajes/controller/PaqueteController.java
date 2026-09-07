package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.controller.dto.request.PaqueteRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PaqueteResponseDTO;
import ar.edu.unq.tusViajes.service.PaqueteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/paquetes")
@RequiredArgsConstructor
public class PaqueteController {

    private final PaqueteService paqueteService;

    @GetMapping
    public ResponseEntity<List<PaqueteResponseDTO>> listar() {
        return ResponseEntity.ok(paqueteService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaqueteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(paqueteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PaqueteResponseDTO> crear(@Valid @RequestBody PaqueteRequestDTO dto) {
        PaqueteResponseDTO creado = paqueteService.crear(dto);
        return ResponseEntity.created(URI.create("/api/paquetes/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaqueteResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PaqueteRequestDTO dto) {
        return ResponseEntity.ok(paqueteService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        paqueteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
