package com.sanvalero.countriesapp;

import com.sanvalero.countriesapp.service.CountriesService;
import com.sanvalero.countriesapp.util.R;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class App extends Application {
    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) throws Exception {
        AppController controller = new AppController();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(R.getUI("countries_app.fxml"));
        loader.setController(controller);

        VBox vBox = loader.load();
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main (String[] args){launch();}
}
