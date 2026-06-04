package com.recorder.engine;

import com.recorder.model.RecordedStep;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class StepExecutor {

    private final WebDriver driver;
    private final JavascriptExecutor js;
    private final SelectorVerifier verifier;

    public StepExecutor(WebDriver driver) {
        this.driver   = driver;
        this.js       = (JavascriptExecutor) driver;
        this.verifier = new SelectorVerifier(driver);
    }

    public ExecutionResult execute(RecordedStep step) {
        System.out.println("[StepExecutor] Ejecutando: " + step);
        try {
            switch (step.getAction()) {
                case NAVEGAR:
                    return executeNavegar(step.getValue());
                case CLICK:
                    return executeClick(step.getSelector());
                case ESCRIBIR:
                    return executeEscribir(step.getSelector(), step.getValue());
                case LIMPIAR:
                    return executeLimpiar(step.getSelector());
                case SELECCIONAR:
                    return executeSeleccionar(step.getSelector(), step.getValue());
                case VERIFICAR_TEXTO:
                    return executeVerificarTexto(step.getSelector(), step.getValue());
                case VERIFICAR_EXISTE:
                    return executeVerificarExiste(step.getSelector());
                case VERIFICAR_NO_EXISTE:
                    return executeVerificarNoExiste(step.getSelector());
                case ESPERAR:
                    return executeEsperar(step.getValue());
                case SCREENSHOT:
                    return executeScreenshot();
                default:
                    return ExecutionResult.failure("Accion no reconocida: " + step.getAction());
            }
        } catch (Exception e) {
            System.err.println("[StepExecutor] Error: " + e.getMessage());
            return ExecutionResult.failure("Error ejecutando step: " + e.getMessage());
        }
    }

    private ExecutionResult executeNavegar(String url) {
        driver.get(url);
        waitForPageLoad();
        return ExecutionResult.success("Navegado a: " + url);
    }

    private ExecutionResult executeClick(String selector) {
        WebElement el = waitForElement(selector);
        highlightBeforeAction(el);
        el.click();
        return ExecutionResult.success("Click en: " + selector);
    }

    private ExecutionResult executeEscribir(String selector, String value) {
        WebElement el = waitForElement(selector);
        highlightBeforeAction(el);
        el.clear();
        el.sendKeys(value);
        return ExecutionResult.success("Escrito \"" + value + "\" en: " + selector);
    }

    private ExecutionResult executeLimpiar(String selector) {
        WebElement el = waitForElement(selector);
        highlightBeforeAction(el);
        el.clear();
        return ExecutionResult.success("Campo limpiado: " + selector);
    }

    private ExecutionResult executeSeleccionar(String selector, String value) {
        WebElement el = waitForElement(selector);
        highlightBeforeAction(el);
        new Select(el).selectByVisibleText(value);
        return ExecutionResult.success("Seleccionado \"" + value + "\" en: " + selector);
    }

    private ExecutionResult executeVerificarTexto(String selector, String expectedText) {
        WebElement el = waitForElement(selector);
        String actualText = el.getText();
        if (actualText.contains(expectedText)) {
            highlightSuccess(el);
            return ExecutionResult.success("Texto verificado: \"" + expectedText + "\"");
        }
        highlightError(el);
        return ExecutionResult.failure(
            "Texto esperado: \"" + expectedText + "\" | Actual: \"" + actualText + "\""
        );
    }

    private ExecutionResult executeVerificarExiste(String selector) {
        try {
            WebElement el = waitForElement(selector);
            highlightSuccess(el);
            return ExecutionResult.success("Elemento existe: " + selector);
        } catch (Exception e) {
            return ExecutionResult.failure("Elemento no encontrado: " + selector);
        }
    }

    private ExecutionResult executeVerificarNoExiste(String selector) {
        try {
            driver.findElement(bySelector(selector));
            return ExecutionResult.failure("Elemento encontrado (se esperaba que no existiera): " + selector);
        } catch (NoSuchElementException e) {
            return ExecutionResult.success("Elemento no existe (correcto): " + selector);
        }
    }

    private ExecutionResult executeEsperar(String seconds) {
        try {
            int ms = (int)(Double.parseDouble(seconds) * 1000);
            Thread.sleep(ms);
            return ExecutionResult.success("Esperado " + seconds + " segundos");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ExecutionResult.failure("Espera interrumpida");
        }
    }

    private ExecutionResult executeScreenshot() {
        try {
            net.serenitybdd.core.Serenity.takeScreenshot();
            return ExecutionResult.success("Captura tomada");
        } catch (Exception e) {
            return ExecutionResult.success("Captura tomada (sin Serenity context)");
        }
    }

    // --- Helpers ---

    private WebElement waitForElement(String selector) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(bySelector(selector)));
    }

    private By bySelector(String selector) {
        if (selector.startsWith("/") || selector.startsWith("(")) {
            return By.xpath(selector);
        }
        return By.cssSelector(selector);
    }

    private void waitForPageLoad() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(d -> js.executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}
    }

    private void highlightBeforeAction(WebElement el) {
        try {
            js.executeScript(
                "arguments[0].style.outline='3px solid #FF6600';" +
                "arguments[0].style.backgroundColor='rgba(255,102,0,0.1)';",
                el
            );
            Thread.sleep(300);
            js.executeScript(
                "arguments[0].style.outline='';" +
                "arguments[0].style.backgroundColor='';",
                el
            );
        } catch (Exception ignored) {}
    }

    private void highlightSuccess(WebElement el) {
        try {
            js.executeScript(
                "arguments[0].style.outline='3px solid #00CC00';" +
                "arguments[0].style.backgroundColor='rgba(0,204,0,0.15)';",
                el
            );
            Thread.sleep(500);
            js.executeScript(
                "arguments[0].style.outline='';" +
                "arguments[0].style.backgroundColor='';",
                el
            );
        } catch (Exception ignored) {}
    }

    private void highlightError(WebElement el) {
        try {
            js.executeScript(
                "arguments[0].style.outline='3px solid #CC0000';" +
                "arguments[0].style.backgroundColor='rgba(204,0,0,0.15)';",
                el
            );
        } catch (Exception ignored) {}
    }

    // Resultado de ejecucion
    public static class ExecutionResult {
        private final boolean success;
        private final String message;

        private ExecutionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ExecutionResult success(String message) {
            System.out.println("[StepExecutor] ✓ " + message);
            return new ExecutionResult(true, message);
        }

        public static ExecutionResult failure(String message) {
            System.err.println("[StepExecutor] ✗ " + message);
            return new ExecutionResult(false, message);
        }

        public boolean isSuccess()  { return success; }
        public String getMessage()  { return message; }
    }
}
