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
- Default ctor left for now.
- useless javadoc particularly egregious

