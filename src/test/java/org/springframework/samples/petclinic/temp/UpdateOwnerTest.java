package org.springframework.samples.petclinic.temp;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.OwnerMapperImpl;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.controller.OwnerRestController;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.service.OwnerService;
import org.springframework.samples.petclinic.service.OwnerServiceImpl;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UpdateOwnerTest {


    OwnerRepository ownerRepository = new FakeOwnerRepository();

    OwnerService ownerService = new OwnerServiceImpl(ownerRepository);

    OwnerMapper ownerMapper = new OwnerMapperImpl();

    OwnerRestController controller = new OwnerRestController(
        ownerService, null, null,
        ownerMapper, null, null);

    @Test
    void testUpdateOwnerSuccess() {
        int ownerId = 1;

        Owner owner = new Owner();
        owner.setId(ownerId);
        ownerRepository.save(owner);

        OwnerFieldsDto updateOwner = getUpdateOwner();

        ResponseEntity<OwnerDto> updateResponse = controller.updateOwner(ownerId, updateOwner);

        assertThat(updateResponse.getStatusCode().value(), equalTo(204));
        matches(ownerId, updateOwner, requireNonNull(updateResponse.getBody()));

        matches(ownerId, updateOwner, ownerRepository.findById(ownerId));

        ResponseEntity<OwnerDto> getResponse = controller.getOwner(ownerId);

        assertThat(getResponse.getStatusCode().value(), equalTo(200));
        matches(ownerId, updateOwner, requireNonNull(getResponse.getBody()));
    }

    private static void matches(int ownerId, OwnerFieldsDto ownerFieldsDto, Owner savedOwner) {
        assertThat(savedOwner.getId(), equalTo(ownerId));
        assertThat(savedOwner.getFirstName(), equalTo(ownerFieldsDto.getFirstName()));
        assertThat(savedOwner.getLastName(), equalTo(ownerFieldsDto.getLastName()));
        assertThat(savedOwner.getAddress(), equalTo(ownerFieldsDto.getAddress()));
        assertThat(savedOwner.getCity(), equalTo(ownerFieldsDto.getCity()));
        assertThat(savedOwner.getTelephone(), equalTo(ownerFieldsDto.getTelephone()));
    }

    private static void matches(int ownerId, OwnerFieldsDto ownerFieldsDto, OwnerDto responseBody) {
        assertThat(responseBody.getId(), equalTo(ownerId));
        assertThat(responseBody.getFirstName(), equalTo(ownerFieldsDto.getFirstName()));
        assertThat(responseBody.getLastName(), equalTo(ownerFieldsDto.getLastName()));
        assertThat(responseBody.getAddress(), equalTo(ownerFieldsDto.getAddress()));
        assertThat(responseBody.getCity(), equalTo(ownerFieldsDto.getCity()));
        assertThat(responseBody.getTelephone(), equalTo(ownerFieldsDto.getTelephone()));
    }

    private static OwnerFieldsDto getUpdateOwner() {
        OwnerFieldsDto updateOwner = new OwnerFieldsDto();
        // body.id = ownerId which is used in url path
//        updateOwner.setId(ownerId);
        updateOwner.setFirstName("GeorgeI");
        updateOwner.setLastName("Franklin");
        updateOwner.setAddress("110 W. Liberty St.");
        updateOwner.setCity("Madison");
        updateOwner.setTelephone("6085551023");
        return updateOwner;
    }

    private static class OwnerRepositoryAdapter implements OwnerRepository {

        public Collection<Owner> findByLastName(String lastName) {
            throw new UnsupportedOperationException();
        }

        public Owner findById(int id) {
            throw new UnsupportedOperationException();
        }

        public void save(Owner owner) {
            throw new UnsupportedOperationException();
        }

        public Collection<Owner> findAll() {
            throw new UnsupportedOperationException();
        }

        public void delete(Owner owner) {
            throw new UnsupportedOperationException();
        }
    }

    private static class FakeOwnerRepository extends OwnerRepositoryAdapter {
        private int lastId = 0;

        private final Map<Integer, Owner> map = new HashMap<>();

        public Owner findById(int id) {
            return map.get(id);
        }

        @Override
        public void save(Owner owner) {
            if (owner.isNew()) {
                owner.setId(++lastId);
            }

            map.put(owner.getId(), owner);
        }
    }
}
