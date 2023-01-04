package core;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;

import static com.codeborne.selenide.Selenide.$x;

public class LogTest {

    private final Supplier<ConditionFactory> WAITER = () -> Awaitility.given()
            .ignoreExceptions()
            .pollInterval(3, TimeUnit.SECONDS)
            .await()
            .dontCatchUncaughtExceptions()
            .atMost(15, TimeUnit.SECONDS);

    private boolean waitLogs(String expectedMessage) {
        WebDriver driver = WebDriverRunner.getWebDriver();

        AtomicBoolean isLogContains = new AtomicBoolean(false);

        WAITER.get().until(() -> {
            LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
            printLogs(logEntries);
            isLogContains.set(logEntries.getAll().stream().anyMatch(x -> x.getMessage().contains(expectedMessage)));
            return isLogContains.get();
        });
        return isLogContains.get();
    }

    private void printLogs(LogEntries logEntries) {
        for (LogEntry entry : logEntries) {
            System.out.println(entry.getMessage());
        }
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

    @Test
    public void checkLogsWithDelay() {

        Selenide.open("https://www.gismeteo.com/");
        boolean isLogExist = waitLogs("ERR_FILE_NOT_FOUND");
        Assertions.assertTrue(isLogExist, "https://www.gismeteo.com/ - The resource https://static.gismeteo.st/assets/bg-header/n.jpg was preloaded using link preload but not used within a few seconds from the window's load event. Please make sure it has an appropriate `as` value and it is preloaded intentionally.");
    }

    @Test
    public void logsErrorTest() {

        String errorMessage = "https://www.gismeteo.com/ - The resource https://static.gismeteo.st/assets/bg-header/n.jpg was preloaded using link preload but not used within a few seconds from the window's load event. Please make sure it has an appropriate `as` value and it is preloaded intentionally.";

        Selenide.open("https://www.gismeteo.com/");
        Configuration.browserSize = "1920x1080";
        $x("//a[@href='/weather-london-744/']");
        $x("//h1[text()='London Weather Forecast']").should(Condition.visible);

        boolean isContainsLogs = waitLogs(errorMessage);

        Assertions.assertTrue(isContainsLogs, "Error message " + errorMessage + " is not found");

    }
}
