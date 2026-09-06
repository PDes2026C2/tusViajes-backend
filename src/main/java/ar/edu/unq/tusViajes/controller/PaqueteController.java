package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.controller.dto.request.PaqueteRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PaqueteResponseDTO;
import ar.edu.unq.tusViajes.service.PaqueteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public List<PaqueteResponseDTO> listar() {
        return paqueteService.listar();
    }

    @GetMapping("/{id}")
    public PaqueteResponseDTO buscarPorId(@PathVariable Long id) {
        return paqueteService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<PaqueteResponseDTO> crear(@Valid @RequestBody PaqueteRequestDTO dto) {
        PaqueteResponseDTO creado = paqueteService.crear(dto);
        return ResponseEntity.created(URI.create("/api/paquetes/" + creado.getId())).body(creado);
    }

    @PutMapping("/{id}")
    public PaqueteResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody PaqueteRequestDTO dto) {
        return paqueteService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        paqueteService.eliminar(id);
    }
}
