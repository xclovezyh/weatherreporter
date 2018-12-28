package com.wb.weatherreporter.db;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class ConcernedCity extends LitePalSupport implements Serializable {
    private String CityName;

    public String getCityName() {
        return CityName;
    }

    public ConcernedCity(String cityName) {
        CityName = cityName;
    }

    public void setCityName(String cityName) {
        CityName = cityName;
    }

    @Override
    public String toString() {
        return "ConcernedCity{" +
                "CityName='" + CityName + '\'' +
                '}';
    }
}
