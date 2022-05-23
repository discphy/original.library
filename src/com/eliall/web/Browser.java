package com.eliall.web;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.eliall.common.Config;
import com.eliall.common.EliObject;

@SuppressWarnings("unchecked")
public class Browser extends EliObject implements Closeable {
	private WebDriver driver = null;
	private long created = 0, updated = 0;
	
	static { setup(Paths.get(Config.get("document.root"), "bin")); }
		
	public static void setup(Path path) { setup(path, "chromedriver." + (System.getProperty("os.type").equals("windows") ? "exe" : System.getProperty("os.type"))); }
	public static void setup(Path path, String binary) {
		System.setProperty("webdriver.chrome.whitelistedIps", "");
		System.setProperty("webdriver.chrome.verboseLogging", "false");
		System.setProperty("webdriver.chrome.driver", path.toAbsolutePath() + File.separator + binary);
	}
	
	public Browser() { this(null); }
	public Browser(Object object) {
		super(object);
		
		driver = new ChromeDriver(options());
		created = System.currentTimeMillis();
		updated = System.currentTimeMillis();
	}
	
	public void load() {

	}
	
	public void back() { driver.navigate().back(); updated = System.currentTimeMillis(); }
	public void forward() { driver.navigate().forward(); updated = System.currentTimeMillis(); }
	
	public boolean await(long time) { return await(time, 1000); }
	public boolean await(long time, long sleep) {
		return new WebDriverWait(driver, Duration.ofSeconds(time), Duration.ofMillis(sleep)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor)driver).executeScript("return document.readyState").toString().equals("complete");
			}
        });
	}
	
	public long created() { return created; }
	public long updated() { return updated; }

	@Override
	public void close() throws IOException {
		if (driver != null) {
			try { driver.quit(); } catch (Throwable e) { }
			try { driver.close(); } catch (Throwable e) { }
		}
	}

	private ChromeOptions options() {
		ChromeOptions options = new ChromeOptions();
		List<String> arguments = (List<String>)get("drvier_options");
		
		if (arguments != null) options.addArguments(arguments);
		if (!getBoolean("debug_mode")) options.addArguments("headless");
		
		return options;
	}
}