package com.sanvalero.countriesapp.service;

import com.sanvalero.countriesapp.domain.Country;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

import java.util.List;

public interface CountriesAPIService {

    @GET("/rest/v2/all")
    Observable<List<Country>> getAllCountries();

    @GET("/rest/v2/region/{region}")
    Observable<List<Country>> getCountriesByRegion(@Path("region") String region);
}
