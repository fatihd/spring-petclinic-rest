package org.springframework.samples.petclinic.service.clinic;

import org.springframework.samples.petclinic.model.Specialty;

import java.util.Collection;

public interface SpecialtyService {
    Specialty findSpecialtyById(int specialtyId);
    Collection<Specialty> findAllSpecialties();
    void saveSpecialty(Specialty specialty);
    void deleteSpecialty(Specialty specialty);

}
