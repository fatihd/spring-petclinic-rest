---
title: Welcome to Petclinic revisited
---

### Purpose

- Exploration
- Good practices
- Bad practices

### Audience

- devs who want to check
- devs who want to see considerations other than piece together something
- more production ready
- making changes to a system in production


### Scope
- what is in
- what is out


### Existing problems

+ service layer
  - low cohesion ClinicService
  - testing multiple repositories through service layer

+ controllers
  - long controller methods
  - FUBAR update in controller
  - multiple calls to service in controller
  - transaction in controller

+ model
  - unconnected visits and vets
  - unnecessary person supertype

+ dependencies
  - unused jaxb dependency


![alternative text](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.github.com/plantuml/plantuml-server/master/src/main/webapp/resource/test2diagrams.txt)

```mermaid
classDiagram
    Animal <|-- Duck
    Animal <|-- Fish
    Animal <|-- Zebra
    Animal : +int age
    Animal : +String gender
    Animal: +isMammal()
    Animal: +mate()
    class Duck{
        +String beakColor
        +swim()
        +quack()
    }
    class Fish{
        -int sizeInFeet
        -canEat()
    }
    class Zebra{
        +bool is_wild
        +run()
    }
```
