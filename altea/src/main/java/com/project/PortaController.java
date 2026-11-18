package com.project;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class PortaController {

    @FXML
    private TextField localitzadorField;
    @FXML
    private VBox panelResultat;
    @FXML
    private Label txtResultat, txtTitular, txtTipus, txtSeient;

    @FXML
    private void initialize() {
        panelResultat.setVisible(false);
        panelResultat.setManaged(false);
    }

    // per a enviar al server
    @FXML
    private void validar() {
        String localitzador = localitzadorField.getText().trim();
        if (localitzador.isEmpty()) {
            showError("Escriu un localitzador");
            return;
        }
        Main.wsClient.safeSend(new JSONObject()
                .put("type", "validarEntrada")
                .put("localizador", localitzador)
                .toString());
    }

    // rep lo del server
    public void handleValidacio(JSONObject obj) {
        if (!obj.optBoolean("ok")) {
            showError(obj.optString("motivo"));
            return;
        }

        showResultat("ok", "Entrada vàlida");
        txtTitular.setText("Titular: " + obj.optString("titular"));
        txtTipus.setText("Tipus: " + obj.optString("tipoEntradaNom"));
        if (obj.isNull("fila") || obj.isNull("numero")) {
            txtSeient.setText("Seient: sense seient, zona general");
        } else {
            txtSeient.setText("Seient: " + (char) ('A' + obj.getInt("fila") - 1) + obj.getInt("numero"));
        }
        txtTipus.setVisible(true);
        txtSeient.setVisible(true);
        localitzadorField.clear();
    }

    public void showError(String motiu) {
        showResultat("error", "Entrada rebutjada");
        txtTitular.setText("Motiu: " + motiu);
        txtTipus.setVisible(false);
        txtSeient.setVisible(false);
    }

    private void showResultat(String type, String text) {
        txtResultat.getStyleClass().removeAll("status-ok", "status-error");
        txtResultat.getStyleClass().add("status-" + type);
        txtResultat.setText(text);
        panelResultat.setVisible(true);
        panelResultat.setManaged(true);
    }

    @FXML
    private void backToConfig() {
        UtilsViews.setViewAnimating("viewConfig");
    }
}
