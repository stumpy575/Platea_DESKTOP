package com.project;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class ConfigController {

    private static final String CLICKED = "button-config-place-clicked";
    private static final String UNCLICKED = "button-config-place-unclicked";

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

    // Marca el botón elegido (clase "clicked") y deja los demás en "unclicked"
    private void selectMode(String mode, Button selected) {
        selectedMode = mode;
        for (Button b : modeButtons) {
            b.getStyleClass().removeAll(CLICKED, UNCLICKED);
            b.getStyleClass().add(b == selected ? CLICKED : UNCLICKED);
        }
        updateConfirm();
    }

    // Confirmar solo se activa con un modo elegido y una URL escrita
    private void updateConfirm() {
        boolean ready = selectedMode != null && !websocketUrlField.getText().isBlank();
        confirmButton.setDisable(!ready);
    }

    @FXML
    private void confirm() {
        Main.mode = selectedMode;
        Main.websocketUrl = websocketUrlField.getText().trim();

        /*String viewId = switch (selectedMode) {
            case "taquilla" -> Main.VIEW_TAQUILLA;
            case "porta" -> Main.VIEW_PUERTA;
            default -> Main.VIEW_OFICINA;
        };

        // Si la vista aún no está registrada en Main, no se cambia de pantalla
        Object ctrl = UtilsViews.getController(viewId);
        if (ctrl == null) {
            System.out.println("La vista " + viewId + " todavía no está añadida en Main");
            return;
        }

        if (ctrl instanceof TaquillaController c) c.onShow();
        else if (ctrl instanceof PuertaController c) c.onShow();
        else if (ctrl instanceof OficinaController c) c.onShow();

        UtilsViews.setViewAnimating(viewId); */
    }
}
