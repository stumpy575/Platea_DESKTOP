package com.project;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.project.ServerModel.Entrada;
import com.project.ServerModel.Esdeveniment;
import com.project.ServerModel.Recinte;
import com.project.ServerModel.TipusEntrada;
import com.project.ServerModel.Zona;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MapController implements Initializable {

    @FXML
    private Label txtNom, txtRecinte;
    @FXML
    private HBox zonesPane;
    @FXML
    private VBox panelVenda;
    @FXML
    private Label txtVendaSeient, txtVendaTitular, txtVendaComprador, txtVendaLocalitzador, txtVendaData, txtVendaPreu;
    @FXML
    private Label txtEntrades;
    @FXML
    private GridPane entradesGrid;

    private Esdeveniment esdeveniment;
    private List<Zona> zones = new ArrayList<>();
    private List<Entrada> entrades = new ArrayList<>();

    // Called when the FXML file is loaded
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void showEsdeveniment(Esdeveniment e) {
        esdeveniment = e;
        txtNom.setText(e.nom());

        // aixo primer s'agafa en el configcontroller handlerecintes
        Recinte recinte = Main.recintes.get(e.recinte());
        if (recinte == null) {
            txtRecinte.setText("Recinte " + e.recinte());
            zones = new ArrayList<>();
        } else {
            txtRecinte.setText("Recinte " + recinte.nom());
            zones = recinte.zones();
        }

        // carrefa de dades
        panelVenda.setVisible(false);
        panelVenda.setManaged(false);
        zonesPane.getChildren().clear();
        entradesGrid.getChildren().clear();
        txtEntrades.setText("Carregant entrades...");

        Main.wsClient.safeSend(new JSONObject()
                .put("type", "getEntradasEvento")
                .put("eventoId", e.id())
                .toString());
    }

    // resposta entradasEvento del servidor, ja ve en ordre de compra
    public void handleEntrades(JSONObject obj) {
        if (esdeveniment == null || !esdeveniment.id().equals(obj.optString("eventoId"))) {
            return;
        }
        JSONArray array = obj.optJSONArray("entradas");
        if (array == null) {
            return;
        }

        List<Entrada> llista = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);

            // les entrades generals no tenen fila ni numero
            String seient = null;
            if (!o.isNull("fila") && !o.isNull("numero")) {
                seient = "" + (char) ('A' + o.getInt("fila") - 1) + o.getInt("numero");
            }

            // la zona i el preu surten del tipus d'entrada
            TipusEntrada tipus = tipus(o.optString("tipoEntradaId"));
            llista.add(new Entrada(
                    o.optString("fechaCompra"),
                    o.optString("localizador"),
                    o.optString("tipoEntradaNom"),
                    tipus == null ? "" : tipus.zona(),
                    seient,
                    o.optString("titular"),
                    o.optString("compradorNombre"),
                    tipus == null ? BigDecimal.ZERO : tipus.preu(),
                    o.optString("estado")));
        }

        // per veure per terminal les entrades rebudes
        System.out.println("[ENTRADES] " + esdeveniment.id() + ": " + llista.size() + " entrades rebudes");
        llista.forEach(en -> System.out.println("[ENTRADES] " + en));

        entrades = llista;
        loadZones();
        loadLlistat();
    }

    // una subvista per zona
    private void loadZones() {
        zonesPane.getChildren().clear();

        for (Zona zona : zones) {
            try {
                if ("numerada".equalsIgnoreCase(zona.tipus())) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/zonaNumerada.fxml"));
                    Parent card = loader.load();
                    ZonaNumeradaController ctrl = loader.getController();
                    ctrl.setZona(zona, ocupats(zona), validats(zona));
                    zonesPane.getChildren().add(card);
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/zonaGeneral.fxml"));
                    Parent card = loader.load();
                    ZonaGeneralController ctrl = loader.getController();
                    ctrl.setZona(zona, Math.max(0, zona.aforament() - venudes(zona)));
                    HBox.setHgrow(card, Priority.ALWAYS);
                    zonesPane.getChildren().add(card);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // llistat de les entrades sota el mapa, en ordre de compra
    private void loadLlistat() {
        entradesGrid.getChildren().clear();
        txtEntrades.setText("Entrades venudes (" + entrades.size() + ")");

        String[] titols = {"Data compra", "Localitzador", "Tipus", "Seient", "Titular", "Estat"};
        for (int c = 0; c < titols.length; c++) {
            Label titol = new Label(titols[c]);
            titol.getStyleClass().add("entrades-header");
            entradesGrid.add(titol, c, 0);
        }

        if (entrades.isEmpty()) {
            Label buit = new Label("Encara no hi ha entrades venudes");
            buit.getStyleClass().add("text-petit");
            entradesGrid.add(buit, 0, 1, titols.length, 1);
            return;
        }

        int fila = 1;
        for (Entrada en : entrades) {
            String[] valors = {
                data(en.dataCompra()),
                en.localitzador(),
                en.tipus(),
                en.seient() == null ? "-" : en.seient(),
                en.titular(),
                estat(en.estat())};
            for (int c = 0; c < valors.length; c++) {
                Label cel = new Label(valors[c]);
                cel.getStyleClass().add("entrades-cell");
                entradesGrid.add(cel, c, fila);
            }
            fila++;
        }
    }

    // detall de la venda del seient clicat
    public void showVenda(Zona zona, String seient) {
        Entrada venda = null;
        for (Entrada en : entrades) {
            if (ocupa(en, zona) && seient.equals(en.seient())) {
                venda = en;
            }
        }
        if (venda == null) {
            return;
        }

        txtVendaSeient.setText(zona.nom() + ", seient " + seient);
        txtVendaTitular.setText("Titular: " + venda.titular());
        txtVendaComprador.setText("Comprador: " + venda.comprador());
        txtVendaLocalitzador.setText("Localitzador: " + venda.localitzador());
        txtVendaData.setText("Data de compra: " + data(venda.dataCompra()));
        txtVendaPreu.setText(String.format("Preu: %.2f € (%s)", venda.preu(), estat(venda.estat()).toLowerCase()));
        panelVenda.setVisible(true);
        panelVenda.setManaged(true);
    }

    // una entrada cancel·lada deixa el lloc lliure
    private boolean ocupa(Entrada en, Zona zona) {
        return zona.id().equals(en.zona()) && !"cancelada".equals(en.estat());
    }

    private Set<String> ocupats(Zona zona) {
        Set<String> ocupats = new HashSet<>();
        for (Entrada en : entrades) {
            if (ocupa(en, zona) && en.seient() != null) {
                ocupats.add(en.seient());
            }
        }
        return ocupats;
    }

    // seients amb l'entrada ja validada a la porta
    private Set<String> validats(Zona zona) {
        Set<String> validats = new HashSet<>();
        for (Entrada en : entrades) {
            if (ocupa(en, zona) && en.seient() != null && "validada".equals(en.estat())) {
                validats.add(en.seient());
            }
        }
        return validats;
    }

    private int venudes(Zona zona) {
        int total = 0;
        for (Entrada en : entrades) {
            if (ocupa(en, zona)) {
                total++;
            }
        }
        return total;
    }

    // tipus d'entrada de l'esdeveniment amb aquest id
    private TipusEntrada tipus(String id) {
        List<TipusEntrada> tipus = Main.tipusEntrada.get(esdeveniment.id());
        if (tipus == null) {
            return null;
        }
        for (TipusEntrada t : tipus) {
            if (t.id().equals(id)) {
                return t;
            }
        }
        return null;
    }

    // la data ve com 2026-05-14T18:30:12.345, es deixa en 2026-05-14 18:30
    private String data(String text) {
        String data = text.replace("T", " ");
        return data.length() > 16 ? data.substring(0, 16) : data;
    }

    private String estat(String estat) {
        if ("vendida".equals(estat)) {
            return "Venuda";
        } else if ("reservada".equals(estat)) {
            return "Reservada";
        } else if ("cancelada".equals(estat)) {
            return "Cancel·lada";
        } else if ("validada".equals(estat)) {
            return "Validada";
        }
        return estat;
    }

    @FXML
    private void backToEvents() {
        UtilsViews.setViewAnimating("ViewEvents");
    }
}
