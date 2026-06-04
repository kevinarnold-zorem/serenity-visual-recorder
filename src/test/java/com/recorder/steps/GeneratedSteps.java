package com.recorder.steps;

import com.recorder.engine.DriverManager;
import com.recorder.locator.LocatorManager;
import io.cucumber.java.en.*;
import net.serenitybdd.core.Serenity;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class GeneratedSteps {

    private static final String LOCATORS_PATH =
        "src/test/resources/locators/recorded.locators";

    private final LocatorManager locatorManager =
        new LocatorManager(LOCATORS_PATH);

    private WebDriver driver() {
        return DriverManager.getDriver();
    }

    private WebElement waitFor(String selector) {
        return new WebDriverWait(driver(), Duration.ofSeconds(10))
            .until(ExpectedConditions.visibilityOfElementLocated(by(selector)));
    }

    private By by(String selector) {
        if (selector.startsWith("/") || selector.startsWith("(")) {
            return By.xpath(selector);
        }
        return By.cssSelector(selector);
    }

    private String resolve(String input) {
        if (input.startsWith("{") && input.endsWith("}")) {
            return locatorManager.resolve(input);
        }
        return input;
    }

    // ─── NAVEGACION ──────────────────────────────────────────────────────────

    @Given("el usuario navega a {string}")
    public void navegarA(String url) {
        System.out.println("[Step] Navegar a: " + url);
        DriverManager.navigateTo(url);
        Serenity.takeScreenshot();
    }

    // ─── INTERACCION ─────────────────────────────────────────────────────────

    @When("el usuario hace click en {string}")
    public void clickEn(String locator) {
        String selector = resolve(locator);
        System.out.println("[Step] Click en: " + selector);
        waitFor(selector).click();
        Serenity.takeScreenshot();
    }

    @When("el usuario escribe {string} en {string}")
    public void escribirEn(String texto, String locator) {
        String selector = resolve(locator);
        System.out.println("[Step] Escribir '" + texto + "' en: " + selector);
        WebElement el = waitFor(selector);
        el.clear();
        el.sendKeys(texto);
        Serenity.takeScreenshot();
    }

    @When("el usuario limpia el campo {string}")
    public void limpiarCampo(String locator) {
        String selector = resolve(locator);
        System.out.println("[Step] Limpiar campo: " + selector);
        waitFor(selector).clear();
    }

    @When("el usuario selecciona {string} en {string}")
    public void seleccionar(String valor, String locator) {
        String selector = resolve(locator);
        System.out.println("[Step] Seleccionar '" + valor + "' en: " + selector);
        new Select(waitFor(selector)).selectByVisibleText(valor);
        Serenity.takeScreenshot();
    }

    @When("el usuario espera {string} segundos")
    public void esperar(String segundos) {
        try {
            int ms = (int)(Double.parseDouble(segundos) * 1000);
            System.out.println("[Step] Esperando " + segundos + " segundos");
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @When("el usuario toma una captura {string}")
    public void tomarCaptura(String nombre) {
        System.out.println("[Step] Captura: " + nombre);
        Serenity.takeScreenshot();
    }

    // ─── VALIDACIONES ────────────────────────────────────────────────────────

    @Then("el usuario verifica el texto {string} en {string}")
    public void verificarTexto(String textoEsperado, String locator) {
        String selector = resolve(locator);
        System.out.println("[Step] Verificar texto '" + textoEsperado + "' en: " + selector);
        String actual = waitFor(selector).getText();
        Serenity.takeScreenshot();
        Assertions.assertThat(actual)
            .as("Texto en elemento " + selector)
            .contains(textoEsperado);
    }

    @Then("el elemento {string} es visible")
    public void elementoEsVisible(String locator) {
        String selector = resolve(locator);
        System.out.println("[Step] Verificar visible: " + selector);
        WebElement el = waitFor(selector);
        Serenity.takeScreenshot();
        Assertions.assertThat(el.isDisplayed())
            .as("Elemento deberia ser visible: " + selector)
            .isTrue();
    }

    @Then("el elemento {string} no es visible")
    public void elementoNoEsVisible(String locator) {
        String selector = resolve(locator);
        System.out.println("[Step] Verificar no visible: " + selector);
        try {
            driver().findElement(by(selector));
            Assertions.fail("Elemento encontrado pero se esperaba que no existiera: " + selector);
        } catch (NoSuchElementException e) {
            System.out.println("[Step] ✓ Elemento no existe como se esperaba");
            Serenity.takeScreenshot();
        }
    }
}
