package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Vet;

import java.util.Collection;

public interface VetService {
    Vet findVetById(int id);

    Collection<Vet> findAllVets();

    void saveVet(Vet vet);

    void deleteVet(Vet vet);
}
