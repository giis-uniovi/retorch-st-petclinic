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
 * Validates the customers-service pet endpoints exposed through the API Gateway:
 * <ul>
 *   <li>GET  /api/customer/petTypes                            — list all pet types</li>
 *   <li>POST /api/customer/owners/{ownerId}/pets               — add a pet to an owner (HTTP 201)</li>
 *   <li>GET  /api/customer/owners/{ownerId}/pets/{petId}       — retrieve a pet by ID</li>
 *   <li>PUT  /api/customer/owners/{ownerId}/pets/{petId}       — update a pet (HTTP 204)</li>
 * </ul>
 */
class TestApiPets extends BaseApiClass {

    /** Seeded pet type IDs in the customers-service database. */
    private static final int PET_TYPE_CAT = 1;
    private static final int PET_TYPE_DOG = 2;
    private static final int EXPECTED_PET_TYPE_COUNT = 6;

    private static final String PET_TYPES_PATH = "/petTypes";

    @AccessMode(resID = "pet", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPIGetPetTypes")
    void testAPIGetPetTypes() throws IOException {
        Assertions.assertEquals(200, getStatus(customerUrl(PET_TYPES_PATH)), "Expected HTTP 200");

        JsonArray types = getJsonArray(customerUrl(PET_TYPES_PATH));
        Assertions.assertEquals(EXPECTED_PET_TYPE_COUNT, types.size(),
                "Expected exactly " + EXPECTED_PET_TYPE_COUNT + " pet types (cat, dog, lizard, snake, bird, hamster)");

        Assertions.assertAll(
                () -> Assertions.assertTrue(containsByField(types, "name", "cat"), "Pet types must include 'cat'"),
                () -> Assertions.assertTrue(containsByField(types, "name", "dog"), "Pet types must include 'dog'")
        );
    }

    @AccessMode(resID = "pet", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPICreatePet")
    void testAPICreatePet() throws IOException {
        String petName = "Whiskers" + unique();
        int ownerId = createOwner("PetCreate");

        int status = postStatus(customerUrl("/owners/" + ownerId + "/pets"),
                petPayload(petName, "2019-06-01", PET_TYPE_CAT));
        Assertions.assertEquals(201, status, "Creating a pet must return HTTP 201");

        int petId = createPet(ownerId, petName, "2019-06-01", PET_TYPE_CAT);
        Assertions.assertTrue(petId > 0, "Created pet must have a positive ID");

        JsonObject pet = getJsonObject(customerUrl("/owners/" + ownerId + "/pets/" + petId));
        Assertions.assertAll(
                () -> Assertions.assertEquals(petId,   pet.get("id").getAsInt(),      "Pet ID must match"),
                () -> Assertions.assertEquals(petName, pet.get("name").getAsString(), "Pet name must match")
        );
    }

    @AccessMode(resID = "pet", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPIUpdatePet")
    void testAPIUpdatePet() throws IOException {
        long ts = unique();
        int ownerId = createOwner("PetUpdate");
        int petId = createPet(ownerId, "OldName" + ts, "2021-03-10", PET_TYPE_DOG);

        String updatedName = "NewName" + ts;
        int putStatus = put(customerUrl("/owners/" + ownerId + "/pets/" + petId),
                petPayload(petId, updatedName, "2021-03-10", PET_TYPE_DOG));
        Assertions.assertEquals(204, putStatus, "Updating a pet must return HTTP 204");

        JsonObject pet = getJsonObject(customerUrl("/owners/" + ownerId + "/pets/" + petId));
        Assertions.assertEquals(updatedName, pet.get("name").getAsString(), "Pet name must be updated");
    }
}
