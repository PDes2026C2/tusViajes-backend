package ar.edu.unq.tusViajes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.PerfilCompradorRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PaqueteResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PerfilCompradorResponseDTO;
import ar.edu.unq.tusViajes.model.Paquete;
import ar.edu.unq.tusViajes.model.PerfilComprador;
import ar.edu.unq.tusViajes.repository.PaqueteRepository;
import ar.edu.unq.tusViajes.repository.PerfilCompradorRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import ar.edu.unq.tusViajes.validator.UsuarioValidator;
import lombok.RequiredArgsConstructor;
 
@Service
@RequiredArgsConstructor
public class PerfilCompradorService {

    private final EntityValidator entityValidator;
    private final UsuarioValidator usuarioValidator;
    private final PerfilCompradorRepository perfilCompradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaqueteRepository paqueteRepository;
    private final PaqueteService paqueteService;
 
    @Transactional(readOnly = true)
    public List<PerfilCompradorResponseDTO> listar() {
        return perfilCompradorRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
 
    @Transactional(readOnly = true)
    public PerfilCompradorResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadPorId(id));
    }
 
    @Transactional
    public PerfilCompradorResponseDTO registrar(PerfilCompradorRequestDTO dto) {
        usuarioValidator.validarEmailDisponible(dto.email());
        String hash = passwordEncoder.encode(dto.password());
        PerfilComprador perfil = new PerfilComprador(dto.nombre(), dto.apellido(), dto.email(), hash,
                dto.telefono(), dto.dni());
        return toResponseDTO(perfilCompradorRepository.save(perfil));
    }
 
    @Transactional
    public void agregarFavorito(Long compradorId, Long paqueteId) {
        PerfilComprador comprador = buscarEntidadPorId(compradorId);
        Paquete paquete = buscarPaquete(paqueteId);

        comprador.agregarFavorito(paquete);
    }

    @Transactional
    public void quitarFavorito(Long compradorId, Long paqueteId) {
        PerfilComprador comprador = buscarEntidadPorId(compradorId);
        Paquete paquete = buscarPaquete(paqueteId);

        comprador.quitarFavorito(paquete);
    }

    @Transactional(readOnly = true)
    public List<PaqueteResponseDTO> listarFavoritos(Long compradorId) {
        PerfilComprador comprador = buscarEntidadPorId(compradorId);

        
        return comprador.getPaquetesFavoritos().stream()
                .map(paquete -> paqueteService.buscarPorId(paquete.getId())) 
                .collect(Collectors.toList());
    }

    private PerfilComprador buscarEntidadPorId(Long id) {
        return entityValidator.findByIdOrThrow(perfilCompradorRepository,id,"Comprador");
    }
 
    private PerfilCompradorResponseDTO toResponseDTO(PerfilComprador comprador) {
        return new PerfilCompradorResponseDTO(comprador.getId(), comprador.getNombre(), comprador.getApellido(),
                comprador.getEmail(), comprador.getTelefono(), comprador.getDni());
    }

    private Paquete buscarPaquete(Long id) {
        return entityValidator.findByIdOrThrow(paqueteRepository, id, "Paquete");
    }
}