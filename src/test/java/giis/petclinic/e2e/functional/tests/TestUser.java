package giis.petclinic.e2e.functional.tests;

import giis.petclinic.e2e.functional.common.BaseLoggedClass;
import giis.petclinic.e2e.functional.common.ElementNotFoundException;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Tests covering the navigation structure of the PetClinic application.
 */
class TestNavigation extends BaseLoggedClass {

    @AccessMode(resID = "vet", concurrency = 1, sharing = true, accessMode = "READONLY")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "frontend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testNavigation")
    void testNavigation() throws ElementNotFoundException {
        log.debug("Test: navbar renders 4 links and each section loads correctly");

        waiter.navWait("a[title='home page']", "Application navbar did not load within timeout");
        List<WebElement> navLinks = driver.findElements(By.className("nav-link"));
        Assertions.assertEquals(4, navLinks.size(),
                "Expected 4 navigation links but found " + navLinks.size());

        navUtils.goToFindOwners(driver, waiter);
        Assertions.assertTrue(
                driver.findElement(By.cssSelector("input.form-control[placeholder='Search Filter']")).isDisplayed(),
                "Owners search filter not displayed after navigation");

        navUtils.goToRegisterOwner(driver, waiter);
        Assertions.assertTrue(
                driver.findElement(By.name("firstName")).isDisplayed(),
                "Register Owner form did not render after navigation");

        navUtils.goToVetsPage(driver, waiter);
        Assertions.assertFalse(
                driver.findElements(By.cssSelector("table.table-striped tbody tr")).isEmpty(),
                "Veterinarians table has no rows after navigation");
    }
}
