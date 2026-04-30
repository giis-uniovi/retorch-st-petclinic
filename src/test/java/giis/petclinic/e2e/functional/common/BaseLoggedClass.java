package giis.petclinic.e2e.functional.common;

import giis.selema.framework.junit5.LifecycleJunit5;
import giis.selema.manager.SeleManager;
import giis.selema.manager.SelemaConfig;
import giis.selema.services.browser.DynamicGridBrowserService;
import giis.selema.services.impl.WatermarkService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import giis.petclinic.e2e.functional.utils.Waiter;


/*
 * This class contains common set-up, tear-down, browser setup, login, and logout methods utilized across various
 * test cases within the test suite. All classes implementing test cases inherit from this class to execute
 * consistent set-up and tear-down procedures before each case. Additionally, it provides common clearance and
 * preparation methods shared among the test cases.
 */
@ExtendWith(LifecycleJunit5.class)
public class BaseLoggedClass {
    public static final Logger log = LoggerFactory.getLogger(BaseLoggedClass.class);
    protected static String sutUrl;
    protected static String tJobName = "DEFAULT_TJOB";
    protected static Properties properties;
    protected WebDriver driver;
    protected Waiter waiter;
    protected String userName;
    protected String password;
    private static final SeleManager seleManager = new SeleManager(new SelemaConfig()
            .setReportSubdir("target/containerlogs/" + (System.getProperty("TJOB_NAME") == null ? "" : System.getProperty("TJOB_NAME")))
            .setName(System.getProperty("TJOB_NAME") == null ? "locallogs" : System.getProperty("TJOB_NAME")));
    private static String dbURL;

    public static String getDbURL() {
        return dbURL;
    }

    @BeforeAll()
    static void setupAll() throws IOException { //28 lines
        log.info("Starting Global Set-up for all the Test Cases");
        properties = new Properties();
        // load a properties file for reading
        properties.load(Files.newInputStream(Paths.get("src/test/resources/test.properties")));
        // Retrieve test job name
        tJobName = System.getProperty("TJOB_NAME");
        String envUrl = System.getProperty("SUT_URL") != null ? System.getProperty("SUT_URL") : System.getenv("SUT_URL");
        if (envUrl == null) {
            // Outside CI
            sutUrl = properties.getProperty("LOCALHOST_URL");
        } else {
            sutUrl = envUrl ;

            log.debug("Configuring the browser to connect to the remote System Under Test (SUT) at the following URL: {} and the DB in {}", sutUrl, dbURL);
        }
        setupBrowser();
        log.info("Ending global setup for all test cases.");

    }

    @BeforeEach
    void setup(TestInfo testInfo) {
        log.info("Starting Individual Set-up for the test: {}.", testInfo.getDisplayName());
        // Initialize WebDriver and Waiter instances
        driver = seleManager.getDriver();
        waiter = new Waiter(driver);
        // Retrieve user credentials
        userName = properties.getProperty("USER_ESHOP");
        password = properties.getProperty("USER_ESHOP_PASSWORD");
        // Navigate to SUT URL
        log.debug("Navigating to {}.", sutUrl);
        driver.get(sutUrl);

        log.info("Individual Set-up for the TJob {} finished, starting test: {}.", tJobName, testInfo.getDisplayName());
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
        // Set up Selenoid configuration if Selenoid is present
        if (System.getenv("SELENOID_PRESENT") != null) {
            seleManager.setDriverUrl("http://selenium-hub:4444/wd/hub")
                    .add(new DynamicGridBrowserService().setVideo())
                    .add(new WatermarkService().setDelayOnFailure(3));
        }
        log.debug("Finished setting up browser ({})", browserUser);
    }


    @AfterEach
    void tearDown(TestInfo testInfo) {
        log.info("Disposing user and releasing/closing browser for the test {}", testInfo.getDisplayName());
    }

}