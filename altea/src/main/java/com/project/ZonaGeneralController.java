package com.project;

import com.project.ServerModel.Zona;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class ZonaGeneralController {

    @FXML
    private Label txtNom, txtInfo, txtOcupacio;
    @FXML
    private ProgressBar barOcupacio;

    public void setZona(Zona zona, int ocupades) {
        txtNom.setText(zona.nom());
        txtInfo.setText("General · aforament " + zona.aforament());
        txtOcupacio.setText(ocupades + " / " + zona.aforament());
        barOcupacio.setProgress(zona.aforament() == 0 ? 0 : (double) ocupades / zona.aforament());
    }
}
