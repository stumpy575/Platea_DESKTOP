package com.project;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import com.project.ServerModel.Esdeveniment;
import com.project.ServerModel.Zona;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class MapController implements Initializable {

    @FXML
    private Label txtNom, txtRecinte;
    @FXML
    private HBox zonesPane;

    // dades de prova
    private static final List<Zona> ZONES_DEMO = List.of(
            new Zona("Z1", "Platea", "numerada", 60, 6, 15),
            new Zona("Z2", "Dempeus", "general", 40, 0, 0));

    private static final Set<String> OCUPATS_DEMO = Set.of(
            "A3", "A4", "A7", "B2", "B3", "B4", "B8", "C5", "C6",
            "D1", "D9", "D10", "E4", "E5", "E6", "E7", "F2", "F10");

    private static final int OCUPADES_GENERAL_DEMO = 14;



    // Called when the FXML file is loaded
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void showEsdeveniment(Esdeveniment e) {
        txtNom.setText(e.nom());
        txtRecinte.setText("Recinte " + e.recinte());
        loadZones(ZONES_DEMO);
    }

    // una subvista per zona
    private void loadZones(List<Zona> zones) {
        zonesPane.getChildren().clear();

        for (Zona zona : zones) {
            try {
                if ("numerada".equalsIgnoreCase(zona.tipus())) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/zonaNumerada.fxml"));
                    Parent card = loader.load();
                    ZonaNumeradaController ctrl = loader.getController();
                    ctrl.setZona(zona, OCUPATS_DEMO);
                    zonesPane.getChildren().add(card);
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/zonaGeneral.fxml"));
                    Parent card = loader.load();
                    ZonaGeneralController ctrl = loader.getController();
                    ctrl.setZona(zona, OCUPADES_GENERAL_DEMO);
                    HBox.setHgrow(card, Priority.ALWAYS);
                    zonesPane.getChildren().add(card);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void backToEvents() {
        UtilsViews.setViewAnimating("ViewEvents");
    }
}
