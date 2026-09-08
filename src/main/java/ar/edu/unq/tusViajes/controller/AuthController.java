package ar.edu.unq.tusViajes.controller;

import java.net.URI;

import ar.edu.unq.tusViajes.controller.dto.request.RefreshTokenRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.RegistroCompradorRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.LoginRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.RegistroAgenciaRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.CompradorResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.LoginResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.RegistroAgenciaResponseDTO;
import ar.edu.unq.tusViajes.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/registro/agencia")
    public ResponseEntity<RegistroAgenciaResponseDTO> registrarAgencia(@Valid @RequestBody RegistroAgenciaRequestDTO dto) {
        RegistroAgenciaResponseDTO respuesta = authService.registrarAgencia(dto);
        return ResponseEntity.created(URI.create("/api/agencias/" + respuesta.id())).body(respuesta);
    }

    @PostMapping("/registro/comprador")
    public ResponseEntity<CompradorResponseDTO> registrarComprador(@Valid @RequestBody RegistroCompradorRequestDTO dto) {
        CompradorResponseDTO respuesta = authService.registrarComprador(dto);
        return ResponseEntity.created(URI.create("/api/compradores/" + respuesta.id())).body(respuesta);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequest dto) {
        return ResponseEntity.ok(authService.refreshToken(dto.refreshToken()));
    }
}
