
### directory structure of the project:
```bash
$ cd src/main/java/org/springframework/samples/petclinic
$ find . -type d
```

output:
```text
.
./config
./mapper
./model
./repository
./repository/jdbc
./repository/jpa
./repository/springdatajpa
./rest
./rest/advice
./rest/controller
./security
./service
./util
```

config, model, repository, rest, service
standard directory names

mapper, security, util
shows technical concerns not in their respective layer

### Largest files

```bash
$ find src/main/java/ -type f -exec wc -l {} \; | sort -n
```

output:
```text
...Some line omitted
114 ./rest/controller/VetRestController.java
116 ./model/Visit.java
129 ./repository/jdbc/JdbcPetTypeRepositoryImpl.java
137 ./rest/controller/BindingErrorsResponse.java
149 ./model/Owner.java
163 ./rest/controller/OwnerRestController.java
169 ./repository/jdbc/JdbcPetRepositoryImpl.java
174 ./repository/jdbc/JdbcVetRepositoryImpl.java
177 ./repository/jdbc/JdbcVisitRepositoryImpl.java
196 ./repository/jdbc/JdbcOwnerRepositoryImpl.java
290 ./service/ClinicServiceImpl.java
```

#### ClinicServiceImpl 
Contains many unrelated methods. 
It violates single responsibility principle and has very low cohesion.
Some people find both SRP and cohesion vague or ambiguous.
This is a very clear example of a violation of these principles.
This clas should be split.

#### Jdbc repositories

Excessive amount of SQL is usually a sign of developers trying to compensate for 
their failure to formulate the problem without resorting to grotesque queries.
In this case this was to demonstrate framework capabilities 
and jdbc repositories provide the same functionality as the other implementations.
The may be removed in due time.

#### Fat controllers

A cursory look at OwnerRestController shows us that the methods do too much. 
Controllers like repositories must be paper thin. 
Ideally they just should delegate to the service layer.
We will investigate further and fix these also.

Another reason may be that the controllers have unrelated handlers stuck together.
This is not the case for these controllers.

#### Entities

A similar examination of Owner and Visit shows us boilerplate code, 
unhelpful javadoc comments and some loops and conditional that don't seem 
to be business logic related. We want to push as much code as possible to
entities, but only if it is business logic related.

We can reduce boilerplate by using lombok.

### Maven config

A look at the pom.xml shows us apart from standard spring boot configuration,
this project sports openapi and mapstruct code generation, jib for producing
docker images and jacoco for test coverage.

The build is very verbose though and we will get rid of maven in favor of gradle
ASAP.

Test coverage percentage is good enough for us to fix a lot. 
We may later need more broad-stack tests when doing more structural refactorings.

We see that project uses Java 8. We should get this to 11 at least.


### Running the build

After setting JAVA_HOME to the jdk 8 location, we can run the build.

```bash
$ export JAVA_HOME="--path to JDK 8--"
$ mvn clean install
```

The build runs smoothly.

By opening /target/site/jacoco/index.html in the browser, 
we can confirm the test coverage is indeed good. 
We also see that many uncovered branches are caused by unused mapper methods,
and uncovered null checks in mappers and update methods in controllers.
These can be covered with additional tests, but this is not urgent or crucial. 

### Next steps

Now that we confirmed we have test coverage and spotted some problems.
Our next step would be to improve the signal-to-noise ratio in the project.

In order to achieve this we will first migrate from maven to gradle 
then we will use lombok to remove some of the boilerplate. 

This will bring forward more problems that are hiding among the noise.

