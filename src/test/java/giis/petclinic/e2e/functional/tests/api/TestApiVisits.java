package giis.petclinic.e2e.functional.tests.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import giis.petclinic.e2e.functional.common.BaseApiClass;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Validates the visits-service endpoints exposed through the API Gateway:
 * <ul>
 *   <li>POST /api/visit/owners/{ownerId}/pets/{petId}/visits — create a visit (HTTP 201)</li>
 *   <li>GET  /api/visit/owners/{ownerId}/pets/{petId}/visits — list visits for a pet</li>
 *   <li>GET  /api/visit/pets/visits?petId=...               — multi-pet visit query</li>
 * </ul>
 */
class TestApiVisits extends BaseApiClass {

    private static final int PET_TYPE_DOG = 2;
    private static final int PET_TYPE_CAT = 1;



    @AccessMode(resID = "visit", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "pet", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPICreateVisit")
    void testAPICreateVisit() throws IOException {
        String description = "Annual API checkup " + unique();
        int ownerId = createOwner("VisitCreate");
        int petId = createPet(ownerId, "Fido" + unique(), "2018-05-10", PET_TYPE_DOG);

        int status = postStatus(visitUrl(visitsPath(ownerId, petId)),
                visitPayload("2024-01-15", description));
        Assertions.assertEquals(201, status, "Creating a visit must return HTTP 201");

        JsonArray visits = getJsonArray(visitUrl(visitsPath(ownerId, petId)));
        Assertions.assertFalse(visits.isEmpty(), "Visit list must contain at least one entry");
        Assertions.assertTrue(containsByField(visits, "description", description),
                "Created visit '" + description + "' not found in visit list for pet " + petId);
    }

    @AccessMode(resID = "visit", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "pet", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPIVisitListStructure")
    void testAPIVisitListStructure() throws IOException {
        int ownerId = createOwner("VisitStructure");
        int petId = createPet(ownerId, "Lucky" + unique(), "2019-07-15", PET_TYPE_CAT);
        createVisit(ownerId, petId, "2024-03-01", "Structure check visit");

        JsonArray visits = getJsonArray(visitUrl(visitsPath(ownerId, petId)));
        Assertions.assertFalse(visits.isEmpty(), "Visit list must not be empty after creation");

        JsonObject visit = visits.get(0).getAsJsonObject();
        Assertions.assertAll(
                () -> Assertions.assertTrue(visit.has("id"),          "Visit must have 'id'"),
                () -> Assertions.assertTrue(visit.has("petId"),       "Visit must have 'petId'"),
                () -> Assertions.assertTrue(visit.has("visitDate"),   "Visit must have 'visitDate'"),
                () -> Assertions.assertTrue(visit.has("description"), "Visit must have 'description'")
        );
    }

    @AccessMode(resID = "visit", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "pet", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPIGetVisitsForMultiplePets")
    void testAPIGetVisitsForMultiplePets() throws IOException {
        int ownerId = createOwner("MultiPet");
        long ts = unique();
        int petId1 = createPet(ownerId, "Alpha" + ts, "2017-01-01", PET_TYPE_DOG);
        int petId2 = createPet(ownerId, "Beta" + ts,  "2018-02-02", PET_TYPE_CAT);
        createVisit(ownerId, petId1, "2024-04-01", "Alpha visit");
        createVisit(ownerId, petId2, "2024-04-02", "Beta visit");

        JsonObject response = getJsonObject(visitUrl("/pets/visits?petId=" + petId1 + "&petId=" + petId2));
        Assertions.assertTrue(response.has("items"), "Response must have 'items' array");
        JsonArray items = response.get("items").getAsJsonArray();
        Assertions.assertTrue(items.size() >= 2,
                "Expected at least 2 visits for the two pets, got " + items.size());
    }
}
