package com.project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    final int WINDOW_WIDTH = 700;
    final int WINDOW_HEIGHT = 500;

    public static final String VIEW_CONFIG = "ViewConfig";
    

    static final String FXML_PATH = "/assets/";

    public static String mode = null;          // variable per a si es taquilla, porta o oficina
    public static String websocketUrl = "";



    @Override
    public void start(Stage stage) throws Exception {

        // Carrega la vista inicial des del fitxer FXML
        UtilsViews.addView(getClass(), "viewConfig", FXML_PATH + "viewConfig.fxml");

        Scene scene = new Scene(UtilsViews.parentContainer);
        //si no afegeixo aqui el style no carrega i surt tot sense estil
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Altea");
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();

        // Afegeix una icona només si no és un Mac
        if (!System.getProperty("os.name").contains("Mac")) {
            Image icon = new Image("file:icons/icon.png");
            stage.getIcons().add(icon);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
