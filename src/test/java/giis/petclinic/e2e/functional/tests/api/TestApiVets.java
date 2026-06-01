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
 * Validates the vets-service endpoint exposed through the API Gateway:
 * {@code GET /api/vet/vets} — full vet list with specialties.
 */
class TestApiVets extends BaseApiClass {

    private static final String VETS_PATH = "/vets";

    @AccessMode(resID = "vet", concurrency = 10, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "gateway", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("TestAPIGetVetList")
    void testAPIGetVetList() throws IOException {
        Assertions.assertEquals(200, getStatus(vetUrl(VETS_PATH)), "Expected HTTP 200");

        JsonArray vets = getJsonArray(vetUrl(VETS_PATH));
        Assertions.assertFalse(vets.isEmpty(), "Expected at least one vet in the list");

        JsonObject first = vets.get(0).getAsJsonObject();
        Assertions.assertAll(
                () -> Assertions.assertTrue(first.has("id"),          "Vet entry must have 'id'"),
                () -> Assertions.assertTrue(first.has("firstName"),   "Vet entry must have 'firstName'"),
                () -> Assertions.assertTrue(first.has("lastName"),    "Vet entry must have 'lastName'"),
                () -> Assertions.assertTrue(first.has("specialties"), "Vet entry must have 'specialties'")
        );

        boolean found = false;
        for (int i = 0; i < vets.size(); i++) {
            JsonObject vet = vets.get(i).getAsJsonObject();
            if ("James".equals(vet.get("firstName").getAsString())
                    && "Carter".equals(vet.get("lastName").getAsString())) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Seeded vet 'James Carter' not found in the vet list");
    }
}
