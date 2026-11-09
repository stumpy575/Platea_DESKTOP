package com.project;

import java.util.Set;

import com.project.ServerModel.Zona;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

public class ZonaNumeradaController {

    @FXML
    private Label txtNom, txtInfo;
    @FXML
    private GridPane seatGrid;

    public void setZona(Zona zona, Set<String> ocupats) {
        txtNom.setText(zona.nom());
        txtInfo.setText(String.format("Numerada | %d files X %d seients = %d",
                zona.files(), zona.seientsPerFila(), zona.files() * zona.seientsPerFila()));

        seatGrid.getChildren().clear();

        for (int f = 0; f < zona.files(); f++) {
            var fila = (char) ('A' + f);

            Label rowLabel = new Label(String.valueOf(fila));
            rowLabel.getStyleClass().add("seat-row-label");
            seatGrid.add(rowLabel, 0, f);

            for (int n = 1; n <= zona.seientsPerFila(); n++) {
                String id = "" + fila + n;

                Region seat = new Region();
                seat.getStyleClass().add("seat");
                seat.getStyleClass().add(ocupats.contains(id) ? "seat-occupied" : "seat-free");
                seatGrid.add(seat, n, f);
            }
        }
    }
}
