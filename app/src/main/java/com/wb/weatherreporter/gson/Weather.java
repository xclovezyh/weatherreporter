package com.wb.weatherreporter.gson;

import java.util.List;

public class Weather {
    /**
     * 创建类的引用
     */
    public  Basic basic;
    public AQI aqi;
    public NowWeather nowWeather;
    public MyLifeStyle MyLifeStyle;

    //用一个集合来存放Forecast类
    public List<MyForecast> myForecastList;

    public Basic getBasic() {
        return basic;
    }

    public void setBasic(Basic basic) {
        this.basic = basic;
    }

    public AQI getAqi() {
        return aqi;
    }

    public void setAqi(AQI aqi) {
        this.aqi = aqi;
    }

    public NowWeather getNowWeather() {
        return nowWeather;
    }

    public void setNowWeather(NowWeather nowWeather) {
        this.nowWeather = nowWeather;
    }

    public com.wb.weatherreporter.gson.MyLifeStyle getMyLifeStyle() {
        return MyLifeStyle;
    }

    public void setMyLifeStyle(com.wb.weatherreporter.gson.MyLifeStyle myLifeStyle) {
        MyLifeStyle = myLifeStyle;
    }

    public List<MyForecast> getMyForecastList() {
        return myForecastList;
    }

    public void setMyForecastList(List<MyForecast> myForecastList) {
        this.myForecastList = myForecastList;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "basic=" + basic +
                ", aqi=" + aqi +
                ", nowWeather=" + nowWeather +
                ", MyLifeStyle=" + MyLifeStyle +
                ", myForecastList=" + myForecastList +
                '}';
    }
}
