package com.recorder.locator;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class LocatorManager {

    private static final String SEPARATOR = ":@:";
    private final String filePath;
    private final Map<String, String> locators = new LinkedHashMap<>();

    public LocatorManager(String filePath) {
        this.filePath = filePath;
        loadFromFile();
    }

    private void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                String[] parts = line.split(SEPARATOR, 2);
                if (parts.length == 2) {
                    locators.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("[LocatorManager] Error leyendo archivo: " + e.getMessage());
        }
    }

    public void addLocator(String variableName, String selector) {
        locators.put(variableName, selector);
        saveToFile();
        System.out.println("[LocatorManager] Guardado: " + variableName + SEPARATOR + selector);
    }

    public String resolve(String variable) {
        String key = variable.replace("{", "").replace("}", "");
        String resolved = locators.getOrDefault(key, null);
        if (resolved == null) {
            System.err.println("[LocatorManager] Variable no encontrada: " + key);
            return variable;
        }
        return resolved;
    }

    public boolean exists(String variableName) {
        return locators.containsKey(variableName);
    }

    private void saveToFile() {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("# Archivo generado por Serenity Visual Recorder");
            pw.println("# formato: nombre_variable:@:selector");
            pw.println();
            locators.forEach((k, v) -> pw.println(k + SEPARATOR + v));
        } catch (IOException e) {
            System.err.println("[LocatorManager] Error guardando archivo: " + e.getMessage());
        }
    }

    public Map<String, String> getAll() {
        return locators;
    }

    public String getFilePath() {
        return filePath;
    }
}
