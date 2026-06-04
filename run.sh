#!/bin/bash
mvn compile -q && mvn exec:java \
  -Dexec.mainClass="com.recorder.ui.RecorderLauncher" \
  -Dexec.jvmArgs="--add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics" \
  -q 2>/dev/null
