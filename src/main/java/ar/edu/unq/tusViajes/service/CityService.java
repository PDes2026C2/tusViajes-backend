package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.dto.CityDTO;
import ar.edu.unq.tusViajes.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;

    @Transactional(readOnly = true)
    public List<CityDTO> getCities(String isoCode) {
        if (isoCode != null && !isoCode.isBlank()) {
            return getByCountryIsoCode(isoCode.trim());
        }
        return getAll();
    }

    @Transactional(readOnly = true)
    public List<CityDTO> getAll() {
        return cityRepository.findAll().stream()
                .map(CityDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CityDTO> getByCountryIsoCode(String isoCode) {
        return cityRepository.findByCountry_IsoCodeIgnoreCase(isoCode).stream()
                .map(CityDTO::from)
                .toList();
    }
}
