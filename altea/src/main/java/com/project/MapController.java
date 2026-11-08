package com.project;

import java.net.URL;
import java.util.ResourceBundle;

import com.project.ServerModel.Esdeveniment;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class MapController implements Initializable {

    @FXML
    private Label txtNom, txtRecinte;

    // Called when the FXML file is loaded
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void showEsdeveniment(Esdeveniment e) {
        txtNom.setText(e.nom());
        txtRecinte.setText("Recinte " + e.recinte());
    }

    @FXML
    private void backToEvents() {
        UtilsViews.setViewAnimating("ViewEvents");
    }
}
