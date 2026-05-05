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
 * This test class provides the test methods that validate the functionalities of PetClinic related
 * to the Pet owners of the clinic
 */
class TestOwners extends BaseLoggedClass {

    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "DYNAMIC")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "frontend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testOwnerCreationAndSearch")
    void testOwnerCreationAndSearch() throws ElementNotFoundException {
        log.debug("Test: create owner and locate it in the owners list");
        createOwner("George", "Franklin", "110 W. Liberty St.", "Madison", "608555102030");
        waiter.waitForOwnersListPage();
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.linkText("George Franklin")), "Owner 'George Franklin' not found in the owners list after creation");
        List<WebElement> matches = driver.findElements(By.linkText("George Franklin"));
        Assertions.assertFalse(matches.isEmpty(), "Expected at least one link for 'George Franklin' in the list");
        WebElement searchInput = driver.findElement(By.cssSelector("input.form-control[placeholder='Search Filter']"));
        searchInput.sendKeys("Franklin");
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText("Franklin")), "Owner 'Franklin' not visible after applying search filter");
    }

    @AccessMode(resID = "owner", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "frontend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testOwnerEdit")
    void testOwnerEdit() throws ElementNotFoundException {
        log.debug("Starting test that check the user edition");
        createOwner("Betty", "Davis", "638 Cardinal Ave.", "Sun Prairie", "608555174020");
        waiter.waitForOwnersListPage();
        navUtils.goToOwnerDetails("Betty", "Davis", driver, waiter);
        Click.element(driver, waiter, driver.findElement(By.linkText("Edit Owner")));
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.name("telephone")), "Edit owner form not loaded");
        WebElement telephoneField = driver.findElement(By.name("telephone"));
        telephoneField.clear();
        telephoneField.sendKeys("608555174099");
        Click.element(driver, waiter, driver.findElement(By.cssSelector("button[type='submit']")));
        waiter.waitForOwnerDetailsPage();
        waiter.waitUntil(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("table.table-striped"), "608555174099"), "Updated telephone '608555174099' not displayed after editing the owner");
        Assertions.assertTrue(driver.findElement(By.cssSelector("table.table-striped")).getText().contains("608555174099"), "Updated telephone '608555174099' not found in owner details table");
    }
}