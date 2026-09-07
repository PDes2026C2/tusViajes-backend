package ar.edu.unq.tusViajes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.CreateCompradorRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.CompradorResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PaqueteResponseDTO;
import ar.edu.unq.tusViajes.model.Comprador;
import ar.edu.unq.tusViajes.model.Paquete;
import ar.edu.unq.tusViajes.repository.CompradorRepository;
import ar.edu.unq.tusViajes.repository.PaqueteRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import ar.edu.unq.tusViajes.validator.UsuarioValidator;

@Service
public class CompradorService {

    private final EntityValidator entityValidator;
    private final UsuarioValidator usuarioValidator;
    private final CompradorRepository compradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaqueteRepository paqueteRepository;
    private final PaqueteService paqueteService;

    public CompradorService(EntityValidator entityValidator,
                            UsuarioValidator usuarioValidator,
                            CompradorRepository compradorRepository,
                            PasswordEncoder passwordEncoder,
                            PaqueteRepository paqueteRepository,
                            @Lazy PaqueteService paqueteService) {
        this.entityValidator = entityValidator;
        this.usuarioValidator = usuarioValidator;
        this.compradorRepository = compradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.paqueteRepository = paqueteRepository;
        this.paqueteService = paqueteService;
    }

    @Transactional(readOnly = true)
    public List<CompradorResponseDTO> listar() {
        return compradorRepository.findAll().stream()
                .map(CompradorResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompradorResponseDTO buscarPorId(Long id) {
        return CompradorResponseDTO.from(buscarEntidadPorId(id));
    }

    @Transactional
    public CompradorResponseDTO registrar(CreateCompradorRequestDTO dto) {
        usuarioValidator.validarEmailDisponible(dto.email());
        String hash = passwordEncoder.encode(dto.password());
        Comprador comprador = new Comprador(dto.nombre(), dto.apellido(), dto.email(), hash,
                dto.telefono(), dto.dni());
        return CompradorResponseDTO.from(compradorRepository.save(comprador));
    }

    @Transactional
    public void agregarFavorito(Long compradorId, Long paqueteId) {
        Comprador comprador = buscarEntidadPorId(compradorId);
        Paquete paquete = buscarPaquete(paqueteId);
        comprador.agregarFavorito(paquete);
    }

    @Transactional
    public void quitarFavorito(Long compradorId, Long paqueteId) {
        Comprador comprador = buscarEntidadPorId(compradorId);
        Paquete paquete = buscarPaquete(paqueteId);
        comprador.quitarFavorito(paquete);
    }

    @Transactional(readOnly = true)
    public List<PaqueteResponseDTO> listarFavoritos(Long compradorId) {
        Comprador comprador = buscarEntidadPorId(compradorId);
        return comprador.getPaquetesFavoritos().stream()
                .map(paquete -> paqueteService.buscarPorId(paquete.getId()))
                .collect(Collectors.toList());
    }

    public Comprador buscarEntidadPorId(Long id) {
        return entityValidator.findByIdOrThrow(compradorRepository, id, "Comprador");
    }

    private Paquete buscarPaquete(Long id) {
        return entityValidator.findByIdOrThrow(paqueteRepository, id, "Paquete");
    }
}
