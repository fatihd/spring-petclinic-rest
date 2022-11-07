package org.springframework.samples.petclinic.service.clinic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class PetTypeServiceTests {
    @Autowired
    PetTypeService petTypeService;

    @Test
    @Transactional
    void shouldDeletePetType(){
        PetType petType = this.petTypeService.findPetTypeById(1);
        this.petTypeService.deletePetType(petType);
        try {
            petType = this.petTypeService.findPetTypeById(1);
        } catch (Exception e) {
            petType = null;
        }
        assertThat(petType).isNull();
    }
    @Test
    void shouldFindPetTypeById(){
        PetType petType = this.petTypeService.findPetTypeById(1);
        assertThat(petType.getName()).isEqualTo("cat");
    }

    @Test
    void shouldFindAllPetTypes(){
        Collection<PetType> petTypes = this.petTypeService.findAllPetTypes();
        PetType petType1 = EntityUtils.getById(petTypes, PetType.class, 1);
        assertThat(petType1.getName()).isEqualTo("cat");
        PetType petType3 = EntityUtils.getById(petTypes, PetType.class, 3);
        assertThat(petType3.getName()).isEqualTo("lizard");
    }

    @Test
    @Transactional
    void shouldInsertPetType() {
        Collection<PetType> petTypes = this.petTypeService.findAllPetTypes();
        int found = petTypes.size();

        PetType petType = new PetType();
        petType.setName("tiger");

        this.petTypeService.savePetType(petType);
        assertThat(petType.getId().longValue()).isNotEqualTo(0);

        petTypes = this.petTypeService.findAllPetTypes();
        assertThat(petTypes.size()).isEqualTo(found + 1);
    }

    @Test
    @Transactional
    void shouldUpdatePetType(){
        PetType petType = this.petTypeService.findPetTypeById(1);
        String oldLastName = petType.getName();
        String newLastName = oldLastName + "X";
        petType.setName(newLastName);
        this.petTypeService.savePetType(petType);
        petType = this.petTypeService.findPetTypeById(1);
        assertThat(petType.getName()).isEqualTo(newLastName);
    }
}
