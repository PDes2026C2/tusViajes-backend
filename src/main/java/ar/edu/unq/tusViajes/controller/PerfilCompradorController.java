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

import ar.edu.unq.tusViajes.controller.dto.request.PerfilCompradorRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PaqueteResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PerfilCompradorResponseDTO;
import ar.edu.unq.tusViajes.service.PerfilCompradorService;
 
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/api/compradores")
@RequiredArgsConstructor
public class PerfilCompradorController {
 
    private final PerfilCompradorService perfilCompradorService;
 
    @GetMapping   //Este deberia ser solo admin?¿
    public List<PerfilCompradorResponseDTO> listar() {
        return perfilCompradorService.listar();
    }
 
    @GetMapping("/{id}")
    public PerfilCompradorResponseDTO buscarPorId(@PathVariable Long id) {
        return perfilCompradorService.buscarPorId(id);
    }
 
    @PostMapping
    public ResponseEntity<PerfilCompradorResponseDTO> registrar(@Valid @RequestBody PerfilCompradorRequestDTO dto) {
        PerfilCompradorResponseDTO creado = perfilCompradorService.registrar(dto);
        return ResponseEntity.created(URI.create("/api/compradores/" + creado.id())).body(creado);
    }

    @PostMapping("/{compradorId}/favoritos/{paqueteId}")
    public ResponseEntity<Void> agregarFavorito(@PathVariable Long compradorId, @PathVariable Long paqueteId) {
        perfilCompradorService.agregarFavorito(compradorId, paqueteId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{compradorId}/favoritos/{paqueteId}")
    public ResponseEntity<Void> quitarFavorito(@PathVariable Long compradorId, @PathVariable Long paqueteId) {
        perfilCompradorService.quitarFavorito(compradorId, paqueteId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{compradorId}/favoritos")
    public ResponseEntity<List<PaqueteResponseDTO>> listarFavoritos(@PathVariable Long compradorId) {
        return ResponseEntity.ok(perfilCompradorService.listarFavoritos(compradorId));
    }
}