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

import ar.edu.unq.tusViajes.controller.dto.request.RegistroCompradorRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.CompradorResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PaqueteResponseDTO;
import ar.edu.unq.tusViajes.service.CompradorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/compradores")
@RequiredArgsConstructor
public class CompradorController {

    private final CompradorService compradorService;

    @GetMapping
    public ResponseEntity<List<CompradorResponseDTO>> listar() {
        return ResponseEntity.ok(compradorService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompradorResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(compradorService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CompradorResponseDTO> registrar(@Valid @RequestBody RegistroCompradorRequestDTO dto) {
        CompradorResponseDTO creado = compradorService.registrar(dto);
        return ResponseEntity.created(URI.create("/api/compradores/" + creado.id())).body(creado);
    }

    @PostMapping("/{compradorId}/favoritos/{paqueteId}")
    public ResponseEntity<Void> agregarFavorito(@PathVariable Long compradorId, @PathVariable Long paqueteId) {
        compradorService.agregarFavorito(compradorId, paqueteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{compradorId}/favoritos/{paqueteId}")
    public ResponseEntity<Void> quitarFavorito(@PathVariable Long compradorId, @PathVariable Long paqueteId) {
        compradorService.quitarFavorito(compradorId, paqueteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{compradorId}/favoritos")
    public ResponseEntity<List<PaqueteResponseDTO>> listarFavoritos(@PathVariable Long compradorId) {
        return ResponseEntity.ok(compradorService.listarFavoritos(compradorId));
    }
}
