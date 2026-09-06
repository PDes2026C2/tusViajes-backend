package ar.edu.unq.tusViajes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.AgenciaRequestDTO;
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
    public List<AgenciaResponseDTO> listar() {
        return agenciaService.listar();
    }
 
    @GetMapping("/{id}")
    public AgenciaResponseDTO buscarPorId(@PathVariable Long id) {
        return agenciaService.buscarPorId(id);
    }
 
    @PostMapping
    public ResponseEntity<AgenciaResponseDTO> crear(@Valid @RequestBody AgenciaRequestDTO dto) {
        AgenciaResponseDTO creada = agenciaService.crear(dto);
        return ResponseEntity.created(URI.create("/api/agencias/" + creada.id())).body(creada);
    }
 
    @PutMapping("/{id}")
    public AgenciaResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody AgenciaRequestDTO dto) {
        return agenciaService.actualizar(id, dto);
    }
 
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        agenciaService.eliminar(id);
    }
}
