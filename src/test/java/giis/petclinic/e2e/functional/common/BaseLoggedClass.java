package giis.petclinic.e2e.functional.common;

import giis.petclinic.e2e.functional.utils.Click;
import giis.petclinic.e2e.functional.utils.Navigation;
import giis.petclinic.e2e.functional.utils.Waiter;
import giis.selema.framework.junit5.LifecycleJunit5;
import giis.selema.manager.SeleManager;
import giis.selema.manager.SelemaConfig;
import giis.selema.services.browser.DynamicGridBrowserService;
import giis.selema.services.impl.WatermarkService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;


/*
 * This class contains common set-up, tear-down, browser setup, login, and logout methods utilized across various
 * test cases within the test suite. All classes implementing test cases inherit from this class to execute
 * consistent set-up and tear-down procedures before each case. Additionally, it provides common clearance and
 * preparation methods shared among the test cases.
 */
@ExtendWith(LifecycleJunit5.class)
public class BaseLoggedClass {

    public static final Logger log = LoggerFactory.getLogger(BaseLoggedClass.class);

    private static final SeleManager seleManager = new SeleManager(new SelemaConfig()
            .setReportSubdir("target/containerlogs/" + (System.getProperty("TJOB_NAME") == null ? "" : System.getProperty("TJOB_NAME")))
            .setName(System.getProperty("TJOB_NAME") == null ? "locallogs" : System.getProperty("TJOB_NAME")));
    protected static String sutUrl;
    protected static String tJobName = "DEFAULT_TJOB";
    protected static Properties properties;
    protected WebDriver driver;
    protected Waiter waiter;
    protected Navigation navUtils;


    @BeforeAll()
    static void setupAll() throws IOException { //28 lines
        log.info("Starting Global Set-up for all the Test Cases");
        properties = new Properties();
        properties.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));
        // Retrieve test job name
        tJobName = System.getProperty("TJOB_NAME");
        String envUrl = System.getProperty("SUT_URL") != null ? System.getProperty("SUT_URL") : System.getenv("SUT_URL");
        if (envUrl == null) {
            log.info("Configuring webdriver for local execution");
            sutUrl = properties.getProperty("LOCALHOST_URL");
        } else {
            sutUrl = envUrl;

            log.debug("Configuring the browser to connect to the remote System Under Test (SUT) at the following URL: {}", sutUrl);
        }
        setupBrowser();
        log.info("Ending global setup for all test cases.");

    }

    /**
     * Configures and initializes the browser for testing.
     * <p>
     * The method sets up the browser using SeleManager with necessary arguments if, SELENOID_PRESENT is not set.
     * If Selenoid is present, it  configures the Selenoid service for video recording and VNC support.
     * </p>
     */
    protected static void setupBrowser() {
        String browserUser = properties.getProperty("BROWSER_USER");
        log.debug("Starting browser ({})", browserUser);
        // Setting up browser using selema with the necessary Arguments.
        seleManager.setBrowser("chrome").setArguments(new String[]{"--start-maximized", "--incognito"});
        if (System.getenv("SELENOID_PRESENT") != null) {
            log.debug("Setting up Selenium WebDriver with Selenium-hub");
            seleManager.setDriverUrl("http://selenium-hub:4444/wd/hub")
                    .add(new DynamicGridBrowserService().setVideo())
                    .add(new WatermarkService().setDelayOnFailure(3));
        }
        log.debug("Finished setting up browser ({})", browserUser);
    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        log.info("Starting Individual Set-up for the test: {}.", testInfo.getDisplayName());

        driver = seleManager.getDriver();
        waiter = new Waiter(driver);
        navUtils = new Navigation();
        log.debug("Navigating to {}.", sutUrl);
        driver.get(sutUrl);

        log.info("Individual Set-up for the TJob {} finished, starting test: {}.", tJobName, testInfo.getDisplayName());
    }

    /**
     * Registers a new owner via the form fulfilling all the necessary fields, requires
     * the all the owner data.
     */
    protected void createOwner(String firstName, String lastName, String address,
                               String city, String telephone) throws ElementNotFoundException {
        log.debug("Creating a owner with name: {}, last name: {}, address: {}, city: {} and telephone: {}", firstName, lastName, address, city, telephone);
        navUtils.goToRegisterOwner(driver, waiter);
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.name("firstName")),
                "Owner registration form not loaded");
        log.debug("Filling the form fields to register owner");
        fillField("firstName", firstName);
        fillField("lastName", lastName);
        fillField("address", address);
        fillField("city", city);
        fillField("telephone", telephone);
        log.debug("Clicking on register button");
        Click.element(driver, waiter, driver.findElement(By.cssSelector("button[type='submit']")));
    }

    protected void createPet(String name, String birthDate, String type) throws ElementNotFoundException {
        log.debug("Creating a pet with name: {}, birthDate: {} and type: {}", name, birthDate, type);
        Click.element(driver, waiter, driver.findElement(By.linkText("Add New Pet")));
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.name("name")),
                "Pet form not loaded");
        driver.findElement(By.name("name")).sendKeys(name);
        setDateInput(driver.findElement(By.cssSelector("input[type='date']")), birthDate);
        waiter.waitUntil(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.cssSelector("select.form-control option")),
                "Pet type options not loaded");
        new Select(driver.findElement(By.cssSelector("select.form-control")))
                .selectByVisibleText(type);
        Click.element(driver, waiter, driver.findElement(By.cssSelector("button[type='submit']")));

        waiter.waitForOwnerDetailsPage();
        waiter.waitUntil(ExpectedConditions.visibilityOfElementLocated(By.linkText(name)),
                "Pet " + name + " not visible in owner details after creation");
    }

    protected void fillField(String fieldName, String value) {
        log.debug("Filling field {} with value {}", fieldName, value);
        WebElement field = driver.findElement(By.name(fieldName));
        field.clear();
        field.sendKeys(value);
    }

    /**
     * Sets a date input value via JavaScript and fires the AngularJS input event
     * so the ng-model binding is updated.
     */
    protected void setDateInput(WebElement dateField, String isoDate) {
        log.debug("Setting date field {} to {}", isoDate, dateField);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = arguments[1]", dateField, isoDate);
        js.executeScript("angular.element(arguments[0]).triggerHandler('input')", dateField);
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        log.info("Disposing user and releasing/closing browser for the test {}", testInfo.getDisplayName());
    }

}