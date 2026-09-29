package ar.edu.unq.tusViajes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.HotelRequestDTO;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.repository.CityRepository;
import ar.edu.unq.tusViajes.repository.HotelRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final EntityValidator entityValidator;
    private final HotelRepository hotelRepository;
    private final CityRepository cityRepository;

    @Transactional(readOnly = true)
    public List<Hotel> getAll() {
        return hotelRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Hotel getById(Long id) {
        return getEntityById(id);
    }

    @Transactional
    public Hotel create(HotelRequestDTO dto) {
        City city = entityValidator.findByIdOrThrow(cityRepository, dto.cityId(), "City");
        Hotel hotel = new Hotel(dto.name(), city, dto.photoUrl(), dto.services());
        return hotelRepository.save(hotel);
    }

    public Hotel getEntityById(Long id) {
        return entityValidator.findByIdOrThrow(hotelRepository, id, "Hotel");
    }
}
