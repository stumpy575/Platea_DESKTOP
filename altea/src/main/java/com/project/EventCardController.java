package com.project;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.project.ServerModel.Esdeveniment;
import com.project.ServerModel.TipusEntrada;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class EventCardController {

    private static final Locale CA = Locale.forLanguageTag("ca");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", CA);

    @FXML
    private Label txtCategoria, txtData, txtNom, txtRecinte, txtDescripcio, txtPreu, txtDisponibles;

    @FXML
    private Button btnMapa;

    private Esdeveniment esdeveniment;

    public void setEsdeveniment(Esdeveniment e) {
        this.esdeveniment = e;
        txtCategoria.setText(e.categoria());
        txtData.setText(e.dataHora().format(DATE_FORMAT));
        txtNom.setText(e.nom());
        txtRecinte.setText("Recinte " + e.recinte());
        txtDescripcio.setText(e.descripcio());
        setTipusEntrada(null);
    }

    // el null es que estan carregant
    public void setTipusEntrada(List<TipusEntrada> tipus) {
        if (tipus == null) {
            txtPreu.setText("Carregant entrades...");
            txtDisponibles.setText("");
            btnMapa.setDisable(true);
            return;
        }

        BigDecimal preuMinim = tipus.stream()
                .map(TipusEntrada::preu)
                .min(Comparator.naturalOrder())
                .orElse(null);
        int disponibles = tipus.stream().mapToInt(TipusEntrada::disponibles).sum();

        if (preuMinim == null) {
            txtPreu.setText("Sense entrades");
        } else {
            txtPreu.setText(String.format(CA, "Des de %.2f €", preuMinim));
        }
        txtDisponibles.setText(disponibles > 0 ? disponibles + " entrades disponibles" : "Esgotat");
        btnMapa.setDisable(disponibles <= 0);
    }

    @FXML
    private void openMap() {
        Main.ctrlMap.showEsdeveniment(esdeveniment);
        UtilsViews.setViewAnimating("ViewMap");
    }
}
