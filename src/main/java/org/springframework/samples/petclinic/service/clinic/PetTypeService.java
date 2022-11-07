package org.springframework.samples.petclinic.service.clinic;

import org.springframework.samples.petclinic.model.PetType;

import java.util.Collection;

public interface PetTypeService {
    PetType findPetTypeById(int petTypeId);
    Collection<PetType> findAllPetTypes();
    Collection<PetType> findPetTypes();
    void savePetType(PetType petType);
    void deletePetType(PetType petType);
}
