# CLAUDE.md — retorch-st-petclinic

This file provides Claude Code with the context needed to work in this repository without re-deriving it each session.

## Project purpose

End-to-end test suite for the [Spring PetClinic Microservices](https://github.com/giis-uniovi/spring-petclinic-microservices) application, orchestrated with the [RETORCH](https://github.com/giis-uniovi/retorch) framework. The project contains two complementary test suites:

- **Frontend (E2E/browser)** — Selenium WebDriver tests that drive the AngularJS UI through Chrome.
- **API** — HTTP-level tests that call the REST endpoints directly through the API Gateway.

Both suites share the same RETORCH resource model and run under the same CI pipeline.

---

## System Under Test (SUT)

The SUT is **not committed** to this repository. It is cloned and started by the deployment scripts at runtime:

```bash
# Linux/macOS
./deploy-local.sh

# Windows PowerShell
./deploy-local.ps1
```

The scripts clone `https://github.com/giis-uniovi/spring-petclinic-microservices.git`, start Docker Compose, and wait up to 200 seconds for the API Gateway to be ready.

**Default local URL:** `http://localhost:5000`

### Microservices and API Gateway routes

| Route prefix | Target service | Example endpoint |
|---|---|---|
| `/api/vet/**` | vets-service | `GET /api/vet/vets` |
| `/api/customer/**` | customers-service | `POST /api/customer/owners` |
| `/api/visit/**` | visits-service | `POST /api/visit/owners/{o}/pets/{p}/visits` |
| `/api/gateway/**` | ApiGatewayController (composite) | `GET /api/gateway/owners/{id}` |

The gateway strips the 2-segment prefix before forwarding (e.g., `/api/customer/owners` → `/owners` on the customers-service).

---

## Repository layout

```
src/test/java/giis/petclinic/e2e/functional/
├── common/
│   ├── BaseLoggedClass.java      # Selenium base class (browser lifecycle, shared helpers)
│   ├── BaseApiClass.java         # HTTP base class (HttpClient lifecycle, REST helpers)
│   └── ElementNotFoundException.java
├── tests/
│   ├── TestOwners.java           # Browser: owner CRUD + search
│   ├── TestPets.java             # Browser: add pet to owner
│   ├── TestVisits.java           # Browser: schedule a visit
│   ├── TestVets.java             # Browser: vet list
│   ├── TestNavigation.java       # Browser: navbar + page routing
│   └── api/
│       ├── TestApiVets.java      # API: GET /api/vet/vets
│       ├── TestApiOwners.java    # API: CRUD /api/customer/owners
│       ├── TestApiPets.java      # API: pet types, create/get/update pet
│       ├── TestApiVisits.java    # API: create/list visits, multi-pet query
│       └── TestApiGateway.java   # API: composite GET /api/gateway/owners/{id}
└── utils/
    ├── Navigation.java           # Page navigation helpers
    ├── Waiter.java               # Explicit wait helpers
    └── Click.java                # Safe click with JS fallback
```

---

## Key dependencies

| Dependency | Purpose |
|---|---|
| JUnit 5 (Jupiter) | Test runner |
| Selenium WebDriver 4.x | Browser automation (frontend tests) |
| Selema | Browser lifecycle manager (wraps SeleManager) |
| Apache HttpClient 4.5.14 | HTTP client (API tests) |
| Gson 2.14.0 | JSON parsing (API tests) |
| RETORCH annotations | `@AccessMode` resource declarations for scheduling |
| Log4j2 + SLF4J | Structured logging with per-TJob log files |

---

## RETORCH resource model

Each test declares the resources it accesses with `@AccessMode`. RETORCH uses these to generate a parallel-safe Jenkinsfile (via `RetorchGenerateJenkinfileTest`).

| Resource ID | Represents | Typical access |
|---|---|---|
| `frontend` | AngularJS SPA (read-only, high concurrency) | `READONLY, concurrency=10, sharing=true` |
| `web-browser` | Chrome browser instance | `READWRITE, concurrency=1, sharing=false` |
| `vet` | Vet data in the database | `READONLY, concurrency=10, sharing=true` (API) / `concurrency=1` (browser) |
| `owner` | Owner data in the database | `READWRITE, concurrency=1, sharing=false` |
| `pet` | Pet data in the database | `READWRITE, concurrency=1, sharing=false` |
| `visit` | Visit data in the database | `READWRITE, concurrency=1, sharing=false` |

**API tests do not use `web-browser` or `frontend`** — they interact directly with the HTTP layer. They can therefore run with higher concurrency for read-only operations (e.g., vets).

---

## Configuration

### `src/test/resources/test.properties`
```properties
BROWSER_USER=CHROME
LOCALHOST_URL=http://localhost:5000
```
Both `BaseLoggedClass` and `BaseApiClass` read `LOCALHOST_URL` as the SUT base URL. In CI, `SUT_URL` is passed as a system property to override it.

### `src/test/resources/log4j2.xml`
Log files are written to `target/testlogs/log${sys:TJOB_NAME:-testinglocal}-test.log`. Each parallel TJob writes to a separate file, preventing log overwrites in CI.

### `pom.xml` — per-TJob build directory
```xml
<directory>${project.basedir}/target/${TJOB_NAME}</directory>
```
Each Maven invocation writes compiled classes and surefire reports under `target/{TJOB_NAME}/`, preventing parallel CI builds from corrupting each other's compiled output. A Maven profile (`local-execution`) defaults `TJOB_NAME=local` when the property is absent.

---

## Running tests

### Local — full suite
```bash
# Start the SUT first
./deploy-local.sh        # Linux
./deploy-local.ps1       # Windows

# Run all tests
mvn test
```

### Local — specific class
```bash
mvn test -Dtest=TestApiVets
mvn test -Dtest=TestOwners
```

### CI (Jenkins)
Tests are executed by the RETORCH TJob lifecycle scripts:
```bash
.retorch/scripts/tjoblifecycles/tjob-testexecution.sh <TJOB_NAME> <STAGE> <SUT_URL> "<TestClass#method>"
```
which internally runs:
```bash
mvn test -Dtest="<TestClass#method>" -DTJOB_NAME="<TJOB_NAME>" -DSUT_URL="<SUT_URL>"
```

---

## Known issues and design decisions

### Eventual consistency in visits
The visits-service is a separate microservice. After `POST /api/visit/…`, the API Gateway's composite `GET /api/gateway/owners/{id}` may briefly return stale visit data. `TestApiGateway.testGatewayOwnerPetHasVisits` includes a 500 ms sleep before the assertion. The frontend `Waiter.waitForVisitText` uses a refresh-retry loop (up to 3 attempts) for the same reason.

### Parallel build isolation
Multiple TJobs run concurrently in CI, each calling `mvn test` in the same workspace. The `<directory>` override in `pom.xml` plus per-TJob log files prevent classpath and log file collisions.

### Date display format
The PetClinic backend stores dates as ISO strings (`yyyy-MM-dd`) but the AngularJS frontend renders them as `yyyy MMM dd` (e.g., `2023 Mar 15`). `TestVisits` uses the displayed format `"2023 Mar 15"` in its row-level assertion.

### `waitForOwnerDetailsPage` selector
Uses `By.linkText("Edit Owner")` — the "Add New Pet" href variant (`a[href*='pets/new']`) was tried first but Angular's router does not render a plain `href` attribute for route links, so the link text approach is more reliable.

### `waitForVetsPage` selector
Uses `ExpectedConditions.numberOfElementsToBeMoreThan(tbody tr, 0)` rather than just table visibility, because the vets table element renders empty before the API response arrives.

---

## Code style conventions (for this project)

- **No comments** unless the WHY is non-obvious (constraint, workaround, invariant).
- **`@DisplayName`** must be a human-readable sentence, not the method name.
- **Test data isolation**: each test creates its own data using a unique suffix to avoid name collisions across parallel or repeated runs.
- **`BaseApiClass` helpers**: use `createOwner()`, `createPet()`, `createVisit()` for setup. Do not duplicate the JSON payload construction inline.
- **Assertions**: use `Assertions.assertAll` when checking multiple fields of the same object.
- **Logger**: always use the inherited `log` field; never instantiate a new logger in a test class.

---

## API test helpers (`BaseApiClass`)

The API tests share a single base class that hides HTTP plumbing and JSON ceremony so the test bodies stay focused on the scenario.

### HTTP verbs
- `get(url)`, `getStatus(url)` — bare body / status only
- `post(url, body)`, `postStatus(url, body)` — create operations
- `put(url, body)` — update operations (PetClinic returns HTTP 204, so only the status variant exists)

### JSON parsing
- `getJsonObject(url)` — GET and parse the response as a `JsonObject`
- `getJsonArray(url)` — GET and parse the response as a `JsonArray`
- `containsByField(array, fieldName, value)` — true if any element of an array has a matching string field; replaces manual loops over `JsonArray`

### URL builders
- `vetUrl(path)`, `customerUrl(path)`, `visitUrl(path)`, `gatewayUrl(path)` — one per API Gateway route prefix; prevents typos and centralizes the base URL.

### Unique test-data generation
- `unique()` — returns `System.currentTimeMillis()` as a uniqueness suffix
- `phone(ts)` — formats a timestamp as a 10-digit phone number (PetClinic's validation max)

### JSON payload builders (Gson-backed, no `String.format`)
- `ownerPayload(firstName, lastName, address, city, telephone)`
- `petPayload(name, birthDate, typeId)` — without `id`, for create
- `petPayload(id, name, birthDate, typeId)` — with `id`, for update
- `visitPayload(visitDate, description)`

### End-to-end fixture creation
- `createOwner(firstName, lastName, address, city, telephone)` — full control, returns the assigned ID
- `createOwner(label)` — convenience: derives all fields from `label + unique()`; use when the test does not assert against the owner's specific fields
- `createPet(ownerId, name, birthDate, typeId)` — returns the assigned pet ID
- `createVisit(ownerId, petId, date, description)` — returns the raw response

### Typical test shape (read-only)
```java
@AccessMode(resID = "vet", concurrency = 10, sharing = true, accessMode = "READONLY")
@AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
@Test
@DisplayName("GET /api/vet/vets returns HTTP 200 with a non-empty vet list")
void testGetVetList() throws IOException {
    Assertions.assertEquals(200, getStatus(vetUrl("/vets")), "Expected HTTP 200");
    JsonArray vets = getJsonArray(vetUrl("/vets"));
    Assertions.assertFalse(vets.isEmpty(), "Expected at least one vet in the list");
}
```

### Typical test shape (write + verify)
```java
@AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READWRITE")
@AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
@Test
@DisplayName("PUT /api/customer/owners/{id} returns HTTP 204 and persists the new field values")
void testUpdateOwner() throws IOException {
    long ts = unique();
    int ownerId = createOwner("Update", "Me" + ts, "3 Update Rd.", "OldCity", phone(ts));

    int putStatus = put(customerUrl("/owners/" + ownerId),
            ownerPayload("Update", "Me" + ts, "3 Update Rd.", "NewCity", phone(ts + 1)));
    Assertions.assertEquals(204, putStatus, "Updating an owner must return HTTP 204");

    JsonObject owner = getJsonObject(customerUrl("/owners/" + ownerId));
    Assertions.assertEquals("NewCity", owner.get("city").getAsString(), "city must be updated");
}
```

### Eventual consistency
The gateway endpoint (`GET /api/gateway/owners/{id}`) merges customer + visits data reactively. After creating a visit, the visit may not appear in the composite response immediately. `TestApiGateway` uses a polling helper (`pollForVisit`) with `LockSupport.parkNanos` for backoff — never `Thread.sleep` (flagged by the lint rule `java:S2925`).
