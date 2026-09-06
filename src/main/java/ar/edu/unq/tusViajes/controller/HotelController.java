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
@RequestMapping("/api/hoteles")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public List<HotelResponseDTO> listar() {
        return hotelService.listar();
    }

    @GetMapping("/{id}")
    public HotelResponseDTO buscarPorId(@PathVariable Long id) {
        return hotelService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<HotelResponseDTO> crear(@Valid @RequestBody HotelRequestDTO dto) {
        HotelResponseDTO creado = hotelService.crear(dto);
        return ResponseEntity.created(URI.create("/api/hoteles/" + creado.id())).body(creado);
    }

}
