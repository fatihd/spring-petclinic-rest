## Moving logic to service layer

### Fat controllers

Compare the amount we do in the controller layer vs the service layer:

```java
// OwnerRestController
public ResponseEntity<OwnerDto> updateOwner(
            Integer ownerId, 
            OwnerFieldsDto ownerFieldsDto
        ) {
    Owner currentOwner = this.ownerService.findOwnerById(ownerId);
    if (currentOwner == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    currentOwner.setAddress(ownerFieldsDto.getAddress());
    currentOwner.setCity(ownerFieldsDto.getCity());
    currentOwner.setFirstName(ownerFieldsDto.getFirstName());
    currentOwner.setLastName(ownerFieldsDto.getLastName());
    currentOwner.setTelephone(ownerFieldsDto.getTelephone());
    this.ownerService.saveOwner(currentOwner);
    return new ResponseEntity<>(
            ownerMapper.toOwnerDto(currentOwner), 
            HttpStatus.NO_CONTENT);
    }
```

```java
// OwnerServiceImpl
public void saveOwner(Owner owner) {
    ownerRepository.save(owner);
}
```

This anti-pattern has the name fat controller.
It comes about in several ways, which are not our concern now.
We need to understand why it is bad though.
Fat controllers are bad for two main reasons:
- Controllers are coupled to the framework they are using.
- Many controllers may be invoking the same behavior of the system.

Any functionality left in the controller, instead of service or model layer,
may be duplicated or may need to be reimplemented.
Even before the project becomes feature complete some technical choices will be obsolete;
besides ui an integration API will need to be opened,
and some api design choices will need to be reconsidered 
such that incompatible new api versions will need to be rolled out,
a backend-for-frontend will need to be written for the fancy new ui technology,
and some tasks will need to be triggered by a scheduler,
and the list goes...

In addition, tests that verify the functionality left in the controllers
will be hard to write and slow to run.

Fat controllers were the norm some years ago and are still quite common in extant codebases,
so need to know how to fix it.

### Refactoring

Refactoring is defined as changing the structure of a solution 
without changing its observable behavior.
In order to make sure we are not actually changing the behavior,
we need to have the piece of code that is going to be changed under test.

#### How much test should there be?

We can do some trivial renaming etc. without tests or even automatic refactoring tools.
Some minor refactorings can be done by relying on the automatic refactoring tools, 
without tests too. 
Even then we need to make sure that we're not changing behavior 
that rely on reflection.  
More obscurely, even recompiling the source code
without changing it may produce different bytecode. 
This may change the behavior depending on the code generated, 
even if it doesn't change the behavior, the checksum of the bytecode could be different 
and may be refused to run by its users in specific use cases.
Of course these are extreme cases.
We need to have enough tests in place to reduce the risk 
to a significantly lower level than the benefit sought by the refactoring.

#### What to test?

If there are already tests in place, 
and we are confident that they do a good job of preventing regressions, 
we should look at them to determine the test cases.
Let's say a controller method has *a* test cases, and it calls two service methods 
each having *b* and *c* test cases respectively. We might need to write 
upto *a×(b+c)* test cases two provide as much coverage as existing tests.
In reality some of the *a* test cases for the controller will be 
for early return and exception paths, and will never invoke the service methods.
In other test cases the two methods will always be called together.

#### A closer look at the current methods

There are two paths through the code: one in which the owner is found, 
the other in which `ownerService.findOwnerById` returns `null`;
If you remember, or check the coverage report, 
the second case is not covered by the existing tests.

```java
public ResponseEntity<OwnerDto> updateOwner(
        Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
    Owner currentOwner = this.ownerService.findOwnerById(ownerId);
    if (currentOwner == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    currentOwner.setAddress(ownerFieldsDto.getAddress());
    currentOwner.setCity(ownerFieldsDto.getCity());
    currentOwner.setFirstName(ownerFieldsDto.getFirstName());
    currentOwner.setLastName(ownerFieldsDto.getLastName());
    currentOwner.setTelephone(ownerFieldsDto.getTelephone());
    
    this.ownerService.saveOwner(currentOwner);
    
    return new ResponseEntity<>(
            ownerMapper.toOwnerDto(currentOwner), 
            HttpStatus.NO_CONTENT);
}
```

The 3 existing test cases are `testUpdateOwnerSuccess`, 
`testUpdateOwnerSuccessNoBodyId`,
and `testUpdateOwnerError` in `OwnerRestControllerTests`.
In the first two cases the test sends and `OwnerDto` with and without 
and `ownerId` field filled. 
and asserts in both cases the result is successful.
Careful readers would recognize `updateOwner` method doesn't take an `OwnerDto`; 
it takes an `OwnerFieldsDto`, 
which doesn't have an `ownerId` field at all.
We can just assume for now, 
the endpoint just ignores unmapped fields in the body in general.
We won't change this configuration and we don't need to test for it.
`testUpdateOwnerError` similarly verifies 
that bean validation annotations are taken into account.
We won't change that configuration. In fact, 
`testUpdateOwnerError` shouldn't break by this refactoring.
We may need to change, or rewrite, the other two cases with 
what they say about the configuration in mind.

Although not our concern here, it is important when reviewing tests in general: 
note that the case where `ownerId` in the path doesn't match the `ownerId` in the request body 
is not tested. 
We would want to know, document, and express whether 
- one of them takes precedence,
- one of them ignored in all cases, 
- or it is an error if they are not the same.

In this case the schema clearly states there is no `ownerId` field in the request body.
If we want to demonstrate that the endpoint tolerates unkown fields, 
it's better to use a field with a less confusing, even expressive, name: 
such as `someUnmappedField`.

As for the service methods, we have `findOwnerById`:

```java
public Owner findOwnerById(int id) {
    Owner owner = null;
    try {
        owner = ownerRepository.findById(id);
    } catch (
            ObjectRetrievalFailureException | 
            EmptyResultDataAccessException e) {
        // just ignore not found exceptions for Jdbc/Jpa realization
        return null;
    }
    return owner;
}
```

Since there is a `try/catch` in the method,
we normally should consider the paths where `ownerRepository.findById` throws exceptions.
But we see that that case is explicitly handled the same as the case 
where repository returns a null, we will consider two cases where repository returns null, 
where it doesn't.

You might have noticed this two paths are the same as the two paths in the controller,
if you are familiar with spring+hibernate you can imagine already 
that there are similar redundant paths in the repository and 
the spring proxy that magically converts thrown exceptions to these these exceptions as well.
And that this is the case for all the entities. 
We will find a way to handle these.

---
**TODO**
A general handling strategy for missing entities needed.

---

As for the test cases for it two test cases are of interest:
- `shouldFindSingleOwnerWithPet`: This is _the test_ that tests `ownerRepository.findById`. 
But the pet bit is weird, for update we have no business with the pet.
We see that this method returns the `Pet`s of the `Owner`, even when not needed.
This is because this methods has more than a single responsibility:
  - One where it retrieves the `Owner` and also the related records 
to be displayed to the user.
  - The other it returns owners own fields to be updated.

  Since this test case is related to the first responsibility 
and we are interested in the second, we can ignore this test.

---
**TODO**
`findById` methods have multiple responsibilities.

---

- `shouldUpdateOwner`: This clearly is one of the test cases we should pay attention to,
just by judging from the name. 

```java
void shouldUpdateOwner() {
    Owner owner = this.ownerService.findOwnerById(1);
    String oldLastName = owner.getLastName();
    String newLastName = oldLastName + "X";

    owner.setLastName(newLastName);
    this.ownerService.saveOwner(owner);

    // retrieving new name from database
    owner = this.ownerService.findOwnerById(1);
    assertThat(owner.getLastName()).isEqualTo(newLastName);
}
```

```java
public void saveOwner(Owner owner) {
    ownerRepository.save(owner);
}
```
