package com.wb.weatherreporter.util;



import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.wb.weatherreporter.gson.AQI;
import com.wb.weatherreporter.gson.Basic;
import com.wb.weatherreporter.gson.MyForecast;
import com.wb.weatherreporter.gson.MyLifeStyle;
import com.wb.weatherreporter.gson.NowWeather;
import com.wb.weatherreporter.gson.Weather;

import java.util.ArrayList;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.air.Air;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.Lifestyle;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;

public class DataSourceParse {
    private final static String TAG = DataSourceParse.class.getSimpleName();

    private static NowWeather nowWeather = new NowWeather();
    private static Basic basic = new Basic();
    private static AQI aqi = new AQI() ;
    private static MyLifeStyle myLifeStyle = new MyLifeStyle();
    private static Weather myWeather = new Weather();
    private static List<MyForecast> myForecastList = new ArrayList<>();

    public static int backCallCount = 0;

    public static void handleForecastWeather(List<Forecast> list){
        myForecastList.clear();
        for (Forecast forecast : list){
            for (ForecastBase forecastBase : forecast.getDaily_forecast()){
                MyForecast myForecast = new MyForecast();
                myForecast.setDate(forecastBase.getDate());
                myForecast.setCond(forecastBase.getCond_txt_d());
                myForecast.setMaxTemperature(forecastBase.getTmp_max());
                myForecast.setMinTemperature(forecastBase.getTmp_min());
                myForecastList.add(myForecast);
            }
        }
    }

    public static void handleAirNow(List<AirNow> list){
        if (list.size() > 0){
            for (AirNow airNow : list){
                aqi.setAqi(airNow.getAir_now_city().getAqi());
                aqi.setPm25(airNow.getAir_now_city().getPm25());
            }
        }
    }

    public static void  handleLifeStyle(List<Lifestyle> list){
        if (list.size() > 0){
            for (Lifestyle lifestyle : list){
                //myLifeStyle = new MyLifeStyle();
                myLifeStyle.setComfort("舒适度：" + lifestyle.getLifestyle().get(0).getTxt());
                myLifeStyle.setCarWash("洗车指数：" + lifestyle.getLifestyle().get(6).getTxt());
                myLifeStyle.setSport("运动建议：" + lifestyle.getLifestyle().get(3).getTxt());
            }
        }
    }

    public static void handleNowWeather(List<Now> list){
        if (list.size() > 0){
            for (Now now : list){
                //nowWeather = new NowWeather();
                nowWeather.setTemperature(now.getNow().getTmp());
                nowWeather.setInformation(now.getNow().getCond_txt());

                //basic = new Basic();
                basic.setCityName(now.getBasic().getLocation());
                basic.setWeatherId(now.getBasic().getCid());
                basic.setUpdateTime(now.getUpdate().getLoc());
                basic.setStatus(now.getStatus());
            }
        }
    }


    public static Weather callBackBuilder(Context context){
        if (backCallCount == 4){
            Weather weather = DataSourceParse.build();
            backCallCount = 0;
            if (weather != null && weather.basic.status.equals("ok")){
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putString("cityName",weather.basic.cityName);
                editor.apply();
            return weather;
            }

        }
        return null;
    }



    public static Weather build(){
        //myWeather = new Weather();
        myWeather.setBasic(basic);
        myWeather.setAqi(aqi);
        myWeather.setNowWeather(nowWeather);
        myWeather.setMyForecastList(myForecastList);
        myWeather.setMyLifeStyle(myLifeStyle);
        return myWeather;
    }
}
