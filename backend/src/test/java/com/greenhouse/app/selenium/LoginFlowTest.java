package com.greenhouse.app.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Selenium end-to-end test for the login page of the Greenhouse frontend.
 *
 * <p>This test requires:
 * <ul>
 *   <li>The Vue frontend running at {@code http://localhost:5173}.</li>
 *   <li>Chrome installed (WebDriverManager auto-downloads ChromeDriver).</li>
 * </ul>
 * The test is tagged {@code @Tag("e2e")} and is excluded from the default
 * Maven Surefire run ({@code -P e2e} to include it).</p>
 */
@Tag("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginFlowTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "http://localhost:5173";

    /**
     * Sets up ChromeDriver before all tests in this class.
     */
    @BeforeAll
    static void setUpDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Tears down the WebDriver after all tests.
     */
    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Verifies that the login page loads and the Google login button is visible.
     */
    @Test
    @Order(1)
    @DisplayName("Login page displays Google OAuth button")
    void loginPage_displaysGoogleButton() {
        driver.get(BASE_URL + "/login");

        WebElement googleButton = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("[data-testid='google-login-btn']")
                )
        );

        assertThat(googleButton.isDisplayed()).isTrue();
        assertThat(googleButton.getText()).containsIgnoringCase("Google");
    }

    /**
     * Verifies that clicking the Google button navigates to the OAuth2 authorization URL.
     */
    @Test
    @Order(2)
    @DisplayName("Clicking Google button redirects to OAuth2 provider")
    void googleButton_click_redirectsToOAuth() {
        driver.get(BASE_URL + "/login");

        WebElement googleButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("[data-testid='google-login-btn']")
                )
        );
        googleButton.click();

        wait.until(ExpectedConditions.urlContains("accounts.google.com"));
        assertThat(driver.getCurrentUrl()).contains("accounts.google.com");
    }

    /**
     * Verifies that the page title matches the application name.
     */
    @Test
    @Order(3)
    @DisplayName("Login page has correct title")
    void loginPage_hasCorrectTitle() {
        driver.get(BASE_URL + "/login");

        wait.until(ExpectedConditions.titleContains("Greenhouse"));
        assertThat(driver.getTitle()).containsIgnoringCase("Greenhouse");
    }
}
