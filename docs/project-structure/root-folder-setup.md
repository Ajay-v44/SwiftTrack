# Parent `pom.xml` Overview & Solution Canvas

## Purpose

The parent `pom.xml` is set up at the project root to enable a modular Maven structure for Spring Boot microservices. This solves the issue where individual services within a `/services` folder are **not recognized as runnable modules** by IntelliJ IDEA or Maven unless explicitly defined.

## What Was Done

- **Created a parent `pom.xml`** at the project root, configured with `<packaging>pom</packaging>`.
- **Defined all microservices as Maven modules** in the `<modules>` section (e.g., `services/EurekaServer`, `services/api-gateway`, etc.).
- **Added `dependencyManagement`** in parent `pom.xml` to handle shared dependency versions (incl. Spring Cloud).
- **Set common project properties** (`java.version`, `spring.boot.version`) for consistency across modules.

## Problem Solved

- **Before:** IntelliJ IDEA did not recognize each microservice as an independent Maven project when they were placed inside a `services` directory.
- **Now:** Opening the root project in IntelliJ IDEA auto-detects and configures all services as individual runnable modules, each with their own `pom.xml`, making development and management much smoother.

## How It Works

- The parent `pom.xml` acts as an aggregator, controlling project-wide dependencies and module membership.
- Each service is referenced as a `<module>` and must contain its own Maven project structure (`pom.xml`, source code).
- All shared dependencies and properties are managed from the top level.
- New microservices are added by:
    - Creating a new folder inside `services/`
    - Adding the folder name to the `<modules>` list in root `pom.xml`
    - Creating a `pom.xml` for the new service

## Benefits

- **Scalability**: Easily add, remove, or update microservices.
- **IDE Integration**: Each service appears as a separate project/module.
- **Centralized Management**: Streamlined dependency and version control.
- **Readiness for CI/CD**: Multi-module projects are compatible with most build pipelines and test setups.

---
**Copy this canvas into your documentation so contributors understand the parent `pom.xml` setup, the motivation for this structure, and how to extend the modular system for future microservices.**
