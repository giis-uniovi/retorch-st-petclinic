package giis.petclinic.e2e.functional.tests;

import giis.petclinic.e2e.functional.common.BaseLoggedClass;
import giis.petclinic.e2e.functional.common.ElementNotFoundException;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/**
 * Tests covering pet registration under an owner.
 */
class TestPets extends BaseLoggedClass {

    @AccessMode(resID = "pet", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "web-browser", concurrency = 1, sharing = false, accessMode = "READWRITE")
    @AccessMode(resID = "frontend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("testAddPet")
    void testAddPet() throws ElementNotFoundException {
        log.debug("Starting Test: add a pet to an owner and verify it appears in the owner details");
        createOwner("Harold", "Davis", "563 Friendly St.", "Windsor", "608555882020");
        waiter.waitForOwnersListPage();
        navUtils.goToOwnerDetails("Harold", "Davis", driver, waiter);
        createPet("Leo", "2015-09-07", "cat");
        Assertions.assertFalse(driver.findElements(By.linkText("Leo")).isEmpty(),
                "Pet 'Leo' not found in owner details");
    }
}