package com.sanvalero.countriesapp;

import com.sanvalero.countriesapp.domain.Country;
import com.sanvalero.countriesapp.service.CountriesService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.sanvalero.countriesapp.util.Checkup.checkInteger;

public class AppController implements Initializable {

    public ListView lvListCountries;
    public ProgressBar pbProgress;
    public ComboBox<String> cbRegions;
    public Label lbName;
    public Label lbRegion;
    public Label lbSubRegion;
    public Label lbCapital;
    public Label lbPopulation;
    public WebView wvFlag;
    public TextField tfPopulation;
    public Button btFilter;
    public ComboBox<String> cbFilterCriteria;

    private CountriesService countriesService;
    private ObservableList<Country> listCountries;
    private Country selectedCountry;

    public AppController(){
        countriesService = new CountriesService();
        wvFlag = new WebView();
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
        if(cbRegions.getSelectionModel().getSelectedItem()==null)return;

        String region = cbRegions.getSelectionModel().getSelectedItem();
        if(region.equals("Todos")){
            getAllCountries();
        } else {
            region.toLowerCase();
            getCountriesByRegion(region);
        }
    }

    @FXML
    public void showCountry(Event event){
        selectedCountry = (Country) lvListCountries.getSelectionModel().getSelectedItem();
        lbName.setText(selectedCountry.getName());
        lbRegion.setText(selectedCountry.getRegion());
//        lbSubRegion.setText(selectedCountry.getSubregion());
        lbCapital.setText(selectedCountry.getCapital());
        lbPopulation.setText(String.valueOf(selectedCountry.getPopulation()));
        wvFlag.getEngine().load(String.valueOf(selectedCountry.getFlag()));
    }

    @FXML
    public void filterByPopulation(Event event){
        String textFieldText = tfPopulation.getText();

        if (!checkInteger(textFieldText))return;
        if (tfPopulation.getText().equals(""))return;
        if (cbFilterCriteria.getSelectionModel().getSelectedItem()==null)return;

        int population = Integer.parseInt(textFieldText);
        System.out.println(population);
        String filterCriteria = cbFilterCriteria.getSelectionModel().getSelectedItem();
        List<Country> filterCountries = null;

        if(filterCriteria.equals(">")){
            filterCountries = listCountries.stream()
                    .filter(country -> country.getPopulation()>population)
                    .collect(Collectors.toList());
        }
        else if(filterCriteria.equals("<")){
            filterCountries = listCountries.stream()
                    .filter(country -> country.getPopulation()<population)
                    .collect(Collectors.toList());
        }
        else {
            filterCountries = listCountries.stream()
                    .filter(country -> country.getPopulation()==population)
                    .collect(Collectors.toList());
        }
        listCountries.clear();
        listCountries.addAll(filterCountries);
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
        String[] filterCriteria = new String[]{">", "<", "="};
        cbFilterCriteria.setItems(FXCollections.observableArrayList(filterCriteria));
    }


}