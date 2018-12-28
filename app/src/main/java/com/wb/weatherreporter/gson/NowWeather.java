package com.wb.weatherreporter.gson;

import com.google.gson.annotations.SerializedName;

public class NowWeather {
    public String temperature;

    public String information;

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    @Override
    public String toString() {
        return "NowWeather{" +
                "temperature='" + temperature + '\'' +
                ", information='" + information + '\'' +
                '}';
    }
}
