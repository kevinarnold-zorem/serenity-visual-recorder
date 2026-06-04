package com.recorder.generator;

import com.recorder.model.RecordedStep;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FeatureGenerator {

    private final String featuresPath;
    private final String locatorsPath;

    public FeatureGenerator(String featuresPath, String locatorsPath) {
        this.featuresPath = featuresPath;
        this.locatorsPath = locatorsPath;
    }

    public String generate(String featureName, String scenarioName,
                           List<RecordedStep> steps) {

        String fileName  = toFileName(featureName) + ".feature";
        String filePath  = featuresPath + "/" + fileName;

        new File(featuresPath).mkdirs();

        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {

            // Header
            pw.println("# Generado por Serenity Visual Recorder");
            pw.println("# Fecha: " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            pw.println("# Locators: " + locatorsPath);
            pw.println();

            pw.println("Feature: " + featureName);
            pw.println();
            pw.println("  Scenario: " + scenarioName);

            // Steps
            for (int i = 0; i < steps.size(); i++) {
                pw.println("    " + steps.get(i).toGherkinLine(i));
            }

            pw.println();

        } catch (IOException e) {
            System.err.println("[FeatureGenerator] Error generando feature: " + e.getMessage());
            return null;
        }

        System.out.println("[FeatureGenerator] Feature generado: " + filePath);
        return filePath;
    }

    // Preview del Gherkin sin guardar
    public String preview(String featureName, String scenarioName,
                          List<RecordedStep> steps) {
        StringBuilder sb = new StringBuilder();
        sb.append("Feature: ").append(featureName).append("\n\n");
        sb.append("  Scenario: ").append(scenarioName).append("\n");
        for (int i = 0; i < steps.size(); i++) {
            sb.append("    ").append(steps.get(i).toGherkinLine(i)).append("\n");
        }
        return sb.toString();
    }

    private String toFileName(String name) {
        return name.toLowerCase()
                   .replace(" ", "_")
                   .replaceAll("[^a-z0-9_]", "");
    }
}
