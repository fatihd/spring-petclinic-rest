package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Pet;

import java.util.Collection;

public interface PetService {
    Pet findPetById(int id);

    Collection<Pet> findAllPets();

    void savePet(Pet pet);

    void deletePet(Pet pet);
}
