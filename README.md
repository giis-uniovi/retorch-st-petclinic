[![Build Status](https://github.com/giis-uniovi/retorch-st-petclinic/actions/workflows/test.yml/badge.svg)](https://github.com/giis-uniovi/retorch-st-petclinic/actions)

# RETORCH PetClinic End-to-End Test Suite

This repository contains a detached fork of
[spring-petclinic-microservices](https://github.com/giis-uniovi/spring-petclinic-microservices) and an
End-to-End Test suite that are used as demonstrator of the [RETORCH Framework](https://github.com/giis-uniovi/retorch).
Spring PetClinic Microservices is a sample reference application based on the
[original Spring PetClinic](https://github.com/spring-petclinic/spring-petclinic-microservices), rebuilt as a
distributed microservices architecture using Spring Cloud, Spring Cloud Gateway, Eureka, Zipkin, Grafana and Prometheus,
all running in Docker containers.

## Deployment instructions

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (Windows / macOS) or Docker Engine (Linux)
- Git

### Local deployment — Windows

```powershell
# Start the SUT on the default port (8080)
.\deploy-local.ps1

# Start on a custom port
.\deploy-local.ps1 -Port 9090

# Tear down all containers and volumes
.\deploy-local.ps1 -Down
```

### Local deployment — Linux / macOS

```bash
# Make the script executable (first time only)
chmod +x deploy-local.sh

# Start the SUT on the default port (8080)
./deploy-local.sh

# Start on a custom port
./deploy-local.sh --port 9090

# Tear down all containers and volumes
./deploy-local.sh --down
```

Both scripts handle all setup steps automatically:
clone the `spring-petclinic-microservices` repository (required to build the Grafana and Prometheus images),
create the `jenkins_network` Docker network if it does not exist, build the images, start the containers and
wait up to 200 seconds for the application to be ready.
Once the SUT is up, it is accessible at `http://localhost:<port>` (default `http://localhost:5000`).

### CI deployment — Jenkins

The `Jenkinsfile` at the repository root defines the full pipeline used by the on-premises Jenkins instance.
It relies on the lifecycle scripts located in `.retorch/scripts/` and the environment files in
`.retorch/envfiles/`. The GitHub Actions workflow (`.github/workflows/test.yml`) only compiles the project;
the actual test execution is delegated to Jenkins.
