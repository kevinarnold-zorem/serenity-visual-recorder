package com.recorder.engine;

import org.openqa.selenium.*;

public class SelectorVerifier {

    private final WebDriver driver;
    private final JavascriptExecutor js;

    private static final String HIGHLIGHT_GREEN = """
        arguments[0].style.outline = '3px solid #00CC00';
        arguments[0].style.backgroundColor = 'rgba(0,204,0,0.15)';
        arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});
        """;

    private static final String HIGHLIGHT_CLEAR = """
        arguments[0].style.outline = '';
        arguments[0].style.backgroundColor = '';
        """;

    public SelectorVerifier(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
    }

    public VerificationResult verify(String selector) {
        try {
            WebElement el = findElement(selector);

            // Resaltar en verde
            js.executeScript(HIGHLIGHT_GREEN, el);

            // Quitar resaltado despues de 2 segundos en hilo aparte
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    js.executeScript(HIGHLIGHT_CLEAR, el);
                } catch (Exception ignored) {}
            }).start();

            String tag  = el.getTagName();
            String text = el.getText();
            String type = el.getAttribute("type") != null ? el.getAttribute("type") : "";

            System.out.println("[SelectorVerifier] ✓ Encontrado: <" + tag + "> \"" + text + "\"");
            return VerificationResult.success(tag, text, type);

        } catch (NoSuchElementException e) {
            System.err.println("[SelectorVerifier] ✗ No encontrado: " + selector);
            return VerificationResult.failure("Elemento no encontrado con selector: " + selector);
        } catch (Exception e) {
            System.err.println("[SelectorVerifier] ✗ Error: " + e.getMessage());
            return VerificationResult.failure("Error al verificar: " + e.getMessage());
        }
    }

    private WebElement findElement(String selector) {
        // Intentar como XPath primero
        if (selector.startsWith("/") || selector.startsWith("(")) {
            return driver.findElement(By.xpath(selector));
        }
        // Intentar como CSS selector
        try {
            return driver.findElement(By.cssSelector(selector));
        } catch (Exception e) {
            // Fallback a XPath
            return driver.findElement(By.xpath(selector));
        }
    }

    // Resultado de verificacion
    public static class VerificationResult {
        private final boolean found;
        private final String tag;
        private final String text;
        private final String type;
        private final String errorMessage;

        private VerificationResult(boolean found, String tag, String text,
                                   String type, String errorMessage) {
            this.found        = found;
            this.tag          = tag;
            this.text         = text;
            this.type         = type;
            this.errorMessage = errorMessage;
        }

        public static VerificationResult success(String tag, String text, String type) {
            return new VerificationResult(true, tag, text, type, null);
        }

        public static VerificationResult failure(String errorMessage) {
            return new VerificationResult(false, null, null, null, errorMessage);
        }

        public boolean isFound()         { return found; }
        public String getTag()           { return tag; }
        public String getText()          { return text; }
        public String getType()          { return type; }
        public String getErrorMessage()  { return errorMessage; }

        public String getSummary() {
            if (found) {
                return "✓ Encontrado: <" + tag + ">"
                    + (text != null && !text.isEmpty() ? " \"" + text + "\"" : "")
                    + (type != null && !type.isEmpty() ? " [type=" + type + "]" : "");
            }
            return "✗ " + errorMessage;
        }
    }
}
