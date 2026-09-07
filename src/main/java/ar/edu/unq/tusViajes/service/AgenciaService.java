package ar.edu.unq.tusViajes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.UpdateAgenciaRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AgenciaResponseDTO;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.EstadoAgencia;
import ar.edu.unq.tusViajes.repository.AgenciaRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgenciaService {

    private final EntityValidator entityValidator;
    private final AgenciaRepository agenciaRepository;

    @Transactional(readOnly = true)
    public List<AgenciaResponseDTO> listar() {
        return agenciaRepository.findAll().stream()
                .map(AgenciaResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgenciaResponseDTO> listarPendientes() {
        return agenciaRepository.findByEstado(EstadoAgencia.PENDIENTE).stream()
                .map(AgenciaResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AgenciaResponseDTO buscarPorId(Long id) {
        return AgenciaResponseDTO.from(buscarEntidadPorId(id));
    }

    @Transactional
    public AgenciaResponseDTO actualizar(Long id, UpdateAgenciaRequestDTO dto) {
        Agencia agencia = buscarEntidadPorId(id);
        agencia.actualizarRazonSocial(dto.razonSocial());
        return AgenciaResponseDTO.from(agenciaRepository.save(agencia));
    }

    @Transactional
    public AgenciaResponseDTO autorizar(Long id) {
        Agencia agencia = buscarEntidadPorId(id);
        agencia.autorizar();
        return AgenciaResponseDTO.from(agenciaRepository.save(agencia));
    }

    @Transactional
    public AgenciaResponseDTO rechazar(Long id) {
        Agencia agencia = buscarEntidadPorId(id);
        agencia.rechazar();
        return AgenciaResponseDTO.from(agenciaRepository.save(agencia));
    }

    @Transactional
    public void eliminar(Long id) {
        agenciaRepository.deleteById(id);
    }

    public Agencia buscarEntidadPorId(Long id) {
        return entityValidator.findByIdOrThrow(agenciaRepository, id, "Agencia");
    }
}
