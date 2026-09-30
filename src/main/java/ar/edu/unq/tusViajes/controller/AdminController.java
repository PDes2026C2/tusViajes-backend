package ar.edu.unq.tusViajes.controller;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.AdminRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AdminResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;
import ar.edu.unq.tusViajes.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Administradores", description = "Endpoints administrativos para la gestión de usuarios administradores")
@RestController
@RequestMapping("/api/admin/administrators")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Listar administradores", description = "Obtiene la lista de todos los administradores registrados (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de administradores", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AdminResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> getAll() {
        return ResponseEntity.ok(adminService.getAll().stream().map(AdminResponseDTO::from).toList());
    }

    @Operation(summary = "Obtener administrador por ID", description = "Obtiene los datos de un administrador según su identificador (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administrador encontrado", content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Administrador no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(AdminResponseDTO.from(adminService.getById(id)));
    }

    @Operation(summary = "Crear nuevo administrador", description = "Registra un nuevo usuario con rol ADMIN (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Administrador creado exitosamente", content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación en campos", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto por email duplicado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping
    public ResponseEntity<AdminResponseDTO> create(@Valid @RequestBody AdminRegistrationRequestDTO dto) {
        AdminResponseDTO created = Objects.requireNonNull(AdminResponseDTO.from(adminService.create(dto)), "El mapeo de admin no debe ser nulo");
        return ResponseEntity.created(URI.create("/api/admin/administrators/" + created.id())).body(created);
    }
}
