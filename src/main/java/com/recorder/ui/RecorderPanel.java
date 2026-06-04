package com.recorder.ui;

import com.recorder.engine.DriverManager;
import com.recorder.engine.StepExecutor;
import com.recorder.engine.SelectorVerifier;
import com.recorder.engine.XPathInspector;
import com.recorder.generator.FeatureGenerator;
import com.recorder.locator.LocatorManager;
import com.recorder.model.RecordedStep;
import com.recorder.model.RecordedStep.Action;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class RecorderPanel extends Application {

    // Engine
    private DriverManager driverManager;
    private StepExecutor stepExecutor;
    private SelectorVerifier selectorVerifier;
    private XPathInspector xpathInspector;
    private LocatorManager locatorManager;
    private FeatureGenerator featureGenerator;

    // Steps grabados
    private final List<RecordedStep> recordedSteps = new ArrayList<>();
    private final ObservableList<String> stepListItems = FXCollections.observableArrayList();

    // UI Components
    private TextField txtUrl;
    private TextField txtSelector;
    private TextField txtVariableName;
    private TextField txtValue;
    private TextField txtDescription;
    private ComboBox<String> cmbAction;
    private ListView<String> lstSteps;
    private Label lblVerifyResult;
    private Label lblStatus;
    private TextArea txtGherkinPreview;
    private TextField txtFeatureName;
    private TextField txtScenarioName;

    // Paths
    private static final String FEATURES_PATH  = "src/test/resources/features";
    private static final String LOCATORS_PATH   = "src/test/resources/locators/recorded.locators";

    @Override
    public void start(Stage stage) {
        stage.setTitle("🎬 Serenity Visual Recorder");
        stage.setWidth(900);
        stage.setHeight(780);
        stage.setResizable(true);

        // Inicializar engine
        initEngine();

        // Layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1E1E2E;");
        root.setTop(buildHeader());
        root.setCenter(buildMainContent());
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> shutdown());
        stage.show();

        setStatus("✓ Panel iniciado. Chrome activo en: " + DriverManager.getCurrentUrl(), "#00CC00");
    }

    // ─── HEADER ──────────────────────────────────────────────────────────────
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #13131F; -fx-padding: 12 20;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(10);

        Label title = new Label("🎬  SERENITY VISUAL RECORDER");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#A0A8D8"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label urlLabel = new Label("URL:");
        urlLabel.setTextFill(Color.web("#666888"));
        urlLabel.setFont(Font.font("Arial", 12));

        txtUrl = new TextField(DriverManager.getCurrentUrl());
        txtUrl.setPrefWidth(300);
        txtUrl.setStyle("-fx-background-color: #2A2A3E; -fx-text-fill: #CCCCFF; " +
                        "-fx-border-color: #444466; -fx-border-radius: 4;");

        Button btnGo = styledButton("▶ Ir", "#2E75B6", "#FFFFFF");
        btnGo.setOnAction(e -> {
            DriverManager.navigateTo(txtUrl.getText().trim());
            setStatus("Navegando a: " + txtUrl.getText(), "#2E75B6");
        });

        header.getChildren().addAll(title, spacer, urlLabel, txtUrl, btnGo);
        return header;
    }

    // ─── CONTENIDO PRINCIPAL ─────────────────────────────────────────────────
    private SplitPane buildMainContent() {
        SplitPane split = new SplitPane();
        split.setStyle("-fx-background-color: #1E1E2E;");
        split.getItems().addAll(buildLeftPanel(), buildRightPanel());
        split.setDividerPositions(0.55);
        return split;
    }

    // ─── PANEL IZQUIERDO ─────────────────────────────────────────────────────
    private VBox buildLeftPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #1E1E2E;");

        // Seccion Inspector
        panel.getChildren().add(sectionLabel("🔍  INSPECTOR DE ELEMENTO"));
        panel.getChildren().add(buildInspectorSection());

        // Separador
        panel.getChildren().add(buildSeparator());

        // Seccion Step
        panel.getChildren().add(sectionLabel("⚡  DEFINIR STEP"));
        panel.getChildren().add(buildStepSection());

        // Boton ejecutar y guardar
        Button btnExecute = styledButton("▶  EJECUTAR Y GUARDAR STEP", "#1D7A1D", "#FFFFFF");
        btnExecute.setMaxWidth(Double.MAX_VALUE);
        btnExecute.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btnExecute.setPrefHeight(40);
        btnExecute.setOnAction(e -> executeAndSaveStep());
        panel.getChildren().add(btnExecute);

        // Seccion nombre feature/scenario
        panel.getChildren().add(buildSeparator());
        panel.getChildren().add(sectionLabel("📄  NOMBRE DEL CASO"));
        panel.getChildren().add(buildNamingSection());

        return panel;
    }

    private VBox buildInspectorSection() {
        VBox box = new VBox(8);

        // Boton inspector
        Button btnInspect = styledButton("🖱️  Seleccionar elemento en Chrome", "#7030A0", "#FFFFFF");
        btnInspect.setMaxWidth(Double.MAX_VALUE);
        btnInspect.setOnAction(e -> activateInspector());

        // Campo selector
        HBox selectorRow = new HBox(6);
        Label lblSel = fieldLabel("Selector / XPath:");
        txtSelector = new TextField();
        txtSelector.setPromptText("XPath o CSS selector...");
        txtSelector.setStyle(fieldStyle());
        HBox.setHgrow(txtSelector, Priority.ALWAYS);
        txtSelector.textProperty().addListener((obs, old, val) -> onSelectorChanged());

        Button btnCopy = styledButton("📋", "#444466", "#AAAACC");
        btnCopy.setOnAction(e -> copyToClipboard(txtSelector.getText()));
        selectorRow.getChildren().addAll(txtSelector, btnCopy);

        // Nombre variable
        HBox varRow = new HBox(6);
        Label lblVar = fieldLabel("Nombre variable:");
        txtVariableName = new TextField();
        txtVariableName.setPromptText("ej: btn_login, input_username...");
        txtVariableName.setStyle(fieldStyle());
        HBox.setHgrow(txtVariableName, Priority.ALWAYS);
        varRow.getChildren().add(txtVariableName);

        // Boton verificar
        Button btnVerify = styledButton("✅  Verificar selector", "#1F4E79", "#FFFFFF");
        btnVerify.setMaxWidth(Double.MAX_VALUE);
        btnVerify.setOnAction(e -> verifySelector());

        // Label resultado verificacion
        lblVerifyResult = new Label("— Ingresa un selector y presiona Verificar");
        lblVerifyResult.setTextFill(Color.web("#666888"));
        lblVerifyResult.setFont(Font.font("Arial", 11));
        lblVerifyResult.setWrapText(true);

        box.getChildren().addAll(
            btnInspect,
            lblSel, selectorRow,
            lblVar, varRow,
            btnVerify,
            lblVerifyResult
        );
        return box;
    }

    private VBox buildStepSection() {
        VBox box = new VBox(6);

        // Accion
        Label lblAction = fieldLabel("Accion:");
        cmbAction = new ComboBox<>();
        cmbAction.getItems().addAll(
            "NAVEGAR", "CLICK", "ESCRIBIR", "LIMPIAR",
            "SELECCIONAR", "VERIFICAR_TEXTO", "VERIFICAR_EXISTE",
            "VERIFICAR_NO_EXISTE", "ESPERAR", "SCREENSHOT"
        );
        cmbAction.setValue("CLICK");
        cmbAction.setMaxWidth(Double.MAX_VALUE);
        cmbAction.setStyle("-fx-background-color: #2A2A3E; -fx-text-fill: #CCCCFF;");
        cmbAction.setOnAction(e -> onActionChanged());

        // Valor
        Label lblVal = fieldLabel("Valor (texto, URL o segundos):");
        txtValue = new TextField();
        txtValue.setPromptText("ej: standard_user, https://..., 2");
        txtValue.setStyle(fieldStyle());

        // Descripcion
        Label lblDesc = fieldLabel("Descripcion (opcional):");
        txtDescription = new TextField();
        txtDescription.setPromptText("ej: el usuario hace click en login");
        txtDescription.setStyle(fieldStyle());

        box.getChildren().addAll(lblAction, cmbAction, lblVal, txtValue, lblDesc, txtDescription);
        return box;
    }

    private HBox buildNamingSection() {
        HBox box = new HBox(10);
        VBox featureBox = new VBox(4);
        featureBox.getChildren().add(fieldLabel("Feature:"));
        txtFeatureName = new TextField("Flujo grabado");
        txtFeatureName.setStyle(fieldStyle());
        featureBox.getChildren().add(txtFeatureName);
        HBox.setHgrow(featureBox, Priority.ALWAYS);

        VBox scenarioBox = new VBox(4);
        scenarioBox.getChildren().add(fieldLabel("Scenario:"));
        txtScenarioName = new TextField("Escenario grabado");
        txtScenarioName.setStyle(fieldStyle());
        scenarioBox.getChildren().add(txtScenarioName);
        HBox.setHgrow(scenarioBox, Priority.ALWAYS);

        box.getChildren().addAll(featureBox, scenarioBox);
        return box;
    }

    // ─── PANEL DERECHO ───────────────────────────────────────────────────────
    private VBox buildRightPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #181826;");

        // Steps grabados
        HBox stepsHeader = new HBox(8);
        stepsHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblSteps = sectionLabel("📋  STEPS GRABADOS");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDelete = styledButton("🗑️ Eliminar", "#8B0000", "#FFFFFF");
        btnDelete.setOnAction(e -> deleteSelectedStep());

        Button btnClear = styledButton("🧹 Limpiar todo", "#444466", "#AAAACC");
        btnClear.setOnAction(e -> clearAllSteps());

        stepsHeader.getChildren().addAll(lblSteps, spacer, btnDelete, btnClear);

        lstSteps = new ListView<>(stepListItems);
        lstSteps.setStyle("-fx-background-color: #12121E; -fx-text-fill: #CCCCFF; " +
                          "-fx-border-color: #333355;");
        lstSteps.setPrefHeight(200);
        VBox.setVgrow(lstSteps, Priority.ALWAYS);

        // Preview Gherkin
        panel.getChildren().add(sectionLabel("👁️  PREVIEW GHERKIN"));
        txtGherkinPreview = new TextArea();
        txtGherkinPreview.setEditable(false);
        txtGherkinPreview.setStyle("-fx-background-color: #12121E; -fx-text-fill: #00FF88; " +
                                   "-fx-font-family: 'Courier New'; -fx-font-size: 11;");
        txtGherkinPreview.setPrefHeight(180);
        txtGherkinPreview.setWrapText(false);

        // Botones finales
        HBox btnRow = new HBox(8);
        Button btnPreview = styledButton("👁️ Actualizar Preview", "#1F4E79", "#FFFFFF");
        btnPreview.setOnAction(e -> updateGherkinPreview());
        btnPreview.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnPreview, Priority.ALWAYS);

        Button btnGenerate = styledButton("💾 GENERAR ARCHIVOS", "#1D7A1D", "#FFFFFF");
        btnGenerate.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btnGenerate.setOnAction(e -> generateFiles());
        btnGenerate.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnGenerate, Priority.ALWAYS);

        btnRow.getChildren().addAll(btnPreview, btnGenerate);

        Button btnRunGenerated = styledButton("▶▶ EJECUTAR GHERKIN GENERADO", "#7030A0", "#FFFFFF");
        btnRunGenerated.setMaxWidth(Double.MAX_VALUE);
        btnRunGenerated.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btnRunGenerated.setPrefHeight(40);
        btnRunGenerated.setOnAction(e -> runGeneratedFeature());

        panel.getChildren().addAll(stepsHeader, lstSteps,
            sectionLabel("👁️  PREVIEW GHERKIN"),
            txtGherkinPreview, btnRow, buildSeparator(), btnRunGenerated
        );
        return panel;
    }

    // ─── STATUS BAR ──────────────────────────────────────────────────────────
    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.setStyle("-fx-background-color: #13131F; -fx-padding: 6 15;");
        bar.setAlignment(Pos.CENTER_LEFT);
        lblStatus = new Label("Iniciando...");
        lblStatus.setTextFill(Color.web("#888AAA"));
        lblStatus.setFont(Font.font("Arial", 11));
        bar.getChildren().add(lblStatus);
        return bar;
    }

    // ─── ACCIONES ────────────────────────────────────────────────────────────

    private void activateInspector() {
        setStatus("🖱️ Haz click en el elemento en Chrome...", "#FF6600");
        new Thread(() -> {
            try {
                xpathInspector.activateInspector();
                String xpath = xpathInspector.waitForSelection(30);
                if (xpath != null) {
                    String tag      = xpathInspector.getLastTag();
                    String suggested = xpathInspector.suggestVariableName(xpath, tag);
                    Platform.runLater(() -> {
                        txtSelector.setText(xpath);
                        txtVariableName.setText(suggested);
                        setStatus("✓ XPath capturado: " + xpath, "#00CC00");
                    });
                } else {
                    Platform.runLater(() ->
                        setStatus("⚠ Timeout - no se selecciono ningun elemento", "#FF6600"));
                }
            } catch (Exception e) {
                Platform.runLater(() ->
                    setStatus("✗ Error en inspector: " + e.getMessage(), "#CC0000"));
            }
        }).start();
    }

    private void verifySelector() {
        String selector = txtSelector.getText().trim();
        if (selector.isEmpty()) {
            lblVerifyResult.setText("⚠ Ingresa un selector primero");
            lblVerifyResult.setTextFill(Color.web("#FF6600"));
            return;
        }
        new Thread(() -> {
            SelectorVerifier.VerificationResult result = selectorVerifier.verify(selector);
            Platform.runLater(() -> {
                if (result.isFound()) {
                    lblVerifyResult.setText(result.getSummary());
                    lblVerifyResult.setTextFill(Color.web("#00CC00"));
                    setStatus("✓ Selector verificado correctamente", "#00CC00");
                } else {
                    lblVerifyResult.setText(result.getSummary());
                    lblVerifyResult.setTextFill(Color.web("#CC0000"));
                    setStatus("✗ Selector no encontrado", "#CC0000");
                }
            });
        }).start();
    }

    private void executeAndSaveStep() {
        String actionStr  = cmbAction.getValue();
        String selector   = txtSelector.getText().trim();
        String varName    = txtVariableName.getText().trim();
        String value      = txtValue.getText().trim();
        String desc       = txtDescription.getText().trim();

        // Validaciones basicas
        if (actionStr == null) {
            setStatus("⚠ Selecciona una accion", "#FF6600");
            return;
        }

        Action action = Action.valueOf(actionStr);

        // Para NAVEGAR el valor es obligatorio
        if (action == Action.NAVEGAR && value.isEmpty()) {
            setStatus("⚠ Ingresa la URL para NAVEGAR", "#FF6600");
            return;
        }

        // Para acciones con selector, el selector es obligatorio
        if (action != Action.NAVEGAR && action != Action.ESPERAR &&
            action != Action.SCREENSHOT && selector.isEmpty()) {
            setStatus("⚠ Ingresa o selecciona un selector", "#FF6600");
            return;
        }

        // Guardar en .locators si tiene variable y selector
        if (!varName.isEmpty() && !selector.isEmpty()) {
            if (locatorManager.exists(varName)) {
                setStatus("⚠ Variable '" + varName + "' ya existe en .locators", "#FF6600");
            } else {
                locatorManager.addLocator(varName, selector);
            }
        }

        // Crear y ejecutar el step
        RecordedStep step = new RecordedStep(action, varName, selector, value, desc);
        new Thread(() -> {
            StepExecutor.ExecutionResult result = stepExecutor.execute(step);
            Platform.runLater(() -> {
                if (result.isSuccess()) {
                    recordedSteps.add(step);
                    stepListItems.add((recordedSteps.size()) + ". " + step);
                    updateGherkinPreview();
                    clearStepFields();
                    setStatus("✓ Step guardado: " + step, "#00CC00");
                } else {
                    setStatus("✗ Fallo ejecutando step: " + result.getMessage(), "#CC0000");
                }
            });
        }).start();
    }

    private void generateFiles() {
        if (recordedSteps.isEmpty()) {
            setStatus("⚠ No hay steps grabados", "#FF6600");
            return;
        }
        String featureName  = txtFeatureName.getText().trim();
        String scenarioName = txtScenarioName.getText().trim();
        String filePath = featureGenerator.generate(featureName, scenarioName, recordedSteps);
        if (filePath != null) {
            setStatus("✓ Archivos generados - Feature: " + filePath +
                      " | Locators: " + LOCATORS_PATH, "#00CC00");
        } else {
            setStatus("✗ Error generando archivos", "#CC0000");
        }
    }

    private void runGeneratedFeature() {
        setStatus("▶▶ Para ejecutar el Gherkin generado usa: mvn clean verify", "#7030A0");
    }

    private void deleteSelectedStep() {
        int idx = lstSteps.getSelectionModel().getSelectedIndex();
        if (idx >= 0 && idx < recordedSteps.size()) {
            recordedSteps.remove(idx);
            stepListItems.remove(idx);
            updateGherkinPreview();
            setStatus("🗑️ Step eliminado", "#FF6600");
        }
    }

    private void clearAllSteps() {
        recordedSteps.clear();
        stepListItems.clear();
        txtGherkinPreview.clear();
        setStatus("🧹 Steps limpiados", "#666888");
    }

    private void updateGherkinPreview() {
        if (recordedSteps.isEmpty()) {
            txtGherkinPreview.setText("# Sin steps grabados aun...");
            return;
        }
        String preview = featureGenerator.preview(
            txtFeatureName.getText(),
            txtScenarioName.getText(),
            recordedSteps
        );
        txtGherkinPreview.setText(preview);
    }

    private void onActionChanged() {
        String action = cmbAction.getValue();
        if ("NAVEGAR".equals(action)) {
            txtValue.setPromptText("https://...");
            txtSelector.setDisable(true);
            txtVariableName.setDisable(true);
        } else if ("ESPERAR".equals(action)) {
            txtValue.setPromptText("segundos ej: 2");
            txtSelector.setDisable(true);
            txtVariableName.setDisable(true);
        } else if ("SCREENSHOT".equals(action)) {
            txtSelector.setDisable(true);
            txtVariableName.setDisable(true);
        } else {
            txtSelector.setDisable(false);
            txtVariableName.setDisable(false);
            if ("ESCRIBIR".equals(action) || "SELECCIONAR".equals(action)) {
                txtValue.setPromptText("texto a ingresar...");
            } else if ("VERIFICAR_TEXTO".equals(action)) {
                txtValue.setPromptText("texto esperado...");
            } else {
                txtValue.setPromptText("");
            }
        }
    }

    private void onSelectorChanged() {
        lblVerifyResult.setText("— Presiona Verificar para validar el selector");
        lblVerifyResult.setTextFill(Color.web("#666888"));
    }

    private void clearStepFields() {
        txtSelector.clear();
        txtVariableName.clear();
        txtValue.clear();
        txtDescription.clear();
        lblVerifyResult.setText("— Ingresa un selector y presiona Verificar");
        lblVerifyResult.setTextFill(Color.web("#666888"));
    }

    // ─── HELPERS ─────────────────────────────────────────────────────────────

    private void initEngine() {
        stepExecutor    = new StepExecutor(DriverManager.getDriver());
        selectorVerifier = new SelectorVerifier(DriverManager.getDriver());
        xpathInspector  = new XPathInspector(DriverManager.getDriver());
        locatorManager  = new LocatorManager(LOCATORS_PATH);
        featureGenerator = new FeatureGenerator(FEATURES_PATH, LOCATORS_PATH);
    }

    private void setStatus(String message, String color) {
        Platform.runLater(() -> {
            lblStatus.setText(message);
            lblStatus.setTextFill(Color.web(color));
        });
    }

    private void copyToClipboard(String text) {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
        setStatus("📋 Copiado al portapapeles", "#2E75B6");
    }

    private void shutdown() {
        DriverManager.quit();
        Platform.exit();
    }

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#A0A8D8"));
        return lbl;
    }

    private Label fieldLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", 11));
        lbl.setTextFill(Color.web("#888AAA"));
        return lbl;
    }

    private Separator buildSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #333355;");
        return sep;
    }

    private Button styledButton(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
                     "-fx-border-radius: 4; -fx-background-radius: 4; -fx-cursor: hand; " +
                     "-fx-font-size: 11; -fx-padding: 5 10;");
        return btn;
    }

    private String fieldStyle() {
        return "-fx-background-color: #2A2A3E; -fx-text-fill: #CCCCFF; " +
               "-fx-border-color: #444466; -fx-border-radius: 4; " +
               "-fx-prompt-text-fill: #555577;";
    }

    // Entry point para lanzar el panel
    public static void launch(String[] args) {
        Application.launch(RecorderPanel.class, args);
    }
}
