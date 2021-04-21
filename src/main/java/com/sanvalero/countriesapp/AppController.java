package com.sanvalero.countriesapp;

import com.sanvalero.countriesapp.domain.Country;
import com.sanvalero.countriesapp.service.CountriesService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.*;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.sanvalero.countriesapp.util.Checkup.checkInteger;

public class AppController implements Initializable {

    public ListView lvListCountries;
    public ComboBox<String> cbRegions;
    public Label lbName;
    public Label lbRegion;
    public Label lbSubregion;
    public Label lbCapital;
    public Label lbPopulation;
    public Label lbGini;
    public WebView wvFlag;
    public TextField tfPopulation;
    public Button btFilter;
    public ComboBox<String> cbFilterCriteria;
    public Button btMoreInequality;
    public Button btLessInequality;
    public ProgressIndicator piLoading;

    private CountriesService countriesService;
    private ObservableList<Country> listCountries;
    private Country selectedCountry;
    private File file;

    public AppController(){
        countriesService = new CountriesService();
        wvFlag = new WebView();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listCountries = FXCollections.observableArrayList();
        lvListCountries.setItems(listCountries);
        chargeComboBox();
        piLoading.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        getAllCountries();

        wvFlag.setZoom(0.1);
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
        lbSubregion.setText(selectedCountry.getSubregion());
        lbCapital.setText(selectedCountry.getCapital());
        lbPopulation.setText(String.valueOf(selectedCountry.getPopulation()));
        lbGini.setText(String.valueOf(selectedCountry.getGini()));
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

    @FXML
    public void orderByLeastInequalityFirst(Event event){
        List<Country> filterCountries = listCountries.stream()
                .sorted(Comparator.comparingDouble(Country::getGini))
                .collect(Collectors.toList());
        listCountries.clear();
        listCountries.addAll(filterCountries);
    }

    @FXML
    public void orderByMoreInequalityFirst(Event event){
        List<Country> filterCountries = listCountries.stream()
                .sorted(Comparator.comparingDouble(Country::getGini).reversed())
                .collect(Collectors.toList());
        listCountries.clear();
        listCountries.addAll(filterCountries);
    }

    @FXML
    public void exportToCSV(ActionEvent event){
        exportListToCSV();
    }

    @FXML
    public void exportToZIP(ActionEvent event){
        CompletableFuture.supplyAsync(() -> exportListToCSV())
                .thenAcceptAsync(value -> zipFile(file));
    }

    private void chargeComboBox(){
        String[] regions = new String[]{"Todos", "Africa", "Americas", "Asia", "Europe", "Oceania"};
        cbRegions.setItems(FXCollections.observableArrayList(regions));
        String[] filterCriteria = new String[]{">", "<", "="};
        cbFilterCriteria.setItems(FXCollections.observableArrayList(filterCriteria));
    }

    private void getAllCountries(){
        listCountries.clear();
        piLoading.setVisible(true);
                countriesService.getAllCountries()
                        .flatMap(Observable::from)
                        .doOnCompleted(() -> piLoading.setVisible(false))
                        .doOnError(throwable -> System.out.println(throwable.getMessage()))
                        .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                        .subscribe(country -> Platform.runLater(()->listCountries.add(country)));
    }

    private void getCountriesByRegion(String region){
        listCountries.clear();
        piLoading.setVisible(true);
                countriesService.getCountriesByRegion(region)
                        .flatMap(Observable::from)
                        .doOnCompleted(() -> piLoading.setVisible(false))
                        .doOnError(throwable -> System.out.println(throwable.getMessage()))
                        .subscribeOn(Schedulers.from(Executors.newCachedThreadPool()))
                        .subscribe(country -> Platform.runLater(()->listCountries.add(country)));
    }

    private File exportListToCSV(){

        Platform.runLater(()->{
            try {
                FileChooser fileChooser = new FileChooser();
                file = fileChooser.showSaveDialog(lvListCountries.getScene().getWindow());

                FileWriter fileWriter = new FileWriter(file);
                CSVPrinter printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);

                for (Country country: listCountries){
                    printer.printRecord(country.getName(),
                            country.getRegion(),
                            country.getSubregion(),
                            country.getCapital(),
                            country.getPopulation(),
                            country.getGini(),
                            country.getFlag());
                }
                printer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return file;
    }

    private void zipFile(File file){
        try {
            FileOutputStream fos = new FileOutputStream(file.getName() + ".zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            FileInputStream fis = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0){
                zipOut.write(bytes, 0, length);
            }
            zipOut.close();
            fos.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
