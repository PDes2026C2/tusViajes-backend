package ar.edu.unq.tusViajes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.BuyerRegistrationRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.BuyerResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.model.Buyer;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.BuyerRepository;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import ar.edu.unq.tusViajes.validator.UserValidator;

@Service
public class BuyerService {

    private final EntityValidator entityValidator;
    private final UserValidator userValidator;
    private final BuyerRepository buyerRepository;
    private final PasswordEncoder passwordEncoder;
    private final TravelPackageRepository travelPackageRepository;
    private final TravelPackageService travelPackageService;

    public BuyerService(EntityValidator entityValidator,
                        UserValidator userValidator,
                        BuyerRepository buyerRepository,
                        PasswordEncoder passwordEncoder,
                        TravelPackageRepository travelPackageRepository,
                        @Lazy TravelPackageService travelPackageService) {
        this.entityValidator = entityValidator;
        this.userValidator = userValidator;
        this.buyerRepository = buyerRepository;
        this.passwordEncoder = passwordEncoder;
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
    public BuyerResponseDTO register(BuyerRegistrationRequestDTO dto) {
        userValidator.validateEmailAvailable(dto.email());
        String hash = passwordEncoder.encode(dto.password());
        Buyer buyer = new Buyer(dto.firstName(), dto.lastName(), dto.email(), hash,
                dto.phoneNumber(), dto.nationalId());
        return BuyerResponseDTO.from(buyerRepository.save(buyer));
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
