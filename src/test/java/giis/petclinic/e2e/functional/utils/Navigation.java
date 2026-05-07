package giis.petclinic.e2e.functional.utils;

import giis.petclinic.e2e.functional.common.ElementNotFoundException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Navigation helpers for moving between the main sections of the PetClinic application.
 * Each navigation action employs a custom waiter that is short that the used in the common actions.
 */
public class Navigation {

    private static final Logger log = LoggerFactory.getLogger(Navigation.class);

    public void goToHomePage(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to Home");
        waiter.navWait("a[title='home page']", "Home nav link not clickable");
        Click.element(driver, waiter, driver.findElement(By.cssSelector("a[title='home page']")));
        waiter.waitForHomePage();
    }

    public void goToFindOwners(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to Find Owners page");
        waiter.navWait("a[title='find owners']", "Find Owners nav link not clickable");
        Click.element(driver, waiter, driver.findElement(By.cssSelector("a[title='find owners']")));
        waiter.waitForOwnersListPage();
    }

    public void goToRegisterOwner(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to Register Owner page");
        waiter.navWait("a[title='register owner']", "Register Owner nav link not clickable");
        Click.element(driver, waiter, driver.findElement(By.cssSelector("a[title='register owner']")));
        waiter.waitForRegisterOwnerPage();
    }

    public void goToVetsPage(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to Veterinarians page");
        waiter.navWait("a[title='veterinarians']", "Veterinarians nav link not clickable");
        Click.element(driver, waiter, driver.findElement(By.cssSelector("a[title='veterinarians']")));
        waiter.waitForVetsPage();
    }

    public void goToOwnerDetails( WebDriver driver, Waiter waiter,String firstName, String lastName) throws ElementNotFoundException {
        log.debug("Navigating to owner {} {} details page", firstName, lastName);
        String fullName = firstName + " " + lastName;
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.linkText(fullName)),
                "Owner link '" + fullName + "' not visible in the owners list");
        Click.element(driver, waiter, driver.findElement(By.linkText(fullName)));
        waiter.waitForOwnerDetailsPage();
    }
}
