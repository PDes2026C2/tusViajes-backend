package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.controller.dto.request.PaqueteRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PaqueteResponseDTO;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.Paquete;
import ar.edu.unq.tusViajes.repository.PaqueteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaqueteService {

    private final PaqueteRepository paqueteRepository;
    private final HotelService hotelService;
    private final AgenciaService agenciaService;

    @Transactional(readOnly = true)
    public List<PaqueteResponseDTO> listar() {
        return paqueteRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaqueteResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadPorId(id));
    }

    @Transactional
    public PaqueteResponseDTO crear(PaqueteRequestDTO dto) {

        Hotel hotel = hotelService.buscarEntidadPorId(dto.getHotelId());

        Agencia agencia = agenciaService.buscarEntidadPorId(dto.getAgenciaId());

        Paquete paquete = new Paquete(dto.getNombre(), dto.getDescripcion(), dto.getPrecio(), dto.getFechaInicio(),
                dto.getFechaFin(), hotel, agencia);
        return toResponseDTO(paqueteRepository.save(paquete));
    }

    @Transactional
    public PaqueteResponseDTO actualizar(Long id, PaqueteRequestDTO dto) {

        Paquete paquete = buscarEntidadPorId(id);

        Hotel hotel = hotelService.buscarEntidadPorId(dto.getHotelId());

        Agencia agencia = agenciaService.buscarEntidadPorId(dto.getAgenciaId());

        paquete.actualizarDatos(
                dto.getNombre(),
                dto.getDescripcion(),
                dto.getPrecio(),
                dto.getFechaInicio(),
                dto.getFechaFin(),
                hotel,
                agencia
        );

        return toResponseDTO(paqueteRepository.save(paquete));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!paqueteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paquete con id " + id + " no encontrado");
        }
        paqueteRepository.deleteById(id);
    }

    private Paquete buscarEntidadPorId(Long id) {
        return paqueteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete con id " + id + " no encontrado"));
    }

    private PaqueteResponseDTO toResponseDTO(Paquete paquete) {
        return new PaqueteResponseDTO(paquete.getId(), paquete.getNombre(), paquete.getDescripcion(),
                paquete.getPrecio(), paquete.getFechaInicio(), paquete.getFechaFin(), hotelService.toResponseDTO(paquete.getHotel()),
                agenciaService.toResponseDTO(paquete.getAgencia()));
    }
}
