package giis.petclinic.e2e.functional.tests;

import giis.petclinic.e2e.functional.common.BaseLoggedClass;
import giis.petclinic.e2e.functional.common.ElementNotFoundException;
import giis.petclinic.e2e.functional.utils.Click;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

/**
 * Tests covering functionalities related to the PetClinic visits
 */
class TestVisits extends BaseLoggedClass {

    private static final String VISIT_OWNER_FIRST = "Jean";
    private static final String VISIT_OWNER_LASTNAME = "Coleman";
    private static final String VISIT_DATE = "2023-03-15";
    private static final String VISIT_DESCRIPTION = "Annual checkup";

    @AccessMode(resID = "visit", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READONLY")
    @AccessMode(resID = "pet", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "frontend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testAddVisit")
    void testAddVisit() throws ElementNotFoundException {
        log.debug("Test: schedule a visit for a pet and verify it appears in the owner details");
        createOwner(VISIT_OWNER_FIRST, VISIT_OWNER_LASTNAME, "105 N. Lake St.", "Monona", "608555172060");
        waiter.waitForOwnersListPage();
        navUtils.goToOwnerDetails( driver, waiter,VISIT_OWNER_FIRST, VISIT_OWNER_LASTNAME);
        createPet("Max", "2018-04-12", "dog");
        Click.element(driver, waiter, driver.findElement(By.linkText("Add Visit")));
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("textarea.form-control")), "Visit form not loaded");
        setDateInput(driver.findElement(By.cssSelector("input[type='date']")), VISIT_DATE);
        driver.findElement(By.cssSelector("textarea.form-control")).sendKeys(VISIT_DESCRIPTION);
        Click.element(driver, waiter, driver.findElement(By.cssSelector("button.btn-primary")));
        waiter.waitForOwnerDetailsPage();
        waiter.waitForVisitText(VISIT_DESCRIPTION, driver);
        List<WebElement> visitRows = driver.findElements(By.cssSelector("table.table-condensed tbody tr"));
        Assertions.assertTrue(
                visitRows.stream().anyMatch(row -> row.getText().contains(VISIT_DESCRIPTION) && row.getText().contains(VISIT_DATE)),
                "No visit row found with date '" + VISIT_DATE + "' and description '" + VISIT_DESCRIPTION + "'");
    }
}