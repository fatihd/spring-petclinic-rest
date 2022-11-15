## Low-hanging fruit

### Deleting unused code

When we inspect the coverage report, we see that following mapper methods are not used:
- `PetMapperImpl.toPetTypeDtos`
- `UserMapperImpl.toRoleDtos`
- `UserMapperImpl.toRoles`
- `PetTypeMapperImpl.toPetType(PetTypeFieldsDto)`

If we look closely though we could recognize `PetTypeMapper.toPetType(PetTypeFieldsDto)` 
should have been used instead of `PetTypeMapper.toPetType(PetTypeDto)` 
in `PetTypeRestController.addPetType(PetTypeDto)`. 
But this change affects the API, and should be done as an API change.
For now, we can delete the dead code, and add it back later when needed. 

When we generate the test coverage report again we see that line coverage went from 88% to 89%,
and branch coverage went from 67% to 69%. Let's adjust the jacoco minimum coverage as .88 and.68,
leaving .01 for rounding errors.

#### Deleting maven related code

We haven't ported the jib configuration to gradle yet, 
but we will go ahead and remove maven files anyway.
If we need it, it still is in the git history.

### Reducing boilerplate with lombok

Let's add lombok and mapstruct lombok binding as in the example from 
[mapstruct repository](https://github.com/mapstruct/mapstruct-examples/blob/main/mapstruct-lombok/build.gradle):

```groovy
    implementation "org.mapstruct:mapstruct:${mapstructVersion}", 
        "org.projectlombok:lombok:${lombokVersion}"
    annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}", 
            "org.projectlombok:lombok:${lombokVersion}", 
            "org.projectlombok:lombok-mapstruct-binding:${lombokMapstructBindingVersion}"
```

We can now remove getters, setters, and other boilerplate should they exist, from model classes.
This is not perfectly safe, if there is a piece of functionality that is not tested but depends,
wrongly, on the particular format of toString method, it could possibly fail. 
Whenever we see such a code we should fix it ASAP. 

#### BaseEntity
- We remove the getters and setters. 
- We note that this a `@MappedSuperclass` but not `abstract`. This is weird.
- We also note that `Base...` is a useless prefix

#### NamedEntity
- We remove the getters and setters and toString.
- We note that this a `@MappedSuperclass` but not `abstract`. We make a note of this also.

#### Owner
- We remove the toString as well as getters and setters for `address`, `city`, and `telephone`.
- getters and setters for pets should also be removed, 
but they are not absolutely trivial, we leave it for later. 
- We note that javadoc comments are pretty useless

#### package-info.java
Deleted.

#### Person
- We remove the getters and setters.
- We note that this a `@MappedSuperclass` but not `abstract`.

#### Pet
- We remove the getters and setters.
- getters and setters for `visits` left for now.

#### PetType
- Left alone

#### Role
- We remove the getters and setters.

#### Specialty
- Left alone

#### User
- We remove the getters and setters.

#### Vet
- getters and setters for `specialties` left for now.


#### Visit
- Default constructor left for now.
- useless javadoc particularly egregious

#### Constructor Injection

In all controllers except for `RootRestController` we replace the existing constructor
with Lombok's `@RequiredArgsConstructor` annotation. 
As we have said before, this is to reduce boilerplate and increase signal-to-noise ratio.
Lombok's primary benefit is not it saving us from typing stuff, which is easy; 
but to save us from reading stuff, paying attention it indeed is doing exactly 
what is expected to be doing.

Similarly, we mark the dependencies of `ClinicServiceImpl` and `UserServiceImpl`
as final and we remove existing constructor while adding `@RequiredArgsConstructor` 
annotations to these classes.

### Splitting ClinicService

Previously we had seen that the longest production java file in the code base was `ClinicServiceImpl`.
When we search the code base we see that this class is not used directly. 
Instead, it is used, as expected, through its interface `ClinicService`.

When we read through this interface a few issues come forward. 

Most prominent of these issues is that 
this interface consists of CRUD operations for some of the entities in the system. 
These methods comprise clumped together. These clumps usually means they want to have their own names.
If there is a clump of statements, a method could be extracted; 
and if there is a clump of fields, a class can be extracted.
If as in this case methods on the interface are in clumps, the interface needs to be split.
In fact as it stands this interface is an obvious violation of SRP and ISP of SOLID principles.

The second one is the `throws DataAccessException` declarations for most of the methods.
Why is an exception clearly related to *data access* concerns is on the service layer interface.
We also note that this is an unchecked exception, which we normally need not declare.
Is this exception declared because it is caught and handled in a special way,
such that forgetting to catch it would change the behavior of the system?
Note this would violate **Use exceptions only for exceptional conditions** principle from Effective Java.
But even if this violation exists, we need to keep the current behavior.
When we search the code we see that this exception indeed is not caught.
We can also see that this exception comes from a third party library (in this case spring-tx).
3rd party types on service layer interface usually means our layers are not defined well, 
such that technical concerns leaked into business layer.

A third one is that we have two methods one named `findVets()` and another named `findAllVets()`.
And they do the same thing. Imperfect duplication is a serious problem in legacy codebases, 
there are multiple pieces of code that do basically the same thing, 
albeit possibly in slightly different ways.
Because of these differences we cannot eliminate the redundancy by keeping one copy and simply deleting the rest.
In this case we are in luck, `findVets()` is only used in tests.

We eliminate the latter two by simple deletion. In the test `shouldFindVets()`, 
we replace the call to `findVets()` with a call to `findAllVets()`.

As for the splitting we can rely on the IDE to make our work easier. 
We start by splitting the interface first and making the `ClinicServiceImpl` 
implement the newly extracted interfaces.
We then split the `ClinicServiceImpl` itself.
We leave the `AbstractClinicServiceTests` for now, 
except for replacing the references to `ClinicService` with the new interfaces.
We do this because we do not want to read the tests in depth.
We limit ourselves at this stage to automatic refactorings and cut&paste.

