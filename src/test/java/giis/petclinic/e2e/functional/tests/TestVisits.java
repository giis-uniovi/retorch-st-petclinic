package giis.petclinic.e2e.functional.tests;

import giis.petclinic.e2e.functional.common.BaseLoggedClass;
import giis.petclinic.e2e.functional.common.ElementNotFoundException;
import giis.petclinic.e2e.functional.utils.Click;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Tests covering functionalities related to the PetClinic visits
 */
class TestVisits extends BaseLoggedClass {

    @AccessMode(resID = "visit", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "frontend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testAddVisit")
    void testAddVisit() throws ElementNotFoundException {
        log.debug("Test: schedule a visit for a pet and verify it appears in the owner details");
        createOwner("Jean", "Coleman", "105 N. Lake St.", "Monona", "608555172060");
        waiter.waitForOwnersListPage();
        navUtils.goToOwnerDetails("Jean", "Coleman", driver, waiter);
        createPet("Max", "2018-04-12", "dog");
        Click.element(driver, waiter, driver.findElement(By.linkText("Add Visit")));
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("textarea.form-control")), "Visit form not loaded");
        setDateInput(driver.findElement(By.cssSelector("input[type='date']")), "2023-03-15");
        driver.findElement(By.cssSelector("textarea.form-control")).sendKeys("Annual checkup");
        Click.element(driver, waiter, driver.findElement(By.cssSelector("button.btn-primary")));
        waiter.waitForOwnerDetailsPage();
        waiter.waitForVisitText("Annual checkup", driver, waiter);
        Assertions.assertTrue(driver.findElement(By.cssSelector("table.table-condensed")).getText().contains("Annual checkup"), "Visit 'Annual checkup' not found in the pet visits table on owner details");
    }
}