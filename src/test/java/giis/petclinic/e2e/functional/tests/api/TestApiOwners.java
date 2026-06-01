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
 * Validates the customers-service owner endpoints exposed through the API Gateway:
 * <ul>
 *   <li>GET  /api/customer/owners       — list all owners</li>
 *   <li>POST /api/customer/owners       — create a new owner (HTTP 201)</li>
 *   <li>GET  /api/customer/owners/{id}  — get a single owner by ID</li>
 *   <li>PUT  /api/customer/owners/{id}  — update an owner (HTTP 204)</li>
 * </ul>
 */
class TestApiOwners extends BaseApiClass {

    private static final String OWNERS_PATH = "/owners";

    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READONLY")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPIListOwners")
    void testAPIListOwners() throws IOException {
        Assertions.assertEquals(200, getStatus(customerUrl(OWNERS_PATH)), "Expected HTTP 200");

        JsonArray owners = getJsonArray(customerUrl(OWNERS_PATH));
        Assertions.assertFalse(owners.isEmpty(), "Owner list must not be empty");

        JsonObject first = owners.get(0).getAsJsonObject();
        Assertions.assertAll(
                () -> Assertions.assertTrue(first.has("id"),        "Owner must have 'id'"),
                () -> Assertions.assertTrue(first.has("firstName"), "Owner must have 'firstName'"),
                () -> Assertions.assertTrue(first.has("lastName"),  "Owner must have 'lastName'"),
                () -> Assertions.assertTrue(first.has("address"),   "Owner must have 'address'"),
                () -> Assertions.assertTrue(first.has("city"),      "Owner must have 'city'"),
                () -> Assertions.assertTrue(first.has("telephone"), "Owner must have 'telephone'")
        );
    }

    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPICreateOwner")
    void testAPICreateOwner() throws IOException {
        long ts = unique();
        String firstName = "ApiTest";
        String lastName = "Owner" + ts;
        String address = "1 API St.";
        String city = "TestCity";
        String telephone = phone(ts);

        int status = postStatus(customerUrl(OWNERS_PATH),
                ownerPayload(firstName, lastName, address, city, telephone));
        Assertions.assertEquals(201, status, "Creating an owner must return HTTP 201");

        int ownerId = createOwner(firstName, lastName, address, city, telephone);
        Assertions.assertTrue(ownerId > 0, "Created owner must have a positive ID");

        JsonObject owner = getJsonObject(customerUrl(OWNERS_PATH + "/" + ownerId));
        Assertions.assertAll(
                () -> Assertions.assertEquals(ownerId,   owner.get("id").getAsInt(),           "ID must match"),
                () -> Assertions.assertEquals(firstName, owner.get("firstName").getAsString(), "firstName must match"),
                () -> Assertions.assertEquals(lastName,  owner.get("lastName").getAsString(),  "lastName must match"),
                () -> Assertions.assertEquals(address,   owner.get("address").getAsString(),   "address must match"),
                () -> Assertions.assertEquals(city,      owner.get("city").getAsString(),      "city must match"),
                () -> Assertions.assertEquals(telephone, owner.get("telephone").getAsString(), "telephone must match")
        );
    }

    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPIUpdateOwner")
    void testAPIUpdateOwner() throws IOException {
        long ts = unique();
        int ownerId = createOwner("Update", "Me" + ts, "3 Update Rd.", "OldCity", phone(ts));

        String newTelephone = phone(ts + 1);
        int putStatus = put(customerUrl(OWNERS_PATH + "/" + ownerId),
                ownerPayload("Update", "Me" + ts, "3 Update Rd.", "NewCity", newTelephone));
        Assertions.assertEquals(204, putStatus, "Updating an owner must return HTTP 204");

        JsonObject owner = getJsonObject(customerUrl(OWNERS_PATH + "/" + ownerId));
        Assertions.assertAll(
                () -> Assertions.assertEquals("NewCity",     owner.get("city").getAsString(),      "city must be updated"),
                () -> Assertions.assertEquals(newTelephone, owner.get("telephone").getAsString(), "telephone must be updated")
        );
    }
}
