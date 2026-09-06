package ar.edu.unq.tusViajes.service;

import java.util.List;
import java.util.stream.Collectors;
 
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.PerfilAdminRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PerfilAdminResponseDTO;
import ar.edu.unq.tusViajes.model.PerfilAdmin;
import ar.edu.unq.tusViajes.repository.PerfilAdminRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import ar.edu.unq.tusViajes.validator.UsuarioValidator;
import lombok.RequiredArgsConstructor;
 
@Service
@RequiredArgsConstructor
public class PerfilAdminService {
    
    private final EntityValidator entityValidator;
    private final UsuarioValidator usuarioValidator;
    private final PerfilAdminRepository perfilAdministradorRepository;
    private final PasswordEncoder passwordEncoder;
 
    @Transactional(readOnly = true)
    public List<PerfilAdminResponseDTO> listar() {
        return perfilAdministradorRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
 
    @Transactional(readOnly = true)
    public PerfilAdminResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadPorId(id));
    }
 
    @Transactional
    public PerfilAdminResponseDTO crear(PerfilAdminRequestDTO dto) {
        usuarioValidator.validarEmailDisponible(dto.email());
        String hash = passwordEncoder.encode(dto.password());
        PerfilAdmin perfil = new PerfilAdmin(dto.nombre(), dto.apellido(), dto.email(), hash);
        return toResponseDTO(perfilAdministradorRepository.save(perfil));
    }
 
    private PerfilAdmin buscarEntidadPorId(Long id) {
        return entityValidator.findByIdOrThrow(perfilAdministradorRepository,id,"Perfil de administrador");
    }
 
    private PerfilAdminResponseDTO toResponseDTO(PerfilAdmin perfil) {
        return new PerfilAdminResponseDTO(perfil.getId(), perfil.getNombre(), perfil.getApellido(),
                perfil.getEmail());
    }
}