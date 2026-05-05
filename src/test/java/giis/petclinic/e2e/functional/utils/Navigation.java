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

    public static final Logger log = LoggerFactory.getLogger(Navigation.class);

    public void goToHomePage(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to Home");
        waiter.navWait(driver, "a[title='home page']", "Home nav link not clickable");
        Click.element(driver, waiter, driver.findElement(By.cssSelector("a[title='home page']")));
    }

    public void goToFindOwners(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to Find Owners page");
        waiter.navWait(driver, "a[title='find owners']", "Find Owners nav link not clickable");
        Click.element(driver, waiter, driver.findElement(By.cssSelector("a[title='find owners']")));
    }

    public void goToRegisterOwner(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to Register Owner page");
        waiter.navWait(driver, "a[title='register owner']", "Register Owner nav link not clickable");
        Click.element(driver, waiter, driver.findElement(By.cssSelector("a[title='register owner']")));
    }

    public void goToVetsPage(WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to Veterinarians page");
        waiter.navWait(driver, "a[title='veterinarians']", "Veterinarians nav link not clickable");
        Click.element(driver, waiter, driver.findElement(By.cssSelector("a[title='veterinarians']")));
    }

    public void goToOwnerDetails(String firstName, String lastName, WebDriver driver, Waiter waiter) throws ElementNotFoundException {
        log.debug("Navigating to owner {} details page", firstName + " " + lastName);
        String fullName = firstName + " " + lastName;
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.linkText(fullName)),
                "Owner link '" + fullName + "' not visible in the owners list");
        Click.element(driver, waiter, driver.findElement(By.linkText(fullName)));
        waiter.waitForOwnerDetailsPage();
    }
}
