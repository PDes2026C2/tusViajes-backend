package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Hotel;

public record HotelResponseDTO(Long id, String name, String destination, String photoUrl, String services) {

    public static HotelResponseDTO from(Hotel hotel) {
        if (hotel == null) {
            return null;
        }
        return new HotelResponseDTO(
                hotel.getId(),
                hotel.getName(),
                hotel.getDestination(),
                hotel.getPhotoUrl(),
                hotel.getServices()
        );
    }
}
