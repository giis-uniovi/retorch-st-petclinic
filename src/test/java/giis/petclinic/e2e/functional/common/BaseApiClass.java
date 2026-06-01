package giis.petclinic.e2e.functional.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Base class for PetClinic API test suite. Provides the necessary support methods to
 * interact with the API endpoints, create the API payloads and validate the results.
 * Do not require authentification because PetClinic expose all endpoints publicly.
 */
public class BaseApiClass {

    protected static final Logger log = LoggerFactory.getLogger(BaseApiClass.class);

    //Phone numbers in PetClinic must fit within 10 digits.
    private static final long PHONE_MODULUS = 10_000_000_000L;

    protected static CloseableHttpClient httpClient;
    protected static String sutUrl;
    protected static Properties properties;
    protected static String tJobName;

    @BeforeAll
    static void setupAll() throws IOException {
        log.info("Starting API test global setup");
        properties = new Properties();
        properties.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));
        tJobName = System.getProperty("TJOB_NAME");
        String envUrl = System.getProperty("SUT_URL") != null
                ? System.getProperty("SUT_URL")
                : System.getenv("SUT_URL");
        sutUrl = envUrl != null ? envUrl : properties.getProperty("LOCALHOST_URL");
        log.info("API base URL: {}", sutUrl);
        httpClient = HttpClients.createDefault();
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        if (httpClient != null) {
            httpClient.close();
            log.info("Shared HTTP client closed");
        }
    }

    //Support methods to get URIS and URLs of the system.
    protected String vetUrl(String path) {return sutUrl + "/api/vet" + path;}
    protected String customerUrl(String path) {return sutUrl + "/api/customer" + path;}
    protected String visitUrl(String path) {return sutUrl + "/api/visit" + path;}
    protected String gatewayUrl(String path) {return sutUrl + "/api/gateway" + path;}
    protected String visitsPath(int ownerId, int petId) {return "/owners/" + ownerId + "/pets/" + petId + "/visits";}

    /**
     * Support method used to perform an HTTP GET request to a certain url.
     */
    protected String get(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        request.addHeader("Accept", "application/json");
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String body = entity != null ? EntityUtils.toString(entity) : "";
        log.debug("GET {} -> {} ({} chars)", url, response.getStatusLine().getStatusCode(), body.length());
        return body;
    }

    protected int getStatus(String url) throws IOException {
        return statusOf(new HttpGet(url));
    }

    /**
     * Support method used to perform an HTTP POST request in the test cases
     * to a certain url.
     */
    protected String post(String url, String jsonBody) throws IOException {
        HttpResponse response = httpClient.execute(buildPost(url, jsonBody));
        HttpEntity entity = response.getEntity();
        String body = entity != null ? EntityUtils.toString(entity) : "";
        log.debug("POST {} -> {} ({} chars)", url, response.getStatusLine().getStatusCode(), body.length());
        return body;
    }

    /**
     * Support method used to perform an HTTP POST request in the test cases
     * to a certain url, and get the STATUS code.
     */
    protected int postStatus(String url, String jsonBody) throws IOException {
        return statusOf(buildPost(url, jsonBody));
    }

    /**
     * Support method used to perform an HTTP PUT in the test cases and retrieve the status code.
     */
    protected int put(String url, String jsonBody) throws IOException {
        return statusOf(buildPut(url, jsonBody));
    }

    /**
     * Support method used to perform an HTTP PUT and retrieve the status code.
     */
    private static HttpPost buildPost(String url, String jsonBody) {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        request.addHeader("Accept", "application/json");
        return request;
    }

    /**
     * HTTP PUT builder, using as content JSON
     */
    private static HttpPut buildPut(String url, String jsonBody) {
        HttpPut request = new HttpPut(url);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        return request;
    }

    /**
     * Support method to retrieve the status code of a generic HTTP request.
     */
    private int statusOf(HttpUriRequest request) throws IOException {
        int status = httpClient.execute(request).getStatusLine().getStatusCode();
        log.debug("{} {} -> {}", request.getMethod(), request.getURI(), status);
        return status;
    }

    /**
     * Support method used to retrieve a JSON object from a given URL. .
     */
    protected JsonObject getJsonObject(String url) throws IOException {
        return JsonParser.parseString(get(url)).getAsJsonObject();
    }

    /**
     * Support method used to retrieve a JSON array from a given URL. .
     */
    protected JsonArray getJsonArray(String url) throws IOException {
        return JsonParser.parseString(get(url)).getAsJsonArray();
    }

    /**
     * Returns {@code true} if any element of {@code array} is an object whose
     * {@code fieldName} property equals {@code expected}.
     */
    protected static boolean containsByField(JsonArray array, String fieldName, String expected) {
        for (JsonElement element : array) {
            if (element.isJsonObject()
                    && expected.equals(element.getAsJsonObject().get(fieldName).getAsString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a process-monotonic timestamp suitable as a uniqueness suffix.
     */
    protected static long unique() {
        return System.currentTimeMillis();
    }

    /**
     * Formats a timestamp as a 10-digit phone number string.
     */
    protected static String phone(long ts) {
        return String.valueOf(ts % PHONE_MODULUS);
    }

    /**
     * Support method that given the owner data (name, lastname, addres, city and telephone)
     * creates the proper JSON payload to query the owner API.
     */
    protected static String ownerPayload(String firstName, String lastName,
                                         String address, String city, String telephone) {
        JsonObject json = new JsonObject();
        json.addProperty("firstName", firstName);
        json.addProperty("lastName", lastName);
        json.addProperty("address", address);
        json.addProperty("city", city);
        json.addProperty("telephone", telephone);
        return json.toString();
    }

    /**
     * Support method that given the pet data (name, birthdate, typeID)
     * creates the proper JSON payload to query the  pet API.
     */
    protected static String petPayload(String name, String birthDate, int typeId) {
        return petPayload(0, name, birthDate, typeId);
    }

    /**
     * Support method that given the pet data (id, name, birthdate, typeID)
     * creates the proper JSON payload to query a UPDATE the  pet API.
     */
    protected static String petPayload(int id, String name, String birthDate, int typeId) {
        JsonObject json = new JsonObject();
        if (id > 0) json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("birthDate", birthDate);
        json.addProperty("typeId", typeId);
        return json.toString();
    }

    /**
     * Support method that given the visit data (visitData and description)
     * creates the proper JSON payload to query a query the visit API.
     */
    protected static String visitPayload(String visitDate, String description) {
        JsonObject json = new JsonObject();
        json.addProperty("visitDate", visitDate);
        json.addProperty("description", description);
        return json.toString();
    }


    /**
     * Support method that given the owner data (firstname,lastname,addres, city and telephone)
     * creates an owner via POST {@code /api/customer/owners} and returns the assigned ID.
     */
    protected int createOwner(String firstName, String lastName, String address,
                              String city, String telephone) throws IOException {
        String response = post(customerUrl("/owners"),
                ownerPayload(firstName, lastName, address, city, telephone));
        int id = JsonParser.parseString(response).getAsJsonObject().get("id").getAsInt();
        log.debug("Created owner id={} ({} {})", id, firstName, lastName);
        return id;
    }

    /**
     * Convenience fixture: creates an owner with sensible defaults derived from {@code label}
     * and a fresh {@link #unique() unique suffix}. Use this when the test does not need to
     * assert against specific owner field values.
     */
    protected int createOwner(String label) throws IOException {
        long ts = unique();
        return createOwner(label, "Owner" + ts,
                "1 " + label + " St.", label + "City", phone(ts));
    }

    /**
     * Support method that given a pet data (name, birthdate, and typeID)
     * creates a pet under {@code ownerId} via POST and returns the assigned pet ID.
     *
     * @param typeId pet type (1=cat, 2=dog, 3=lizard, 4=snake, 5=bird, 6=hamster)
     */
    protected int createPet(int ownerId, String name, String birthDate, int typeId) throws IOException {
        String response = post(customerUrl("/owners/" + ownerId + "/pets"),
                petPayload(name, birthDate, typeId));
        int id = JsonParser.parseString(response).getAsJsonObject().get("id").getAsInt();
        log.debug("Created pet id={} ({}) for owner {}", id, name, ownerId);
        return id;
    }

    /**
     * Support method for given a date and description, creates a visit for {@code petId}
     * and the owner with id {@code ownerId} via POST and returns the raw response body.
     */
    protected String createVisit(int ownerId, int petId, String date, String description) throws IOException {
        String response = post(visitUrl("/owners/" + ownerId + "/pets/" + petId + "/visits"),
                visitPayload(date, description));
        log.debug("Created visit for pet {}: '{}'", petId, description);
        return response;
    }
}
