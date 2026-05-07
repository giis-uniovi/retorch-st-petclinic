package giis.petclinic.e2e.functional.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class Waiter {

    private static final Logger log = LoggerFactory.getLogger(Waiter.class);
    private static final int NAV_WAIT_SECONDS = 30;
    private static final int WAIT_DEFAULT = 10;
    private final WebDriverWait waiter;
    private final WebDriverWait navWaiter;

    public Waiter(WebDriver driver) {
        waiter = new WebDriverWait(driver, Duration.ofSeconds(WAIT_DEFAULT));
        navWaiter = new WebDriverWait(driver, Duration.ofSeconds(NAV_WAIT_SECONDS));
    }

    public void waitUntil(ExpectedCondition <?> condition, String errorMessage) {
        try {
            this.waiter.until(condition);
        } catch (org.openqa.selenium.TimeoutException timeout) {
            log.error(errorMessage);
            throw new org.openqa.selenium.TimeoutException("\"" + errorMessage + "\" (checked with condition) > " + timeout.getMessage());
        }
    }

    public void navWait(String cssSelector, String errorMessage) {
        try {
            navWaiter
                    .until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector)));
        } catch (org.openqa.selenium.TimeoutException e) {
            throw new org.openqa.selenium.TimeoutException(
                    "\"" + errorMessage + "\" > " + e.getMessage());
        }
    }

    public void waitForHomePage() {
        log.debug("Waiting for home page to load");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.img-responsive")),
                "Home page did not load");
    }

    public void waitForOwnersListPage() {
        log.debug("Waiting for owner list to load");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input.form-control[placeholder='Search Filter']")),
                "Owners list page did not load");
    }

    public void waitForRegisterOwnerPage() {
        log.debug("Waiting for register owner page to load");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.name("firstName")),
                "Register owner page did not load");
    }

    public void waitForOwnerDetailsPage() {
        log.debug("Waiting for owner details page to load");
        waitUntil(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href*='pets/new']")),
                "Owner details page did not load");
    }

    public void waitForVetsPage() {
        log.debug("Waiting for veterinarians page to load");
        waitUntil(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector("table.table-striped tbody tr"), 0),
                "Veterinarians page did not load");
    }

    /**
     * Checks whether a given text appears inside the condensed visits table on the
     * owner details page, retrying with a full page refresh up to {@code maxRetries}
     * times.
     */
    public void waitForVisitText(String expectedText, WebDriver driver) {
        log.debug("Waiting for visit text {} in the visits table", expectedText);
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                this.waitUntil(ExpectedConditions.textToBePresentInElementLocated(
                        By.cssSelector("table.table-condensed"), expectedText), "Visit text '" + expectedText + "' not yet visible in visits table");
                return;
            } catch (org.openqa.selenium.TimeoutException e) {
                if (attempt < maxRetries) {
                    log.debug("Visit text '{}' not found (attempt {}), refreshing owner details...",
                            expectedText, attempt);
                    driver.navigate().refresh();
                    waitForOwnerDetailsPage();
                } else {
                    throw new org.openqa.selenium.TimeoutException(
                            "\"Visit '" + expectedText + "' not found after " + maxRetries
                                    + " retries\" > " + e.getMessage());
                }
            }
        }
    }
}
