package com.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ConfigController {

    private static final String CLICKED = "button-config-place-clicked";
    private static final String UNCLICKED = "button-config-place-unclicked";
    private static final File CONFIG = new File("config.xml");

    @FXML
    private Button taquillaBtn, portaBtn, oficinaBtn, confirmButton;
    @FXML
    private TextField websocketUrlField;

    private List<Button> modeButtons;
    private String selectedMode = null;

    @FXML
    private void initialize() {
        modeButtons = List.of(taquillaBtn, portaBtn, oficinaBtn);
        confirmButton.setDisable(true);
        websocketUrlField.textProperty().addListener((obs, oldV, newV) -> updateConfirm());

        loadConfig();
    }

    // logica per a desar el config.xml
    private void saveConfig(String mode, String url) {
        Properties p = new Properties();
        p.setProperty("mode", mode);
        p.setProperty("websocketUrl", url);
        try (OutputStream os = new FileOutputStream(CONFIG)) {
            p.storeToXML(os, null);
        } catch (IOException e) {
            System.out.println("[ERROR] No s'ha pogut guardar config.xml: " + e.getMessage());
        }
    }

    // logica per a carregar la configuracio en el config.xml
    private void loadConfig() {
        if (!CONFIG.exists()) {
            return;
        }
        Properties p = new Properties();
        try (InputStream is = new FileInputStream(CONFIG)) {
            p.loadFromXML(is);
        } catch (IOException e) {
            System.out.println("[ERROR] No s'ha pogut llegir config.xml: " + e.getMessage());
            return;
        }
        websocketUrlField.setText(p.getProperty("websocketUrl", ""));
        switch (p.getProperty("mode", "")) {
            case "taquilla" -> selectTaquilla();
            case "porta" -> selectPorta();
            case "oficina" -> selectOficina();
        }
    }

    @FXML
    private void selectTaquilla() {
        selectMode("taquilla", taquillaBtn);
    }

    @FXML
    private void selectPorta() {
        selectMode("porta", portaBtn);
    }

    @FXML
    private void selectOficina() {
        selectMode("oficina", oficinaBtn);
    }

    private void selectMode(String mode, Button selected) {
        selectedMode = mode;
        for (Button b : modeButtons) {
            b.getStyleClass().removeAll(CLICKED, UNCLICKED);
            b.getStyleClass().add(b == selected ? CLICKED : UNCLICKED);
        }
        updateConfirm();
    }

    private void updateConfirm() {
        //checa que hagi mode i url
        boolean ready = selectedMode != null && !websocketUrlField.getText().isBlank();
        confirmButton.setDisable(!ready);
    }

    @FXML
    private void confirm() {
        String url = websocketUrlField.getText().trim();

        if (!url.startsWith("ws://") && !url.startsWith("wss://")) {
            System.out.println("[ERROR] La URL ha de començar per ws:// o wss://");
            return;
        }

        Main.mode = selectedMode;
        Main.websocketUrl = url;

        saveConfig(selectedMode, url);
        Main.connectToServer();

        /* String viewId = switch (selectedMode) {
            aqui el canvi a les següents vistes
        };
        */
    }
}