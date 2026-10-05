package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.BuyersTop;

public record BuyersTopResponseDTO(BuyerResponseDTO buyer, Long purchaseCount) {

    public static BuyersTopResponseDTO from(BuyersTop buyersTop) {
        return new BuyersTopResponseDTO(BuyerResponseDTO.from(buyersTop.buyer()), buyersTop.purchaseCount());
    }
}
