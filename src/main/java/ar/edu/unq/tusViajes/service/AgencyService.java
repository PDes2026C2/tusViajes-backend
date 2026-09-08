package ar.edu.unq.tusViajes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.UpdateAgencyRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AgencyResponseDTO;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;
import ar.edu.unq.tusViajes.repository.AgencyRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgencyService {

    private final EntityValidator entityValidator;
    private final AgencyRepository agencyRepository;

    @Transactional(readOnly = true)
    public List<AgencyResponseDTO> getAll() {
        return agencyRepository.findAll().stream()
                .map(AgencyResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgencyResponseDTO> getPending() {
        return agencyRepository.findByStatus(AgencyStatus.PENDING).stream()
                .map(AgencyResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AgencyResponseDTO getById(Long id) {
        return AgencyResponseDTO.from(getEntityById(id));
    }

    @Transactional
    public AgencyResponseDTO update(Long id, UpdateAgencyRequestDTO dto) {
        Agency agency = getEntityById(id);
        agency.updateBusinessName(dto.businessName());
        return AgencyResponseDTO.from(agencyRepository.save(agency));
    }

    @Transactional
    public AgencyResponseDTO authorize(Long id) {
        Agency agency = getEntityById(id);
        agency.authorize();
        return AgencyResponseDTO.from(agencyRepository.save(agency));
    }

    @Transactional
    public AgencyResponseDTO reject(Long id) {
        Agency agency = getEntityById(id);
        agency.reject();
        return AgencyResponseDTO.from(agencyRepository.save(agency));
    }

    @Transactional
    public void delete(Long id) {
        agencyRepository.deleteById(id);
    }

    public Agency getEntityById(Long id) {
        return entityValidator.findByIdOrThrow(agencyRepository, id, "Agency");
    }
}
