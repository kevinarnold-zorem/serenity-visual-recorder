package com.recorder.engine;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XPathInspector {

    private final WebDriver driver;
    private final JavascriptExecutor js;

    private static final String INJECT_INSPECTOR_JS = """
        window.__recordedXPath = null;
        window.__recordedTag   = null;

        function getSmartXPath(el) {
            if (el.id) return '//*[@id="' + el.id + '"]';
            var dt = el.getAttribute('data-test');
            if (dt) return '//*[@data-test="' + dt + '"]';
            var nm = el.getAttribute('name');
            if (nm) return '//*[@name="' + nm + '"]';
            var ph = el.getAttribute('placeholder');
            if (ph) return '//*[@placeholder="' + ph + '"]';
            var txt = el.innerText ? el.innerText.trim() : '';
            if (txt && txt.length < 50)
                return '//' + el.tagName.toLowerCase() + '[normalize-space()="' + txt + '"]';
            var path = '';
            var node = el;
            while (node && node.nodeType === 1) {
                var idx = 1;
                var sib = node.previousSibling;
                while (sib) {
                    if (sib.nodeType === 1 && sib.tagName === node.tagName) idx++;
                    sib = sib.previousSibling;
                }
                path = '/' + node.tagName.toLowerCase() + '[' + idx + ']' + path;
                node = node.parentNode;
            }
            return path;
        }

        function showBadge() {
            if (window.__recorderBadge) window.__recorderBadge.remove();
            var badge = document.createElement('div');
            badge.id = '__recorder_badge';
            badge.style.cssText = [
                'position: fixed',
                'top: 10px', 'left: 50%',
                'transform: translateX(-50%)',
                'background: #1F4E79',
                'color: white',
                'padding: 8px 20px',
                'border-radius: 20px',
                'font-family: Arial',
                'font-size: 13px',
                'font-weight: bold',
                'z-index: 9999999',
                'pointer-events: none',
                'box-shadow: 0 2px 10px rgba(0,0,0,0.4)'
            ].join(';');
            badge.innerText = '🎯 Haz click en el elemento — ESC para cancelar';
            document.body.appendChild(badge);
            window.__recorderBadge = badge;
        }

        function cleanup() {
            if (window.__lastHovered) {
                window.__lastHovered.style.outline = window.__lastOutline || '';
                window.__lastHovered.style.backgroundColor = window.__lastBg || '';
            }
            if (window.__recorderBadge) window.__recorderBadge.remove();
            document.removeEventListener('mouseover', window.__hoverHandler, true);
            document.removeEventListener('click', document.__inspectorHandler, true);
            document.removeEventListener('keydown', window.__escHandler, true);
            document.body.style.cursor = 'default';
        }

        showBadge();

        window.__hoverHandler = function(e) {
            if (window.__lastHovered && window.__lastHovered !== e.target) {
                window.__lastHovered.style.outline = window.__lastOutline || '';
                window.__lastHovered.style.backgroundColor = window.__lastBg || '';
            }
            window.__lastOutline = e.target.style.outline;
            window.__lastBg = e.target.style.backgroundColor;
            window.__lastHovered = e.target;
            e.target.style.outline = '2px dashed #FF6600';
            e.target.style.backgroundColor = 'rgba(255,102,0,0.08)';
        };

        document.__inspectorHandler = function(e) {
            e.preventDefault();
            e.stopPropagation();
            window.__recordedXPath = getSmartXPath(e.target);
            window.__recordedTag   = e.target.tagName.toLowerCase();
            e.target.style.outline = '3px solid #00CC00';
            e.target.style.backgroundColor = 'rgba(0,204,0,0.15)';
            cleanup();
        };

        window.__escHandler = function(e) {
            if (e.key === 'Escape') {
                window.__recordedXPath = '__CANCELLED__';
                cleanup();
            }
        };

        document.body.style.cursor = 'crosshair';
        document.addEventListener('mouseover', window.__hoverHandler, true);
        document.addEventListener('click', document.__inspectorHandler, true);
        document.addEventListener('keydown', window.__escHandler, true);
        """;

    public XPathInspector(WebDriver driver) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
    }

    public void activateInspector() {
        js.executeScript("window.__recordedXPath = null; window.__recordedTag = null;");
        bringChromeToFront();
        js.executeScript(INJECT_INSPECTOR_JS);
        System.out.println("[XPathInspector] Inspector activado");
    }

    // Regresa el foco al panel JavaFX despues de capturar
    public void bringPanelToFront() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                // Busca la app Java/Maven que tiene el panel
                String[] script = {
                    "osascript", "-e",
                    "tell application \"System Events\" to set frontmost of " +
                    "(first process whose name contains \"java\") to true"
                };
                Runtime.getRuntime().exec(script);
                Thread.sleep(400);
            } else if (os.contains("win")) {
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_ALT);
                Thread.sleep(300);
            }
        } catch (Exception e) {
            System.out.println("[XPathInspector] Regreso al panel: " + e.getMessage());
        }
    }

    private void bringChromeToFront() {
        try {
            js.executeScript("window.focus();");
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                String[] script = {
                    "osascript", "-e",
                    "tell application \"Google Chrome\" to activate"
                };
                Runtime.getRuntime().exec(script);
                Thread.sleep(400);
            } else if (os.contains("win")) {
                Robot robot = new Robot();
                robot.keyPress(KeyEvent.VK_ALT);
                robot.keyPress(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_ALT);
                Thread.sleep(300);
            }
        } catch (Exception e) {
            System.out.println("[XPathInspector] Foco Chrome: " + e.getMessage());
        }
    }

    public String waitForSelection(int timeoutSeconds) throws InterruptedException {
        int elapsed = 0;
        while (elapsed < timeoutSeconds * 1000) {
            Object xpath = js.executeScript("return window.__recordedXPath || null;");
            if (xpath != null && !xpath.toString().isEmpty()) {
                String result = xpath.toString();
                if ("__CANCELLED__".equals(result)) {
                    System.out.println("[XPathInspector] Cancelado con ESC");
                    return null;
                }
                System.out.println("[XPathInspector] XPath capturado: " + result);

                // Regresar foco al panel automaticamente
                bringPanelToFront();
                return result;
            }
            Thread.sleep(200);
            elapsed += 200;
        }
        System.out.println("[XPathInspector] Timeout");
        return null;
    }

    public String getLastTag() {
        Object tag = js.executeScript("return window.__recordedTag || 'div';");
        return tag != null ? tag.toString() : "div";
    }

    public String suggestVariableName(String xpath, String tagName) {
        Pattern id = Pattern.compile("@id=\"([^\"]+)\"");
        Matcher m = id.matcher(xpath);
        if (m.find()) return tagName + "_" + m.group(1).replace("-", "_");

        Pattern dt = Pattern.compile("@data-test=\"([^\"]+)\"");
        m = dt.matcher(xpath);
        if (m.find()) return tagName + "_" + m.group(1).replace("-", "_");

        Pattern nm = Pattern.compile("@name=\"([^\"]+)\"");
        m = nm.matcher(xpath);
        if (m.find()) return tagName + "_" + m.group(1).replace("-", "_");

        Pattern ph = Pattern.compile("@placeholder=\"([^\"]+)\"");
        m = ph.matcher(xpath);
        if (m.find()) return tagName + "_" + m.group(1).toLowerCase()
                                              .replace(" ", "_")
                                              .replaceAll("[^a-z0-9_]", "");

        return tagName + "_elemento_" + (System.currentTimeMillis() % 1000);
    }
}
