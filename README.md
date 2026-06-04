# Serenity Visual Recorder

Herramienta de grabacion visual de pruebas automatizadas con generacion automatica de Gherkin y archivos `.locators`. Permite grabar flujos de usuario directamente desde el navegador sin escribir codigo, generando casos de prueba listos para ejecutarse con Serenity BDD + Cucumber.

---

## Como funciona

    MODO 1 - GRABACION
    Abre Chrome + Panel visual → Usuario inspecciona elementos →
    Se captura XPath automaticamente → Se generan .feature y .locators

    MODO 2 - EJECUCION
    Lee el .feature generado → Resuelve variables del .locators →
    Ejecuta con Serenity BDD → Genera reporte HTML

---

## Pre-requisitos

    Java 17+
    Verificar : java --version
    Descargar : https://adoptium.net

    Maven 3.8+
    Verificar : mvn --version
    Mac       : brew install maven
    Windows   : https://maven.apache.org/download.cgi

    Google Chrome (cualquier version reciente)
    ChromeDriver se descarga automaticamente

    macOS: permiso de accesibilidad para osascript
    Ir a: Ajustes > Privacidad > Accesibilidad
    Agregar la Terminal o el IDE que uses

---

## Instalacion

    # 1. Clonar el repositorio
    git clone https://github.com/tu-usuario/serenity-visual-recorder.git

    # 2. Ingresar a la carpeta
    cd serenity-visual-recorder

    # 3. Descargar dependencias
    mvn dependency:resolve

---

## Modo 1 - Grabar un caso de prueba

Dar permisos al script de ejecucion (solo la primera vez):

    chmod +x run.sh

Ejecutar el panel:

    ./run.sh

Se abriran dos ventanas:
- Google Chrome (controlado por el driver)
- Panel de control Serenity Visual Recorder

### Flujo de grabacion

    1. Ingresar la URL en el campo superior y presionar "Ir"

    2. INSPECTOR DE ELEMENTO
       - Presionar "Seleccionar elemento en Chrome"
       - Chrome pasa al frente automaticamente
       - Hacer hover sobre el elemento (se resalta en naranja)
       - Hacer click en el elemento
       - El panel regresa al frente con el XPath capturado
       - Verificar el nombre de variable sugerido (editable)
       - Presionar "Verificar selector" para confirmar
         que el elemento se resalta en verde en Chrome

    3. DEFINIR STEP
       - Elegir la accion del combo (CLICK, ESCRIBIR, etc)
       - Completar el valor si aplica (texto, URL, segundos)
       - Agregar descripcion opcional
       - Presionar "EJECUTAR Y GUARDAR STEP"
         El step se ejecuta en Chrome y se guarda en la lista

    4. Repetir pasos 2 y 3 para cada accion del flujo

    5. Completar los nombres de Feature y Scenario

    6. Presionar "GENERAR ARCHIVOS"
       Se generan automaticamente:
         src/test/resources/features/<nombre>.feature
         src/test/resources/locators/recorded.locators

---

## Acciones disponibles

    NAVEGAR          Navegar a una URL
    CLICK            Hacer click en un elemento
    ESCRIBIR         Escribir texto en un campo
    LIMPIAR          Limpiar el contenido de un campo
    SELECCIONAR      Seleccionar opcion de un dropdown
    VERIFICAR_TEXTO  Verificar que un elemento contiene texto
    VERIFICAR_EXISTE Verificar que un elemento es visible
    VERIFICAR_NO_EXISTE Verificar que un elemento no existe
    ESPERAR          Esperar N segundos
    SCREENSHOT       Tomar captura de pantalla

---

## Formato de archivos generados

### .feature generado

    Feature: Login SauceDemo

      Scenario: Login exitoso con usuario standard
        Given el usuario navega a "https://www.saucedemo.com"
        When el usuario escribe "standard_user" en "{input_user_name}"
        When el usuario escribe "secret_sauce" en "{input_password}"
        When el usuario hace click en "{button_login_button}"
        Then el usuario verifica el texto "Products" en "{span_title}"

### .locators generado

    # Archivo generado por Serenity Visual Recorder
    # formato: nombre_variable:@:selector

    input_user_name:@://*[@id="user-name"]
    input_password:@://*[@id="password"]
    button_login_button:@://*[@id="login-button"]
    span_title:@://*[@class="title"]

---

## Modo 2 - Ejecutar el caso grabado

    mvn clean verify

El reporte HTML se abre automaticamente al finalizar.

Si no abre automaticamente:

    open target/site/serenity/index.html        (Mac)
    start target/site/serenity/index.html       (Windows)

---

## Estructura del proyecto

    serenity-visual-recorder/
    pom.xml                              Dependencias y plugins Maven
    run.sh                               Script de arranque rapido
    README.md                            Este archivo
    src/
      main/java/com/recorder/
        engine/
          DriverManager.java             Maneja Chrome con Selenium
          XPathInspector.java            Captura XPath con JS
          SelectorVerifier.java          Verifica y resalta selectores
          StepExecutor.java              Ejecuta cada step en Chrome
        generator/
          FeatureGenerator.java          Genera el archivo .feature
        locator/
          LocatorManager.java            Lee y escribe el .locators
        model/
          RecordedStep.java              Modelo de step grabado
        ui/
          RecorderPanel.java             Panel visual JavaFX
          RecorderLauncher.java          Entry point
      test/java/com/recorder/
        steps/
          GeneratedSteps.java            Step definitions genericos
        runner/
          RecorderRunner.java            JUnit runner para Serenity
      test/resources/
        features/                        Features generados aqui
        locators/                        Locators generados aqui
        serenity.conf                    Configuracion Serenity BDD

---

## Tecnologias

    Serenity BDD  4.1.4
    Selenium      incluido en Serenity
    Cucumber      incluido en Serenity
    JUnit         4.13.2
    JavaFX        21.0.2 (mac-aarch64)
    Java          17+
    Maven         3.8+

---

## Reporte Serenity

El reporte generado en target/site/serenity/index.html incluye:

    - Resultado de cada escenario (pass/fail)
    - Captura de pantalla por cada step
    - Captura final en caso de fallo
    - Tiempo de ejecucion
    - Stack trace detallado en caso de error

---

## Notas

    - ChromeDriver se descarga automaticamente al ejecutar
    - El archivo .locators persiste entre sesiones de grabacion
    - Si una variable ya existe en .locators no se sobreescribe
    - Presionar ESC durante la inspeccion cancela la seleccion
    - El inspector resalta en naranja al hacer hover
    - El inspector resalta en verde al seleccionar correctamente
    - El selector falla: se muestra en rojo en el panel

