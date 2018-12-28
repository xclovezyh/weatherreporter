package com.wb.weatherreporter.gson;

public class AQI {
    public String aqi;

    public String pm25;

    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    @Override
    public String toString() {
        return "AQI{" +
                "aqi='" + aqi + '\'' +
                ", pm25='" + pm25 + '\'' +
                '}';
    }
}
