package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PurchaseResponseDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.Purchase;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.BuyerRepository;
import ar.edu.unq.tusViajes.repository.PurchaseRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    private final PurchaseRepository purchaseRepository;
    private final BuyerRepository buyerRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final FlightsApiService flightsApiService;
    private final EntityValidator entityValidator;

    public PurchaseService(PurchaseRepository purchaseRepository,
                           BuyerRepository buyerRepository,
                           TravelPackageRepository travelPackageRepository,
                           FlightsApiService flightsApiService,
                           EntityValidator entityValidator) {
        this.purchaseRepository = purchaseRepository;
        this.buyerRepository = buyerRepository;
        this.travelPackageRepository = travelPackageRepository;
        this.flightsApiService = flightsApiService;
        this.entityValidator = entityValidator;
    }

    @Transactional
    public PurchaseResponseDTO purchase(Long buyerId, Long travelPackageId) {
        Buyer buyer = entityValidator.findByIdOrThrow(buyerRepository, buyerId, "Comprador");
        TravelPackage travelPackage = travelPackageRepository.findById(travelPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete de viaje con id " + travelPackageId + " no encontrado"));

        if (travelPackage.getPrice() == null) {
            throw new IllegalArgumentException("El precio del paquete de viaje no puede ser nulo");
        }
        if (travelPackage.hasEnded()) {
            throw new IllegalArgumentException("No se puede comprar un paquete de viaje que ya ha finalizado");
        }
        if (buyer.hasAcquired(travelPackage)) {
            throw new DuplicateResourceException("El comprador ya adquirió este paquete de viaje");
        }

        PassengerDTO passenger = toPassengerDTO(buyer);
        logger.info("Processing purchase for buyer {} and travelPackage {}", buyerId, travelPackageId);

        flightsApiService.sellFlight(travelPackage.getDepartureFlight().getId(), passenger);
        logger.info("Departure flight {} sold for buyer {}", travelPackage.getDepartureFlight().getId(), buyerId);
        flightsApiService.sellFlight(travelPackage.getReturnFlight().getId(), passenger);
        logger.info("Return flight {} sold for buyer {}", travelPackage.getReturnFlight().getId(), buyerId);

        Purchase purchase = buyer.buy(travelPackage);
        Purchase saved = purchaseRepository.save(purchase);
        logger.info("Purchase {} created for buyer {} with price {}", saved.getId(), buyerId, saved.getPrice());
        return PurchaseResponseDTO.from(saved);
    }

    private PassengerDTO toPassengerDTO(Buyer buyer) {
        int dni;
        try {
            dni = Integer.parseInt(buyer.getNationalId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El DNI del comprador debe ser numérico: " + buyer.getNationalId());
        }
        return new PassengerDTO(dni, buyer.getFirstName(), buyer.getLastName());
    }
}
