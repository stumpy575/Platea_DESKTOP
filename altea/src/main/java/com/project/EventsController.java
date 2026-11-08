package com.project;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.project.ServerModel.Esdeveniment;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class EventsController implements Initializable {

    @FXML
    private VBox yPane;
    @FXML
    private Label txtResum;

    private final Map<String, EventCardController> cards = new HashMap<>();

    // Called when the FXML file is loaded
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtResum.setText("Esperant el cataleg...");
    }

    public void setEvents(List<Esdeveniment> events) {
        URL resource = this.getClass().getResource("/assets/eventCard.fxml");

        yPane.getChildren().clear();
        cards.clear();

        for (Esdeveniment e : events) {
            try {
                FXMLLoader loader = new FXMLLoader(resource);
                Parent card = loader.load();
                EventCardController cardController = loader.getController();

                cardController.setEsdeveniment(e);
                cardController.setTipusEntrada(Main.tipusEntrada.get(e.id()));

                yPane.getChildren().add(card);
                cards.put(e.id(), cardController);
            } catch (IOException ex) {
                System.out.println("[ERROR] No s'ha pogut carregar la card de " + e.id() + ": " + ex.getMessage());
            }
        }

        txtResum.setText(events.size() == 1 ? "1 esdeveniment" : events.size() + " esdeveniments");
    }

    public void updateTipusEntrada(String eventId) {
        EventCardController card = cards.get(eventId);
        if (card != null) {
            card.setTipusEntrada(Main.tipusEntrada.get(eventId));
        }
    }

    @FXML
    private void backToConfig() {
        UtilsViews.setViewAnimating("viewConfig");
    }
}
