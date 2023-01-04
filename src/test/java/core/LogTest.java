package core;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;

public class LogTest {

    private final Supplier<ConditionFactory> WAITER = () -> Awaitility.given()
            .ignoreExceptions()
            .pollInterval(3, TimeUnit.SECONDS)
            .await()
            .dontCatchUncaughtExceptions()
            .atMost(10, TimeUnit.SECONDS);

    private boolean waitLogs(String expectedMessage) {
        WebDriver driver = WebDriverRunner.getWebDriver();

        AtomicBoolean isLogContains = new AtomicBoolean(false);

        WAITER.get().until(() -> {
            LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
            isLogContains.set(logEntries.getAll().stream().anyMatch(x -> x.getMessage().contains(expectedMessage)));
            return isLogContains.get();
        });
        return isLogContains.get();
    }

    @BeforeAll
    public static void setUp() {

        DesiredCapabilities capabilities = new DesiredCapabilities();
        ChromeOptions options = new ChromeOptions();
        LoggingPreferences loggingPreferences = new LoggingPreferences();

        loggingPreferences.enable(LogType.BROWSER, Level.ALL);
        loggingPreferences.enable(LogType.PERFORMANCE, Level.ALL);

        capabilities.setCapability("goog:loggingPrefs", loggingPreferences);
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);

        Configuration.browserCapabilities = capabilities;
        Configuration.timeout = 10000;
        Configuration.pageLoadTimeout = 10000;
    }
}
