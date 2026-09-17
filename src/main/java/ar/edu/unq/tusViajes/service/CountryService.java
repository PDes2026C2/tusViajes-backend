package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.dto.CountryDTO;
import ar.edu.unq.tusViajes.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    @Transactional(readOnly = true)
    public List<CountryDTO> getCountries() {
        return getAll();
    }

    @Transactional(readOnly = true)
    public List<CountryDTO> getAll() {
        return countryRepository.findAll().stream()
                .map(CountryDTO::from)
                .toList();
    }
}
