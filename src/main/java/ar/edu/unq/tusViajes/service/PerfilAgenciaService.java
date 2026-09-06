package ar.edu.unq.tusViajes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.PerfilAgenciaRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PerfilAgenciaResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.PerfilAgencia;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import ar.edu.unq.tusViajes.repository.PerfilAgenciaRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import ar.edu.unq.tusViajes.validator.UsuarioValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerfilAgenciaService {
    
    private final UsuarioValidator usuarioValidator;
    private final EntityValidator entityValidator;
    private final PerfilAgenciaRepository perfilAgenciaRepository;
    private final AgenciaRepository agenciaRepository;
    private final PasswordEncoder passwordEncoder;
 
    @Transactional(readOnly = true)
    public List<PerfilAgenciaResponseDTO> listar() {
        return perfilAgenciaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
 
    @Transactional(readOnly = true)
    public PerfilAgenciaResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadPorId(id));
    }
 
    @Transactional
    public PerfilAgenciaResponseDTO crear(PerfilAgenciaRequestDTO dto) {
        usuarioValidator.validarEmailDisponible(dto.email());

        Agencia agencia = entityValidator.findByIdOrThrow(agenciaRepository,dto.agenciaId(),"Agencia");
 
        if (perfilAgenciaRepository.existsByAgenciaId(agencia.getId())) {
            throw new DuplicateResourceException("La agencia " + agencia.getId() + " ya tiene un perfil asociado");
        }
 
        String hash = passwordEncoder.encode(dto.password());
        PerfilAgencia perfil = new PerfilAgencia(dto.nombre(), dto.apellido(), dto.email(), hash, agencia);
        return toResponseDTO(perfilAgenciaRepository.save(perfil));
    }
 
    private PerfilAgencia buscarEntidadPorId(Long id) {
        return entityValidator.findByIdOrThrow(perfilAgenciaRepository,id,"Perfil de agencia");
    }
 
    private PerfilAgenciaResponseDTO toResponseDTO(PerfilAgencia perfil) {
        return new PerfilAgenciaResponseDTO(perfil.getId(), perfil.getNombre(), perfil.getApellido(),
                perfil.getEmail(), perfil.getAgencia().getId(),
                perfil.getAgencia().getRazonSocial());
    }
}
