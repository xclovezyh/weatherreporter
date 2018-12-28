package com.wb.weatherreporter.gson;

import com.google.gson.annotations.SerializedName;

public class MyLifeStyle {
    public String comfort;

    public String carWash;

    public String sport;

    public String getComfort() {
        return comfort;
    }

    public void setComfort(String comfort) {
        this.comfort = comfort;
    }

    public String getCarWash() {
        return carWash;
    }

    public void setCarWash(String carWash) {
        this.carWash = carWash;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    @Override
    public String toString() {
        return "MyLifeStyle{" +
                "comfort='" + comfort + '\'' +
                ", carWash='" + carWash + '\'' +
                ", sport='" + sport + '\'' +
                '}';
    }
}
