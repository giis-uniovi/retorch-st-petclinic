package giis.petclinic.e2e.functional.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import giis.petclinic.e2e.functional.common.BaseLoggedClass;
import giis.petclinic.e2e.functional.common.ElementNotFoundException;
import giis.petclinic.e2e.functional.utils.Click;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TestLoadInitialScreen extends BaseLoggedClass {
	private final Logger log=LoggerFactory.getLogger(this.getClass());

	@AccessMode(resID = "dummyresource", concurrency = 50, sharing = true, accessMode = "READONLY")
	@Test
	@DisplayName("basicTest")
	void basicTest() throws ElementNotFoundException {
		log.debug("Accesing to the main page");
		Click.element(driver,waiter,driver.findElement(By.xpath("//*[@id=\"main-navbar\"]/ul/li[1]/a")));
		assertEquals(4,driver.findElements(By.className("nav-link")).size());
	}
	
}
