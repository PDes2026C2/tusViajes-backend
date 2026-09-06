package ar.edu.unq.tusViajes.controller;

import java.net.URI;
import java.util.List;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.PerfilAgenciaRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PerfilAgenciaResponseDTO;
import ar.edu.unq.tusViajes.service.PerfilAgenciaService;
 
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
 
@RestController
@RequestMapping("/api/admin/perfiles-agencia") //admin ya que este los gestiona
@RequiredArgsConstructor
public class PerfilAgenciaController {
 
    private final PerfilAgenciaService perfilAgenciaService;
 
    @GetMapping
    public List<PerfilAgenciaResponseDTO> listar() {
        return perfilAgenciaService.listar();
    }
 
    @GetMapping("/{id}")
    public PerfilAgenciaResponseDTO buscarPorId(@PathVariable Long id) {
        return perfilAgenciaService.buscarPorId(id);
    }
 
    @PostMapping
    public ResponseEntity<PerfilAgenciaResponseDTO> crear(@Valid @RequestBody PerfilAgenciaRequestDTO dto) {
        PerfilAgenciaResponseDTO creado = perfilAgenciaService.crear(dto);
        return ResponseEntity.created(URI.create("/api/admin/perfiles-agencia/" + creado.id())).body(creado);
    }
}
