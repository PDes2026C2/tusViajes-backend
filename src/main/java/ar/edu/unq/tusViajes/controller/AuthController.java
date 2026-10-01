package ar.edu.unq.tusViajes.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.AgencyRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.BuyerRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.LoginRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.RefreshTokenRequest;
import ar.edu.unq.tusViajes.controller.dto.response.AgencyRegistrationResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;
import ar.edu.unq.tusViajes.controller.dto.response.LoginResponseDTO;
import ar.edu.unq.tusViajes.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Autenticación", description = "Endpoints para inicio de sesión, registro público y refresco de tokens JWT")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario (Admin, Agencia o Comprador) y retorna tokens JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa", content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación en campos", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Agencia pendiente de autorización", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Registrar nueva agencia", description = "Registra una postulación de agencia con estado inicial PENDING.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agencia registrada con éxito", content = @Content(schema = @Schema(implementation = AgencyRegistrationResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación en datos de agencia", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto por email o CUIT duplicado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/register/agency")
    public ResponseEntity<AgencyRegistrationResponseDTO> registerAgency(@Valid @RequestBody AgencyRegistrationRequestDTO dto) {
        AgencyRegistrationResponseDTO response = authService.registerAgency(dto);
        return ResponseEntity.created(URI.create("/api/agencies/" + response.id())).body(response);
    }

    @Operation(summary = "Registrar nuevo comprador", description = "Registra un comprador e inicia sesión automáticamente retornando tokens JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comprador registrado con éxito y sesión iniciada", content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación en datos del comprador", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto por email duplicado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/register/buyer")
    public ResponseEntity<LoginResponseDTO> registerBuyer(@Valid @RequestBody BuyerRegistrationRequestDTO dto) {
        LoginResponseDTO response = authService.registerBuyer(dto);
        return ResponseEntity.created(URI.create("/api/buyers/" + response.id())).body(response);
    }

    @Operation(summary = "Refrescar token de acceso", description = "Genera un nuevo token de acceso a partir de un refresh token válido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refrescado con éxito", content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Refresh token no enviado o inválido", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token expirado o no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequest dto) {
        return ResponseEntity.ok(authService.refreshToken(dto.refreshToken()));
    }
}
