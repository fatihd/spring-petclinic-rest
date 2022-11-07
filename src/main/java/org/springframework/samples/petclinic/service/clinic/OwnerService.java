package org.springframework.samples.petclinic.service.clinic;

import org.springframework.samples.petclinic.model.Owner;

import java.util.Collection;

public interface OwnerService {

    Owner findOwnerById(int id);

    Collection<Owner> findAllOwners();

    void saveOwner(Owner owner);

    void deleteOwner(Owner owner);

    Collection<Owner> findOwnerByLastName(String lastName);
}
