package com.recorder.engine;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.JavascriptExecutor;

import java.util.HashMap;
import java.util.Map;

public class DriverManager {

    private static WebDriver driver;
    private static JavascriptExecutor js;

    public static WebDriver getDriver() {
        if (driver == null) {
            initDriver();
        }
        return driver;
    }

    public static JavascriptExecutor getJs() {
        if (js == null) {
            getDriver();
        }
        return js;
    }

    private static void initDriver() {
        System.out.println("[DriverManager] Iniciando Chrome...");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--start-maximized");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-infobars");

        driver = new ChromeDriver(options);
        js = (JavascriptExecutor) driver;

        System.out.println("[DriverManager] Chrome iniciado correctamente");
    }

    public static void navigateTo(String url) {
        getDriver().get(url);
        System.out.println("[DriverManager] Navegando a: " + url);
    }

    public static String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }

    public static String getPageTitle() {
        return getDriver().getTitle();
    }

    public static void quit() {
        if (driver != null) {
            System.out.println("[DriverManager] Cerrando Chrome...");
            driver.quit();
            driver = null;
            js = null;
        }
    }

    public static boolean isActive() {
        if (driver == null) return false;
        try {
            driver.getCurrentUrl();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
