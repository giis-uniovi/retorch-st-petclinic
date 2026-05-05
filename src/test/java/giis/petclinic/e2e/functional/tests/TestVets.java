package giis.petclinic.e2e.functional.tests;

import giis.petclinic.e2e.functional.common.BaseLoggedClass;
import giis.petclinic.e2e.functional.common.ElementNotFoundException;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;


/**
 * Tests covering the PetClinic veterinarians functionalities.
 */
class TestVets extends BaseLoggedClass {

    @AccessMode(resID = "vet", concurrency = 1, sharing = false, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "frontend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testVetList")
    void testVetList() throws ElementNotFoundException {
        log.debug("Test: the veterinarians list is populated");
        navUtils.goToVetsPage(driver, waiter);
        waiter.waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("table.table-striped tbody tr"),
                0), "No veterinarian data rows loaded");
        List<WebElement> vetRows = driver.findElements(By.cssSelector("table.table-striped tbody tr"));
        Assertions.assertFalse(vetRows.isEmpty(), "Expected at least one veterinarian in the table but found none");
    }
}
