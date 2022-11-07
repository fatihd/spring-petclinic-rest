package org.springframework.samples.petclinic.service.clinic;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.PetTypeRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;

@Service
@Transactional
public class PetTypeServiceImpl implements PetTypeService {
    private final PetTypeRepository petTypeRepository;

    // FIXME
    private final PetRepository petRepository;

    public PetTypeServiceImpl(PetTypeRepository petTypeRepository, PetRepository petRepository) {
        this.petTypeRepository = petTypeRepository;
        this.petRepository = petRepository;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public PetType findPetTypeById(int petTypeId) {
        PetType petType = null;
        try {
            petType = petTypeRepository.findById(petTypeId);
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            // just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
        return petType;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Collection<PetType> findAllPetTypes() {
        return petTypeRepository.findAll();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void savePetType(PetType petType) {
        petTypeRepository.save(petType);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deletePetType(PetType petType) {
        petTypeRepository.delete(petType);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Collection<PetType> findPetTypes() {
        return petRepository.findPetTypes();
    }
}
