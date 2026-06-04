package com.recorder.ui;

import com.recorder.engine.DriverManager;

public class RecorderLauncher {

    public static void main(String[] args) {
        System.out.println("[Launcher] Iniciando Serenity Visual Recorder...");

        // Abrir Chrome primero
        DriverManager.navigateTo("about:blank");

        // Lanzar panel JavaFX
        RecorderPanel.launch(args);
    }
}
