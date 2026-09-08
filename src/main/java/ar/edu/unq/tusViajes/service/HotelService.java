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
    public List<HotelResponseDTO> getAll() {
        return hotelRepository.findAll().stream()
                .map(HotelResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HotelResponseDTO getById(Long id) {
        return HotelResponseDTO.from(getEntityById(id));
    }

    @Transactional
    public HotelResponseDTO create(HotelRequestDTO dto) {
        Hotel hotel = new Hotel(dto.name(), dto.destination(), dto.photoUrl(), dto.services());
        return HotelResponseDTO.from(hotelRepository.save(hotel));
    }

    public Hotel getEntityById(Long id) {
        return entityValidator.findByIdOrThrow(hotelRepository, id, "Hotel");
    }
}
