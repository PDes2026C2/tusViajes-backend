package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TravelPackageService {

    private final TravelPackageRepository travelPackageRepository;
    private final HotelService hotelService;
    private final AgencyService agencyService;

    @Transactional(readOnly = true)
    public Page<TravelPackageResponseDTO> search(Pageable pageable) {
        return travelPackageRepository.findAll(pageable).map(TravelPackageResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public TravelPackageResponseDTO getById(Long id) {
        return TravelPackageResponseDTO.from(getEntityById(id));
    }

    @Transactional
    public TravelPackageResponseDTO create(TravelPackageRequestDTO dto) {
        Hotel hotel = hotelService.getEntityById(dto.getHotelId());
        Agency agency = agencyService.getEntityById(dto.getAgencyId());

        TravelPackage travelPackage = new TravelPackage(
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getStartDate(),
                dto.getEndDate(),
                hotel,
                agency
        );
        return TravelPackageResponseDTO.from(travelPackageRepository.save(travelPackage));
    }

    @Transactional
    public TravelPackageResponseDTO update(Long id, TravelPackageRequestDTO dto) {
        TravelPackage travelPackage = getEntityById(id);
        Hotel hotel = hotelService.getEntityById(dto.getHotelId());
        Agency agency = agencyService.getEntityById(dto.getAgencyId());

        travelPackage.updateData(
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getStartDate(),
                dto.getEndDate(),
                hotel,
                agency
        );

        return TravelPackageResponseDTO.from(travelPackageRepository.save(travelPackage));
    }

    @Transactional
    public void delete(Long id) {
        if (!travelPackageRepository.existsById(id)) {
            throw new ResourceNotFoundException("TravelPackage with id " + id + " not found");
        }
        travelPackageRepository.deleteById(id);
    }

    public TravelPackage getEntityById(Long id) {
        return travelPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TravelPackage with id " + id + " not found"));
    }
}
