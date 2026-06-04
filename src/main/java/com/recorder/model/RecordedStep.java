package com.recorder.model;

public class RecordedStep {

    public enum Action {
        NAVEGAR,
        CLICK,
        ESCRIBIR,
        LIMPIAR,
        SELECCIONAR,
        VERIFICAR_TEXTO,
        VERIFICAR_EXISTE,
        VERIFICAR_NO_EXISTE,
        ESPERAR,
        SCREENSHOT
    }

    private final Action action;
    private final String variableName; // nombre en .locators
    private final String selector;     // xpath/css real
    private final String value;        // texto a escribir o verificar
    private final String description;  // descripcion del step

    public RecordedStep(Action action, String variableName,
                        String selector, String value, String description) {
        this.action       = action;
        this.variableName = variableName;
        this.selector     = selector;
        this.value        = value;
        this.description  = description;
    }

    public Action getAction()        { return action; }
    public String getVariableName()  { return variableName; }
    public String getSelector()      { return selector; }
    public String getValue()         { return value; }
    public String getDescription()   { return description; }

    // Genera la linea Gherkin del step
    public String toGherkinLine(int index) {
        String keyword = index == 0 ? "Given" : (index == 1 ? "When" : "And");
        String locator = (variableName != null && !variableName.isEmpty())
                         ? "{" + variableName + "}" : "";

        switch (action) {
            case NAVEGAR:
                return keyword + " el usuario navega a \"" + value + "\"";
            case CLICK:
                return keyword + " el usuario hace click en \"" + locator + "\"";
            case ESCRIBIR:
                return keyword + " el usuario escribe \"" + value + "\" en \"" + locator + "\"";
            case LIMPIAR:
                return keyword + " el usuario limpia el campo \"" + locator + "\"";
            case SELECCIONAR:
                return keyword + " el usuario selecciona \"" + value + "\" en \"" + locator + "\"";
            case VERIFICAR_TEXTO:
                return "Then el usuario verifica el texto \"" + value + "\" en \"" + locator + "\"";
            case VERIFICAR_EXISTE:
                return "Then el elemento \"" + locator + "\" es visible";
            case VERIFICAR_NO_EXISTE:
                return "Then el elemento \"" + locator + "\" no es visible";
            case ESPERAR:
                return keyword + " el usuario espera " + value + " segundos";
            case SCREENSHOT:
                return keyword + " el usuario toma una captura \"" + value + "\"";
            default:
                return keyword + " " + description;
        }
    }

    @Override
    public String toString() {
        return "[" + action + "] " + 
               (variableName != null ? "{" + variableName + "} " : "") + 
               (value != null ? "= " + value : "");
    }
}
