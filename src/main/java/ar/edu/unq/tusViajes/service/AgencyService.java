package ar.edu.unq.tusViajes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.unq.tusViajes.controller.dto.request.UpdateAgencyRequestDTO;
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
    public List<Agency> getAll() {
        return agencyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Agency> getPending() {
        return agencyRepository.findByStatus(AgencyStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public Agency getById(Long id) {
        return getEntityById(id);
    }

    @Transactional
    public Agency update(Long id, UpdateAgencyRequestDTO dto) {
        Agency agency = getEntityById(id);
        agency.updateBusinessName(dto.businessName());
        return agencyRepository.save(agency);
    }

    @Transactional
    public Agency authorize(Long id) {
        Agency agency = getEntityById(id);
        agency.authorize();
        return agencyRepository.save(agency);
    }

    @Transactional
    public Agency reject(Long id) {
        Agency agency = getEntityById(id);
        agency.reject();
        return agencyRepository.save(agency);
    }

    @Transactional
    public void delete(Long id) {
        agencyRepository.deleteById(id);
    }

    public Agency getEntityById(Long id) {
        return entityValidator.findByIdOrThrow(agencyRepository, id, "Agency");
    }
}
