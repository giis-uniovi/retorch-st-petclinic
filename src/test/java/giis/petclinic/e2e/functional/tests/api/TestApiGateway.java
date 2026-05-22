package giis.petclinic.e2e.functional.tests.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import giis.petclinic.e2e.functional.common.BaseApiClass;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * Validates the API Gateway composite endpoint that aggregates customers-service
 * and visits-service data into a single response:
 * <ul>
 *   <li>GET /api/gateway/owners/{ownerId} — owner enriched with pets and their visit histories</li>
 * </ul>
 */
class TestApiGateway extends BaseApiClass {

    private static final int PET_TYPE_CAT = 1;
    //Maximum time to wait for the reactive aggregator to surface a freshly-created visit.
    private static final long VISIT_PROPAGATION_TIMEOUT_MS = 10_000;
    private static final long VISIT_PROPAGATION_POLL_MS = 250;

    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "pet", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "visit", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPIGatewayOwnerHasPetsWithVisits")
    void testAPIGatewayOwnerHasPetsWithVisits() throws IOException {
        String description = "Gateway composite visit " + unique();
        int ownerId = createOwner("Gateway");
        int petId = createPet(ownerId, "CompPet" + unique(), "2020-11-20", PET_TYPE_CAT);
        createVisit(ownerId, petId, "2024-05-01", description);

        JsonObject owner = pollForVisit(ownerId, description);
        Assertions.assertEquals(ownerId, owner.get("id").getAsInt(), "Owner ID must match");
        Assertions.assertTrue(owner.has("pets"), "Response must contain 'pets' array");

        JsonArray pets = owner.get("pets").getAsJsonArray();
        Assertions.assertFalse(pets.isEmpty(), "Pets array must contain at least one pet");

        JsonObject pet = pets.get(0).getAsJsonObject();
        Assertions.assertTrue(pet.has("visits"), "Pet entry must have 'visits' array");
        Assertions.assertTrue(containsByField(pet.get("visits").getAsJsonArray(), "description", description),
                "Visit '" + description + "' not found in gateway composite response within "
                        + VISIT_PROPAGATION_TIMEOUT_MS + " ms");
    }

    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPIGatewayOwnerFields")
    void testAPIGatewayOwnerFields() throws IOException {
        int ownerId = createOwner("GatewayFields");

        Assertions.assertEquals(200, getStatus(gatewayUrl("/owners/" + ownerId)),
                "Gateway owner endpoint must return HTTP 200");

        JsonObject owner = getJsonObject(gatewayUrl("/owners/" + ownerId));
        Assertions.assertAll(
                () -> Assertions.assertTrue(owner.has("id"), "Gateway response must have 'id'"),
                () -> Assertions.assertTrue(owner.has("firstName"), "Gateway response must have 'firstName'"),
                () -> Assertions.assertTrue(owner.has("lastName"), "Gateway response must have 'lastName'"),
                () -> Assertions.assertEquals("GatewayFields", owner.get("firstName").getAsString(),
                        "firstName must match the created owner")
        );
    }

    /**
     * Polls {@code GET /api/gateway/owners/{id}} until the visit with {@code description}
     * appears in the composite response, or {@link #VISIT_PROPAGATION_TIMEOUT_MS} elapses.
     * Returns the final owner JSON so the caller can run further assertions on it.
     */
    private JsonObject pollForVisit(int ownerId, String description) throws IOException {
        long deadline = System.currentTimeMillis() + VISIT_PROPAGATION_TIMEOUT_MS;
        JsonObject owner;
        while (true) {
            owner = getJsonObject(gatewayUrl("/owners/" + ownerId));
            if (compositeContainsVisit(owner, description) || System.currentTimeMillis() >= deadline) {
                return owner;
            }
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(VISIT_PROPAGATION_POLL_MS));
            if (Thread.currentThread().isInterrupted()) {
                return owner;
            }
        }
    }

    /**
     * Validation method that checks if the provided owner (JSON) contains a visit.
     */
    private static boolean compositeContainsVisit(JsonObject owner, String description) {
        if (!owner.has("pets")) return false;
        for (com.google.gson.JsonElement pet : owner.get("pets").getAsJsonArray()) {
            if (pet.isJsonObject() && pet.getAsJsonObject().has("visits")
                    && containsByField(pet.getAsJsonObject().get("visits").getAsJsonArray(),
                    "description", description)) {
                return true;
            }
        }
        return false;
    }
}
