package ar.edu.unq.tusViajes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.response.BuyerResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.BuyerRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;

@Service
public class BuyerService {

    private final EntityValidator entityValidator;
    private final BuyerRepository buyerRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final TravelPackageService travelPackageService;

    public BuyerService(EntityValidator entityValidator,
                        BuyerRepository buyerRepository,
                        TravelPackageRepository travelPackageRepository,
                        @Lazy TravelPackageService travelPackageService) {
        this.entityValidator = entityValidator;
        this.buyerRepository = buyerRepository;
        this.travelPackageRepository = travelPackageRepository;
        this.travelPackageService = travelPackageService;
    }

    @Transactional(readOnly = true)
    public List<BuyerResponseDTO> getAll() {
        return buyerRepository.findAll().stream()
                .map(BuyerResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BuyerResponseDTO getById(Long id) {
        return BuyerResponseDTO.from(getEntityById(id));
    }

    @Transactional
    public void addFavorite(Long buyerId, Long travelPackageId) {
        Buyer buyer = getEntityById(buyerId);
        TravelPackage travelPackage = getTravelPackage(travelPackageId);
        buyer.addFavorite(travelPackage);
    }

    @Transactional
    public void removeFavorite(Long buyerId, Long travelPackageId) {
        Buyer buyer = getEntityById(buyerId);
        TravelPackage travelPackage = getTravelPackage(travelPackageId);
        buyer.removeFavorite(travelPackage);
    }

    @Transactional(readOnly = true)
    public List<TravelPackageResponseDTO> getFavorites(Long buyerId) {
        Buyer buyer = getEntityById(buyerId);
        return buyer.getFavoriteTravelPackages().stream()
                .map(travelPackage -> travelPackageService.getById(travelPackage.getId()))
                .collect(Collectors.toList());
    }

    public Buyer getEntityById(Long id) {
        return entityValidator.findByIdOrThrow(buyerRepository, id, "Buyer");
    }

    private TravelPackage getTravelPackage(Long id) {
        return entityValidator.findByIdOrThrow(travelPackageRepository, id, "TravelPackage");
    }
}
