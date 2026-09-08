package ar.edu.unq.tusViajes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.HotelRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.HotelResponseDTO;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class HotelService {

    private final EntityValidator entityValidator;
    private final HotelRepository hotelRepository;

    @Transactional(readOnly = true)
    public List<HotelResponseDTO> listar() {
        return hotelRepository.findAll().stream()
                .map(HotelResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HotelResponseDTO buscarPorId(Long id) {
        return HotelResponseDTO.from(buscarEntidadPorId(id));
    }

    @Transactional
    public HotelResponseDTO crear(HotelRequestDTO dto) {
        Hotel hotel = new Hotel(dto.nombre(), dto.destino(), dto.fotoUrl(), dto.servicio());
        return HotelResponseDTO.from(hotelRepository.save(hotel));
    }

    public Hotel buscarEntidadPorId(Long id) {
        return entityValidator.findByIdOrThrow(hotelRepository, id, "Hotel");
    }
}
