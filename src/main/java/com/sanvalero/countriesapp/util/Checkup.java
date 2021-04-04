package com.sanvalero.countriesapp.util;

public class Checkup {

    public static boolean checkInteger(String string){
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException nfe){
            return false;
        }
        return true;
    }
}
