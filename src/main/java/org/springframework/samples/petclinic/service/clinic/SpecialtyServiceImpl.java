package org.springframework.samples.petclinic.service.clinic;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.repository.SpecialtyRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;

@Service
@Transactional
public class SpecialtyServiceImpl implements SpecialtyService {
    private final SpecialtyRepository specialtyRepository;

    public SpecialtyServiceImpl(SpecialtyRepository specialtyRepository) {
        this.specialtyRepository = specialtyRepository;
    }




    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Specialty findSpecialtyById(int specialtyId) {
        Specialty specialty = null;
        try {
            specialty = specialtyRepository.findById(specialtyId);
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            // just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
        return specialty;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Collection<Specialty> findAllSpecialties() {
        return specialtyRepository.findAll();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void saveSpecialty(Specialty specialty) {
        specialtyRepository.save(specialty);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteSpecialty(Specialty specialty) {
        specialtyRepository.delete(specialty);
    }

}
