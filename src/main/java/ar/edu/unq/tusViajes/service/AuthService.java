package ar.edu.unq.tusViajes.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.CreateCompradorRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.LoginRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.RegistroAgenciaRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.CompradorResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.LoginResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.RegistroAgenciaResponseDTO;
import ar.edu.unq.tusViajes.exception.AgenciaNoAutorizadaException;
import ar.edu.unq.tusViajes.exception.CredencialesInvalidasException;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.Usuario;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import ar.edu.unq.tusViajes.repository.UsuarioRepository;
import ar.edu.unq.tusViajes.security.JwtTokenService;
import ar.edu.unq.tusViajes.validator.UsuarioValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AgenciaRepository agenciaRepository;
    private final CompradorService compradorService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UsuarioValidator usuarioValidator;

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales invalidas"));

        if (!passwordEncoder.matches(dto.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException("Credenciales invalidas");
        }

        if (!usuario.isActivo()) {
            throw new AgenciaNoAutorizadaException("La agencia se encuentra pendiente de autorizacion por un administrador");
        }

        String token = jwtTokenService.generarToken(usuario);

        return new LoginResponseDTO(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getEmail(),
                usuario.getIdentificadorVisual(),
                usuario.getRol().name()
        );
    }

    @Transactional
    public RegistroAgenciaResponseDTO registrarAgencia(RegistroAgenciaRequestDTO dto) {
        usuarioValidator.validarEmailDisponible(dto.email());

        if (agenciaRepository.existsByCuit(dto.cuit())) {
            throw new DuplicateResourceException("Ya existe una agencia con el CUIT " + dto.cuit());
        }

        String hash = passwordEncoder.encode(dto.password());
        Agencia agencia = new Agencia(dto.email(), hash, dto.razonSocial(), dto.cuit());
        Agencia guardada = agenciaRepository.save(agencia);

        return RegistroAgenciaResponseDTO.from(
                guardada,
                "Propuesta de registro recibida. Pendiente de autorizacion por un administrador."
        );
    }

    @Transactional
    public CompradorResponseDTO registrarComprador(CreateCompradorRequestDTO dto) {
        return compradorService.registrar(dto);
    }
}
