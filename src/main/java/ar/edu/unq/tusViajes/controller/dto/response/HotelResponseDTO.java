package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Hotel;

public record HotelResponseDTO(Long id, String nombre, String destino, String fotoUrl, String servicio) {

    public static HotelResponseDTO from(Hotel hotel) {
        if (hotel == null) {
            return null;
        }
        return new HotelResponseDTO(
                hotel.getId(),
                hotel.getNombre(),
                hotel.getDestino(),
                hotel.getFotoUrl(),
                hotel.getServicio()
        );
    }
}
