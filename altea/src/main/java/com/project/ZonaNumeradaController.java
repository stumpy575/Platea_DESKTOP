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

    public void setZona(Zona zona, Set<String> ocupats, Set<String> validats) {
        txtNom.setText(zona.nom());
        txtInfo.setText(String.format("Numerada | %d files x %d seients | %d / %d venuts",
                zona.files(), zona.seientsPerFila(), ocupats.size(), zona.aforament()));

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
                if (validats.contains(id)) {
                    seat.getStyleClass().add("seat-validated");
                } else if (ocupats.contains(id)) {
                    seat.getStyleClass().add("seat-occupied");
                } else {
                    seat.getStyleClass().add("seat-free");
                }
                if (ocupats.contains(id)) {
                    seat.setOnMouseClicked(e -> Main.ctrlMap.showVenda(zona, id));
                }
                seatGrid.add(seat, n, f);
            }
        }
    }
}
