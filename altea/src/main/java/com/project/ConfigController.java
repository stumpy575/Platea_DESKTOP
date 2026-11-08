package com.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.project.ServerModel.Esdeveniment;
import com.project.ServerModel.TipusEntrada;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class ConfigController {

    private static final String CLICKED = "button-config-place-clicked";
    private static final String UNCLICKED = "button-config-place-unclicked";
    private static final File CONFIG = new File("config.xml");

    @FXML
    private Button taquillaBtn, portaBtn, oficinaBtn, confirmButton;
    @FXML
    private TextField websocketUrlField;
    @FXML
    private Label statusLabel;
    @FXML
    private HBox statusRow;

    private List<Button> modeButtons;
    private String selectedMode = null;
    private boolean connected = false;

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

    // type: ok, error o info 
    //defineix l'estil del missatge
    private void showStatus(String type, String text) {
        statusLabel.getStyleClass().removeAll("status-ok", "status-error", "status-info");
        statusLabel.getStyleClass().add("status-" + type);
        statusLabel.setText(text);
        statusRow.setVisible(true);
        statusRow.setManaged(true);
    }

    @FXML
    private void confirm() {
        String url = websocketUrlField.getText().trim();

        if (!url.startsWith("ws://") && !url.startsWith("wss://")) {
            System.out.println("[ERROR] La URL ha de començar per ws:// o wss://");
            showStatus("error", "La URL ha de començar per ws:// o wss://");
            return;
        }

        Main.mode = selectedMode;
        Main.websocketUrl = url;

        saveConfig(selectedMode, url);
        showStatus("info", "Connectant a " + url + "...");
        connectToServer(url);

        /* String viewId = switch (selectedMode) {
            aqui el canvi a les següents vistes
        };
        */
    }

    private void connectToServer(String url) {
        System.out.println("Connectant a " + url + " (mode: " + Main.mode + ") ...");
        connected = false;

        UtilsWS ws = UtilsWS.getSharedInstance(url);
        Main.wsClient = ws;

        ws.onOpen(message -> Platform.runLater(() -> {
            if (ws != Main.wsClient) return;
            connected = true;
            System.out.println("[OK] Connexió establerta: " + message);
            showStatus("ok", "Connexió establerta amb " + url);

            // demana el cataleg aqui
            ws.safeSend(new JSONObject().put("type", "getEventos").toString());
        }));

        ws.onMessage(message -> Platform.runLater(() -> {
            if (ws != Main.wsClient) return;
            try {
                JSONObject obj = new JSONObject(message);
                String type = obj.optString("type");

                if ("eventos".equals(type)) {
                    handleEvents(obj);
                } else if ("tiposEntrada".equals(type)) {
                    handleTipusEntrada(obj);
                } else if ("error".equals(type)) {
                    System.out.println("[SRV ERROR] " + obj.optString("message", "(sense missatge)"));
                } else {
                    System.out.println("[MSG] " + message);
                }
            } catch (JSONException e) {
                System.out.println("Error processant el missatge del servidor: " + e.getMessage());
            }
        }));

        ws.onError(message -> Platform.runLater(() -> {
            if (ws != Main.wsClient) return;
            System.out.println("[ERROR] No s'ha pogut connectar: " + message);
            if (message.contains("is not a valid URI")) {
                showStatus("error", "La URL no és vàlida");
            } else {
                showStatus("error", "No s'ha pogut connectar. Es tornarà a provar cada 5 segons");
            }
        }));

        ws.onClose(message -> Platform.runLater(() -> {
            if (ws != Main.wsClient) return;
            System.out.println("[CLOSE] " + message);
            if (connected) {
                connected = false;
                showStatus("error", "S'ha perdut la connexió. Es tornarà a provar cada 5 segons");
            }
        }));

        ws.connect();
    }

    // els esdeveniments es desen al main i es demana el tipùs d'entrada
    private void handleEvents(JSONObject obj) {
        JSONArray array = obj.optJSONArray("eventos");
        if (array == null) {
            return;
        }

        Main.events.clear();
        Main.tipusEntrada.clear();

        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            try {
                Main.events.add(new Esdeveniment(
                        o.optString("id"),
                        o.optString("nom"),
                        o.optString("recinte"),
                        LocalDateTime.parse(o.optString("dataHora")),
                        o.optString("categoria"),
                        o.optString("descripcio"),
                        o.optString("imatge")));
            } catch (DateTimeParseException e) {
                System.out.println("[CATALEG] Data no vàlida a l'esdeveniment " + o.optString("id")
                        + ": " + o.optString("dataHora"));
            }
        }
        Main.events.sort(Comparator.comparing(Esdeveniment::dataHora));
        System.out.println("[CATALEG] " + Main.events.size() + " esdeveniments rebuts");

        for (Esdeveniment e : Main.events) {
            Main.wsClient.safeSend(new JSONObject()
                    .put("type", "getTiposEntrada")
                    .put("eventoId", e.id())
                    .toString());
        }
    }

    private void handleTipusEntrada(JSONObject obj) {
        String eventoId = obj.optString("eventoId", "");
        JSONArray array = obj.optJSONArray("tiposEntrada");
        if (eventoId.isEmpty() || array == null) {
            return;
        }

        List<TipusEntrada> lista = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            lista.add(new TipusEntrada(
                    o.optString("id"),
                    o.optString("nom"),
                    o.optString("zona"),
                    o.optBigDecimal("preu", BigDecimal.ZERO),
                    o.optInt("disponibles")));
        }
        Main.tipusEntrada.put(eventoId, lista);
        System.out.println("[CATALEG] " + eventoId + ": " + lista.size() + " tipus d'entrada");
    }
}