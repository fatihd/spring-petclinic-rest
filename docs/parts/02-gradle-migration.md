## Gradle migration

### Generating a gradle build script

```bash
~/dev/gradle-7.5.1/bin/gradle init
```

After selecting default options build.gradle and wrapper is generated.

However, if we are using JDK8, e.g. JAVA_HOME is pointing to JDK8,
when we run the following command:
```bash
./gradlew clean build
```
We see a bunch of error like the following:
```text
.../VetRestController.java:57: error: cannot find symbol
    public ResponseEntity<List<VetDto>> listVets() {
                               ^
  symbol:   class VetDto
  location: class org.springframework.samples.petclinic.rest.controller.VetRestController
```
This is expected as gradle init didn't port the plugin settings.

### Configuring OpenAPI generator

We find the GitHub page of the necessary plugin by searching online:
https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin
And we follow the instruction to add the plugin to our build.

First we add the plugin to the plugins section in the `build.gradle`:
<pre>
plugins {
    id 'java'
    id 'maven-publish'
    <strong>id "org.openapi.generator" version "6.2.1"</strong>
}
</pre>

Then we add `compileJava.dependsOn tasks.openApiGenerate` to build.gradle.

If we try to build at this stage we will get an error missing configuration. Fair enough.
If we copy the plugin configuration and ignoring the unsupported configurations we get:
```groovy
openApiGenerate {
    inputSpec = "$rootDir/src/main/resources/openapi.yml".toString()
    generatorName = 'spring'
    library = 'spring-boot'
    modelNameSuffix = 'Dto'
    apiPackage = 'org.springframework.samples.petclinic.rest.api'
    modelPackage = 'org.springframework.samples.petclinic.rest.dto'
    outputDir = "$buildDir/generated".toString()
    configOptions = [
            interfaceOnly        : 'true',
            performBeanValidation: 'true',
            dateLibrary          : 'java8',
            java8                : 'true',
            openApiNullable      : 'false',
            serializationLibrary : 'jackson',
            documentationProvider: 'springdoc'
    ]
}
```

After this we can run `./gradlew clean build` and see that the files are generated. 
However, we see that generated files are not taken into account during compilation.
We need to add the generated files to the source sets:
```groovy
sourceSets.main.java.srcDirs += ["$buildDir/generated/src/main/java"]
```

At this point the build complete successfully, if a little too quickly, but the tests would not have run.

### Test configuration
The build completed too quickly, because we need to specify which test framework should be used:
```groovy
test {
    useJUnitPlatform()

    testLogging {
        showStandardStreams = true
    }
}
```

When we do this we see errors like the following:
```text
java.lang.NoSuchMethodError: org.springframework.boot.SpringApplication.convertEnvironment(Lorg/springframework/core/env/ConfigurableEnvironment;)Lorg/springframework/core/env/StandardEnvironment;
```

### Fixing dependency conflicts

This error means there is a conflict regarding the version of the jar containing `org.springframework.boot.SpringApplication`,
such that some other library expecting one version (which contains the method mentioned) but finds another version on the class path.

Running the following command confirms this:
```bash
$ ./gradlew -q dependencies --configuration testRuntimeClasspath
```

<pre>
testRuntimeClasspath - Runtime classpath of source set 'test'.
+--- org.springframework.boot:spring-boot-starter-actuator:2.6.2
|    +--- org.springframework.boot:spring-boot-starter:2.6.2
|    |    +--- <strong>org.springframework.boot:spring-boot:2.6.2 -> 2.7.2</strong>
|
<i>.... omitted lines ....</i>
</pre>

Since we find the conflicting package probably causing the failures, 
we now need to find the package that brings the 2.7.2 version overriding the specified 2.6.2 version:
```bash
./gradlew -q dependencyInsight --dependency spring-boot
```

<pre>
<i>...omitted lines...</i>
org.springframework.boot:spring-boot:2.7.2
\--- org.springframework.boot:spring-boot-autoconfigure:2.7.2
     +--- <strong>org.springdoc:springdoc-openapi-common:1.6.11</strong>
<i>...omitted lines...</i>
</pre>

the culprit seems to be `springdoc-openapi-common`. In other cases we might bump the older dependency version up 
or downgrade the dependencies bringing incompatible newer version. 
This may cause a cascade effect, so we first will try to exclude the conflicting version and cross our fingers:
<pre>
<del>implementation 'org.springdoc:springdoc-openapi-ui:1.6.11'</del>
<st>implementation('org.springdoc:springdoc-openapi-ui:1.6.11') {
    exclude group: 'org.springframework.boot'
}</st>
</pre>

When we try to build we now get a bunch of errors like the following:

```text
Parameter 1 of constructor in org.springframework.samples.petclinic.rest.controller.OwnerRestController required a bean of type 'org.springframework.samples.petclinic.mapper.OwnerMapper' that could not be found.
```

This shows our tests are actually running, but we now need to configure mapstruct.

### Configuring mapstruct

Since tests takes quite some time to run we will try to run a single one of the failing tests for a while until we fix this error:
```text
$ ./gradlew cleanTest test --tests org.springframework.samples.petclinic.SpringConfigTests
```

Consulting the mapstruct site (https://mapstruct.org/documentation/installation/) we see `gradle init` 
ignored the annotation processor.

<pre>
<del>implementation 'org.mapstruct:mapstruct-processor:1.4.1.Final'</del>
<strong>annotationProcessor 'org.mapstruct:mapstruct-processor:1.4.1.Final'</strong>
</pre>

We copy the compiler args for mapstruct from `pom.xml`:
```groovy
tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
    options.compilerArgs = [
            '-Amapstruct.suppressGeneratorTimestamp=true',
            '-Amapstruct.suppressGeneratorVersionInfoComment=true',
            '-Amapstruct.defaultComponentModel=spring'
    ]
}
```

After this the single test passes, then we run all the tests.

We see the build complete successfully.

### Configuring test coverage

We need to ensure we still have the same amount of coverage as before. For this we will configure the jacoco plugin:

<pre>
plugins {
<i>...omitted lines...</i>
<strong>id 'jacoco'</strong>
}
</pre>

and 

```groovy
jacocoTestReport {
    dependsOn test
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                    'org/springframework/samples/petclinic/rest/dto/**',
                    'org/springframework/samples/petclinic/rest/api/**'
            ])
        }))
    }
}
```

Examining `build/reports/jacoco/test/html/index.html` we see that coverage is the same as expected.

Verification then can be configured as such: 

```groovy
jacocoTestCoverageVerification {
    dependsOn test

    violationRules {
        rule {
            limit {
                limit {
                    counter = 'LINE'
                    minimum = 0.85
                }
                limit {
                    counter = 'BRANCH'
                    minimum = 0.66
                }
            }
        }
    }
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                    'org/springframework/samples/petclinic/rest/dto/**',
                    'org/springframework/samples/petclinic/rest/api/**'
            ])
        }))
    }
}
```

```bash
./gradlew cleanTest jacocoTestCoverageVerification
```

The build passes. But in order for us to be sure that this verification works we need to see it fail.
We can do this by disabling some tests. We open `AbstractClinicServiceTests` and replace all `@Test` with `// @Test`.
When we run the command above again, we now see the build fail with the following message:

```text
Execution failed for task ':jacocoTestCoverageVerification'.
> Rule violated for bundle spring-petclinic-rest: lines covered ratio is 0.53, but expected minimum is 0.85
  Rule violated for bundle spring-petclinic-rest: branches covered ratio is 0.45, but expected minimum is 0.66
```
