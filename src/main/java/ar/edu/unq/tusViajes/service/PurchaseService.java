package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.dto.PassengerDTO;
import ar.edu.unq.tusViajes.exception.DuplicateResourceException;
import ar.edu.unq.tusViajes.exception.FlightFullException;
import ar.edu.unq.tusViajes.exception.PackageAlreadyStartedException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

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
    public Purchase purchase(Long buyerId, Long travelPackageId) {
        Buyer buyer = entityValidator.findByIdOrThrow(buyerRepository, buyerId, "Comprador");
        TravelPackage travelPackage = travelPackageRepository.findById(travelPackageId)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete de viaje con id " + travelPackageId + " no encontrado"));

        PassengerDTO passenger = toPassengerDTO(buyer);
        logger.info("Processing purchase for buyer {} and travelPackage {}", buyerId, travelPackageId);

        Long departureFlightId = travelPackage.getDepartureFlight().getId();
        Long returnFlightId = travelPackage.getReturnFlight().getId();

        boolean departureSold = false;
        boolean returnSold = false;

        try {
            sellFlight(departureFlightId, passenger);
            departureSold = true;

            sellFlight(returnFlightId, passenger);
            returnSold = true;

            Purchase purchase = buyer.buy(travelPackage);
            Purchase saved = purchaseRepository.save(purchase);
            logger.info("Purchase {} created for buyer {} with price {}", saved.getId(), buyerId, saved.getPrice());
            return saved;
        } catch (Exception ex) {
            logger.warn("Purchase failed for buyer {} and travelPackage {}. Initiating flight compensation.", buyerId, travelPackageId, ex);
            if (returnSold) {
                compensateFlight(returnFlightId, passenger);
            }
            if (departureSold) {
                compensateFlight(departureFlightId, passenger);
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public Page<Purchase> getPurchasesByBuyer(Long buyerId, Pageable pageable) {
        return purchaseRepository.findByBuyerId(buyerId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Purchase> getSalesByAgency(Long agencyId, Pageable pageable) {
        return purchaseRepository.findByTravelPackageAgencyId(agencyId, pageable);
    }

    private void sellFlight(Long flightId, PassengerDTO passenger) {
        try {
            flightsApiService.sellFlight(flightId, passenger);
            logger.info("Flight {} sold for passenger DNI {}", flightId, passenger.dni());
        } catch (FlightFullException e) {
            throw e;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 409) {
                logger.warn("Flight {} is full (409) for passenger DNI {}", flightId, passenger.dni());
                throw new FlightFullException("Uno de los vuelos asociados no cuenta con cupo disponible");
            }
            throw e;
        }
    }

    private void compensateFlight(Long flightId, PassengerDTO passenger) {
        try {
            flightsApiService.cancelFlight(flightId, passenger);
            logger.info("Compensated flight {} for passenger DNI {}", flightId, passenger.dni());
        } catch (Exception cancelEx) {
            logger.error("Failed to compensate flight {} for passenger DNI {}", flightId, passenger.dni(), cancelEx);
        }
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
