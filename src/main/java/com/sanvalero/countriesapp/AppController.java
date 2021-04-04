package com.sanvalero.countriesapp;

import com.sanvalero.countriesapp.domain.Country;
import com.sanvalero.countriesapp.service.CountriesService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

public class AppController implements Initializable {

    public ListView lvListCountries;
    public ProgressBar pbProgress;
    public ComboBox<String> cbRegions;
    public Label lbName;
    public Label lbRegion;
    public Label lbSubRegion;
    public Label lbCapital;
    public Label lbPopulation;

    private CountriesService countriesService;
    private ObservableList<Country> listCountries;

    public AppController(){
        countriesService = new CountriesService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listCountries = FXCollections.observableArrayList();
        lvListCountries.setItems(listCountries);

        chargeComboBox();

        getAllCountries();
    }

    @FXML
    public void showCountries(ActionEvent event){
        String region = cbRegions.getSelectionModel().getSelectedItem();
        if(region.equals("Todos")){
            getAllCountries();
        } else {
            region.toLowerCase();
            getCountriesByRegion(region);
        }
    }

    private void getAllCountries(){
        listCountries.clear();
        Task<Void> getAllCountriesTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                countriesService.getAllCountries()
                        .flatMap(Observable::from)
                        .doOnCompleted(()->System.out.println("Terminado"))
                        .doOnError(throwable -> System.out.println(throwable.getMessage()))
                        .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                        .subscribe(country -> Platform.runLater(()->listCountries.add(country)));
                return null;
            }
        };
        new Thread(getAllCountriesTask).start();
    }

    private void getCountriesByRegion(String region){
        listCountries.clear();
        Task<Void> getCountriesByRegionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                countriesService.getCountriesByRegion(region)
                        .flatMap(Observable::from)
                        .doOnCompleted(()-> System.out.println("Terminado"))
                        .doOnError(throwable -> System.out.println(throwable.getMessage()))
                        .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                        .subscribe(country -> Platform.runLater(()->listCountries.add(country)));
                return null;
            }
        };
        new Thread(getCountriesByRegionTask).start();
    }

    private void chargeComboBox(){
        String[] regions = new String[]{"Todos", "Africa", "Americas", "Asia", "Europe", "Oceania"};
        cbRegions.setItems(FXCollections.observableArrayList(regions));
    }
}
