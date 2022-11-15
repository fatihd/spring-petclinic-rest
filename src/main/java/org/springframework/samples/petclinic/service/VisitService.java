package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Visit;

import java.util.Collection;

public interface VisitService {
    Collection<Visit> findVisitsByPetId(int petId);

    Visit findVisitById(int visitId);

    Collection<Visit> findAllVisits();

    void saveVisit(Visit visit);

    void deleteVisit(Visit visit);
}
