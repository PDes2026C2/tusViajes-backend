package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.adapters.dto.CountryDTO;
import ar.edu.unq.tusViajes.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/countries"})
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public ResponseEntity<List<CountryDTO>> getCountries() {
        return ResponseEntity.ok(countryService.getCountries());
    }
}
