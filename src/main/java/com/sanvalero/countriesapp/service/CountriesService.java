package com.sanvalero.countriesapp.service;


import com.sanvalero.countriesapp.domain.Country;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

import java.util.List;

import static com.sanvalero.countriesapp.util.Constants.URL;

public class CountriesService {

    private CountriesAPIService api;

    public CountriesService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        api = retrofit.create(CountriesAPIService.class);
    }

    public Observable<List<Country>> getAllCountries(){
        return api.getAllCountries();
    }

    public Observable<List<Country>> getCountriesByRegion(String region){
        return api.getCountriesByRegion(region);
    }
}
