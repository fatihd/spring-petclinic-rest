package org.springframework.samples.petclinic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class VetServiceImpl implements VetService {
    private final VetRepository vetRepository;

    @Override
    @Transactional(readOnly = true)
    public Vet findVetById(int id) {
        Vet vet = null;
        try {
            vet = vetRepository.findById(id);
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            // just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
        return vet;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<Vet> findAllVets() {
        return vetRepository.findAll();
    }

    @Override
    @Transactional
    public void saveVet(Vet vet) {
        vetRepository.save(vet);
    }

    @Override
    @Transactional
    public void deleteVet(Vet vet) {
        vetRepository.delete(vet);
    }
}
