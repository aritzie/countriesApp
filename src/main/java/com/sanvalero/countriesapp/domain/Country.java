package com.sanvalero.countriesapp.domain;

import lombok.Builder;
import lombok.Data;

import java.net.URL;

@Data
@Builder
public class Country {

    private String name;
    private String capital;
    private String region;
    private String subregion;
    private int population;
    private double gini;
    private URL flag;

    @Override
    public String toString() {
        return name + " [" + capital + "]";
    }
}
