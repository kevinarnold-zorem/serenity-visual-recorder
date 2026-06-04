# Generado por Serenity Visual Recorder
# Fecha: 2026-06-04 09:15:21
# Locators: src/test/resources/locators/recorded.locators

Feature: Flujo grabado

  Scenario: Escenario grabado
    Given el usuario navega a "https://www.saucedemo.com/"
    When el usuario escribe "standard_user" en "{input_user_name}"
    And el usuario escribe "secret_sauce" en "{input_password}"
    And el usuario hace click en "{input_login_button}"
    And el usuario hace click en "{button_add_to_cart_sauce_labs_backpack}"
    And el usuario hace click en "{button_add_to_cart_sauce_labs_bike_light}"
    And el usuario hace click en "{a_shopping_cart_link}"
    And el usuario hace click en "{button_checkout}"
    And el usuario escribe "Jhon" en "{input_first_name}"
    And el usuario escribe "Doe" en "{input_last_name}"
    And el usuario escribe "110002" en "{input_postal_code}"
    And el usuario hace click en "{input_continue}"
    And el usuario hace click en "{button_finish}"
    Then el usuario verifica el texto "Thank you for your order!" en "{h2_complete_header}"

