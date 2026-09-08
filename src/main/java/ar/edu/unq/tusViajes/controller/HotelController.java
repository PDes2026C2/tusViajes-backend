package ar.edu.unq.tusViajes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.HotelRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.HotelResponseDTO;
import ar.edu.unq.tusViajes.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public ResponseEntity<List<HotelResponseDTO>> getAll() {
        return ResponseEntity.ok(hotelService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getById(id));
    }

    @PostMapping
    public ResponseEntity<HotelResponseDTO> create(@Valid @RequestBody HotelRequestDTO dto) {
        HotelResponseDTO created = hotelService.create(dto);
        return ResponseEntity.created(URI.create("/api/hotels/" + created.id())).body(created);
    }
}
