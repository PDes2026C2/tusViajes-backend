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

import ar.edu.unq.tusViajes.controller.dto.request.PerfilAdminRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PerfilAdminResponseDTO;
import ar.edu.unq.tusViajes.service.PerfilAdminService;
 
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/perfiles-admin")   //admin ya que este los gestiona
@RequiredArgsConstructor
public class PerfilAdminController {
 
    private final PerfilAdminService perfilAdminService;
 
    @GetMapping
    public List<PerfilAdminResponseDTO> listar() {
        return perfilAdminService.listar();
    }
 
    @GetMapping("/{id}")
    public PerfilAdminResponseDTO buscarPorId(@PathVariable Long id) {
        return perfilAdminService.buscarPorId(id);
    }
 
    @PostMapping
    public ResponseEntity<PerfilAdminResponseDTO> crear(@Valid @RequestBody PerfilAdminRequestDTO dto) {
        PerfilAdminResponseDTO creado = perfilAdminService.crear(dto);
        return ResponseEntity.created(URI.create("/api/admin/perfiles-admin/" + creado.id())).body(creado);
    }
}