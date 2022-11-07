package org.springframework.samples.petclinic.service.clinic;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;

@Service
@Transactional
public class OwnerServiceImpl implements OwnerService {
    private final OwnerRepository ownerRepository;

    public OwnerServiceImpl(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Collection<Owner> findAllOwners() {
        return ownerRepository.findAll();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteOwner(Owner owner) {
        ownerRepository.delete(owner);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Owner findOwnerById(int id) {
        Owner owner = null;
        try {
            owner = ownerRepository.findById(id);
        } catch (ObjectRetrievalFailureException | EmptyResultDataAccessException e) {
            // just ignore not found exceptions for Jdbc/Jpa realization
            return null;
        }
        return owner;
    }


    @Override
    @org.springframework.transaction.annotation.Transactional
    public void saveOwner(Owner owner) {
        ownerRepository.save(owner);

    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Collection<Owner> findOwnerByLastName(String lastName) {
        return ownerRepository.findByLastName(lastName);
    }


}
