package com.wb.weatherreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("cityName",null) != null){
            Intent intent = new Intent(this,WeatherActivity.class);
            intent.putExtra("weather_id",prefs.getString("cityName",null));
            startActivity(intent);
            this.finish();
        }
        /**
         * 定义图片与天气的映射并存入文件中
         * 如果文件weather_pic存在，则直接跳过，不执行代码
         * 如果文件weather_pic不存在，，创建文件，并且将下面的内容放入weather_pic文件中
         */
        SharedPreferences weatherPicPrefs = getSharedPreferences("weather_pic",MODE_PRIVATE);
        if (weatherPicPrefs.getString("isExit",null) == null){
            Log.d(TAG, "onCreate: 添加天气图标文件成功！");
            SharedPreferences.Editor  editor = getSharedPreferences("weather_pic",MODE_PRIVATE).edit();
            editor.putString("isExit","文件已经存在啦！");
            editor.putInt("晴",R.mipmap.sunny);
            editor.putInt("多云",R.mipmap.cloudy);
            editor.putInt("阴",R.mipmap.overcast);
            editor.putInt("小雨",R.mipmap.light_rain);
            editor.putInt("中雨",R.mipmap.moderate_rain);
            editor.putInt("大雨",R.mipmap.heavy_rain);
            editor.putInt("雨",R.mipmap.rain);
            editor.putInt("小雪",R.mipmap.light_snow);
            editor.putInt("中雪",R.mipmap.moderate_snow);
            editor.putInt("大雪",R.mipmap.heavy_snow);
            editor.putInt("雪",R.mipmap.snow);
            editor.putInt("雾",R.mipmap.foggy);
            editor.putInt("浓雾",R.mipmap.dense_fog);
            editor.putInt("大雾",R.mipmap.heavy_fog);
            editor.putInt("热",R.mipmap.hot);
            editor.putInt("冷",R.mipmap.cold);
            editor.putInt("未知",R.mipmap.unknown);
            editor.apply();
        }
    }


}