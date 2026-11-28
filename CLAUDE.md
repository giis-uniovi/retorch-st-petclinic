# CLAUDE.md — retorch-st-petclinic

Demonstrator of the [RETORCH](https://github.com/giis-uniovi/retorch) framework: a Java end-to-end suite for
[Spring PetClinic Microservices](https://github.com/giis-uniovi/spring-petclinic-microservices) with two kinds of tests: **browser** tests
(Selenium, AngularJS UI) and **API** tests (HTTP through the API gateway). The SUT is **not** stored here: `deploy.sh` / `deploy.ps1` and the
`coi-setup` step of the pipeline clone it. See `README.md` for the user documentation.

## RETORCH rules (do not break)

- Every test method carries `@AccessMode(...)` annotations (`giis.retorch.annotations`) that declare the logical resources it uses and
  how (`READONLY`/`READWRITE`, concurrency, sharing). The TJob schedule of the `Jenkinsfile` is **generated from them**.
  - Never drop or alter existing `@AccessMode` annotations when refactoring.
  - Never rename test classes or methods: the `Jenkinsfile` references them as `Class#method`.
  - A new test method needs sensible `@AccessMode` values (copy a sibling) and is only run by CI after regenerating the pipeline.
- `Jenkinsfile`, `.retorch/scripts/`, `.retorch/envfiles/` (except `local.env`), `.retorch/executionplan.json` and the resource model in
  `.retorch/configurations/*SystemResources.json` are **generated** by `orchestrate.ps1` (it needs the
  `retorch-orchestration-*-standalone.jar` of the RETORCH tool in the repository root). Do not edit them by hand; put SUT-specific
  commands in `.retorch/customscriptscode/` and settings in `.retorch/configurations/retorchCI.properties`.
- Only `retorch-annotations` is a dependency of the tests; the orchestration code is never part of this project.
- `profiling=true` in `retorchCI.properties` makes the pipeline measure the resources of every TJob and publish
  `artifacts/usageprofile/usageprofile.html`. `.retorch/customscriptscode/custom-usageprofile` temporarily builds the profiling tool from
  source; delete it when a release publishes the jar.

## Layout

```
src/test/java/giis/petclinic/e2e/functional/
  common/    BaseLoggedClass (Selenium), BaseApiClass (HTTP), ElementNotFoundException
  tests/     TestOwners, TestPets, TestVisits, TestVets, TestNavigation          (browser)
  tests/api/ TestApiVets, TestApiOwners, TestApiPets, TestApiVisits, TestApiGateway
  utils/     Navigation, Waiter, Click
src/test/resources/   test.properties (BROWSER_USER, LOCALHOST_URL), log4j2.xml
deploy.sh / deploy.ps1   local deployment (--port, --no-build, --down); project "default", .retorch/envfiles/local.env
.retorch/                generated pipeline files + customscriptscode/ (coi-setup clones the SUT), configurations/retorchCI.properties
orchestrate.ps1 + Jenkinsfile   regenerate / the generated pipeline: 12 TJobs (tjoba…tjobl) in 3 stages
```

RETORCH resources: `web-browser`, `frontend`, `gateway`, `vet`, `owner`, `pet`, `visit`. API tests use neither `web-browser` nor `frontend`, so
read-only API tests can run with higher concurrency.

## SUT facts

- Local URL `http://localhost:5000` (the api-gateway); inside the CI network `http://<tjob>-api-gateway:8080`.
- Routes: `/api/vet/**`, `/api/customer/**`, `/api/visit/**` (the gateway strips the 2-segment prefix) and the composite `/api/gateway/**`.
- Eventual consistency: after creating a visit the composite `GET /api/gateway/owners/{id}` may briefly be stale. `TestApiGateway` polls with
  `LockSupport.parkNanos` (never `Thread.sleep`, Sonar `java:S2925`); the UI `Waiter.waitForVisitText` retries with a refresh.
- The UI renders dates as `yyyy MMM dd` (`2023 Mar 15`) although the backend stores ISO dates.
- Waits that work: `By.linkText("Edit Owner")` for the owner page (Angular routes have no plain `href`) and
  `numberOfElementsToBeMoreThan(tbody tr, 0)` for the vets table (it renders empty before the API answers).

## Build and test

- Java 8 source level (no `var`, records or `List.of`), JUnit Jupiter, Selenium 4, Selema, Apache HttpClient 4.5, Gson, Log4j2.
- Local run: `./deploy.sh` (or `.\deploy.ps1`), then `mvn test` (or `-Dtest=TestApiVets`). Target: `SUT_URL` property → `SUT_URL` env var →
  `LOCALHOST_URL` of `test.properties`. `SELENOID_PRESENT` runs the browsers in a Selenoid / Selenium Grid hub.
- Logs go to `target/testlogs/log${sys:TJOB_NAME:-testinglocal}-test.log`, one file per TJob.

## CI

- Jenkins, agent label `xretorch-agent`. Every TJob deploys its **own** copy of the SUT (`--env-file .retorch/envfiles/<tjob>.env
  -p <tjob>`) on the external network `jenkins_network`, runs its tests and tears it down; the TJobs of a stage run in parallel.
- The TJob runs `mvn test -Dtest="<Class#method>,..." -DTJOB_NAME=<tjob> -DSUT_URL=<url>`.
- `pom.xml` builds into `target/${TJOB_NAME}` so parallel TJobs never share compiled classes or reports (profile `local-execution` sets
  `TJOB_NAME=local` when the property is absent).
- `.github/workflows/test.yml` only compiles the project (`mvn test-compile`); the tests run in Jenkins.

## Conventions

- Comments only for a non-obvious *why*. `@DisplayName` is a readable sentence, not the method name.
- Each test creates its own data with a unique suffix (parallel and repeated runs must not collide).
- API setup goes through the `BaseApiClass` helpers (`createOwner()`, `createPet()`, `createVisit()`, `get/post/put` + `*Status`,
  `getJsonObject/getJsonArray`, `containsByField`); do not rebuild JSON payloads inline.
- `Assertions.assertAll` for several fields of one object. Use the inherited `log`; never create a logger in a test class.
