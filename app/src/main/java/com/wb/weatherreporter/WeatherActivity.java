package com.wb.weatherreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wb.weatherreporter.db.ConcernedCity;
import com.wb.weatherreporter.gson.MyForecast;
import com.wb.weatherreporter.gson.Weather;
import com.wb.weatherreporter.service.AutoUpdateService;
import com.wb.weatherreporter.util.DataSourceParse;
import com.wb.weatherreporter.util.HttpUtil;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.air.now.AirNow;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.Forecast;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.Lifestyle;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 用于显示天气信息的类
 */
public class WeatherActivity extends AppCompatActivity {
    private final String TAG = WeatherActivity.class.getSimpleName();

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;
    private ImageView weatherPicImg;
    public SwipeRefreshLayout swipeRefresh;
    private ImageButton addFocusButton;

    private String weatherId;

    public DrawerLayout drawerLayout;
    private Button navButton;

    private List<ConcernedCity> concernedCityList  = new ArrayList<>();

    //用于确定两次返回键的间隔时间
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //配置服务器和个人信息
        HeConfig.init("HE1812191103131227","909fe3de402b48b0b18fdbce8d974e41");
        HeConfig.switchToFreeServerNode();
        //获取布局界面上的控件
        findAllViewById();

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        //注册刷新监听事件
        refreshListener();

        //设置必应的背景图片为APP的背景图片
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bing_pic",null);
        /**
         * 如果获取的字符串不为空，通过Glide加载图片
         * 如果为空，动态获取必应网站的图片
         */
        if (bingPic != null){
            Log.d(TAG, "onResponse: "+bingPic);
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

        /**
         * 每个activity都对应一个窗口window，这个窗口是PhoneWindow的实例，PhoneWindow对应的布局是DecirView，
         * 是一个FrameLayout，DecorView内部又分为两部分，一部分是ActionBar，另一部分是ContentParent，
         * 即activity在setContentView对应的布局。
         *
         * decorView.setSystemUiVisibility解释：
         * 效果：活动的布局会显示在状态栏上面
         * View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN：Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住。
         * View.SYSTEM_UI_FLAG_LAYOUT_STABLE：这个标志来帮助你的应用维持一个稳定的布局。
         */
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            //将状态栏设置为透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        //对选择城市按钮添加一个监听事件
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开滑动菜单
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        titleUpdateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WeatherActivity.this,ManageCityActivity.class);
                String cityName = titleCity.getText().toString();
                intent.putExtra("cityName",cityName);
                startActivity(intent);
            }
        });

        //添加关注按钮监听事件
        addFocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String focusCityName = titleCity.getText().toString();
                if (!checkContains(focusCityName)) {
                    ConcernedCity city = new ConcernedCity(focusCityName);
                    city.save();
                    addFocusButton.setImageDrawable(getResources().getDrawable(R.drawable.success));
                    Toast.makeText(WeatherActivity.this, "关注成功", Toast.LENGTH_SHORT).show();
                }else {
                    LitePal.deleteAll(ConcernedCity.class,"cityName = ?",focusCityName);
                    addFocusButton.setImageDrawable(getResources().getDrawable(R.drawable.add_focus));
                    Toast.makeText(WeatherActivity.this, "取消关注成功", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private boolean checkContains(String weatherId){
        concernedCityList = LitePal.findAll(ConcernedCity.class);

        for (ConcernedCity city : concernedCityList){
            if (city.getCityName().equals(weatherId)){
                return true;
            }
        }
        return false;
    }

    //捕获onKeyDown事件，如果是为了退出程序，调用exit()函数，判断是否符合退出条件
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    //如果两次按返回键的时间间隔超过两秒，则显示再按一次退出程序，否则结束活动，正常退出程序
    private void exit() {
        if (System.currentTimeMillis() - exitTime > 2000){
            Toast.makeText(getApplicationContext(),"再按一次退出程序",Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        }else {
            finish();
            System.exit(0);
        }
    }

    private void refreshListener() {
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                weatherId = prefs.getString("cityName",null);
                getMessage(weatherId);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        weatherId = intent.getStringExtra("weather_id");
        weatherLayout.setVisibility(View.VISIBLE);
        getMessage(weatherId);
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOKHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //刷新背景图片
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    public void findAllViewById() {
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        weatherPicImg = (ImageView) findViewById(R.id.weather_pic);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        addFocusButton = (ImageButton) findViewById(R.id.add_focus);

    }

    private void showWeatherInfo(Weather weather) {
        //激活AutoUpdateService服务
        Intent intent = new Intent(WeatherActivity.this, AutoUpdateService.class);
        startService(intent);

        String cityName = weather.basic.cityName;
        //String updateTime = weather.basic.updateTime.split(" ")[1];
        //本用于显示服务器数据更新时间，后被用作进入城市管理界面
        String updateTime = "城市管理";
        String degree = weather.nowWeather.temperature + " ℃";
        String weatherInfo = weather.nowWeather.information;

        if (checkContains(weather.basic.cityName))
            addFocusButton.setImageDrawable(getResources().getDrawable(R.drawable.success));
        else
            addFocusButton.setImageDrawable(getResources().getDrawable(R.drawable.add_focus));

        //通过weatherInfo字段为key，获取对应的value值，并通过Glide加载图片
        SharedPreferences prefs = getSharedPreferences("weather_pic",MODE_PRIVATE);
        int loadPic = prefs.getInt(weatherInfo,R.mipmap.unknown);

        //动态的加载天气图标
        //loadWeatherPic(loadPicUrl);
        weatherPicImg.setImageDrawable(getResources().getDrawable(loadPic));


        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();
        for (MyForecast myForecast : weather.myForecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);

            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);

            dateText.setText(myForecast.date);
            infoText.setText(myForecast.cond);
            maxText.setText(myForecast.maxTemperature);
            minText.setText(myForecast.minTemperature);

            forecastLayout.addView(view);
        }

        if (weather.aqi != null){
            aqiText.setText(weather.aqi.aqi);
            pm25Text.setText(weather.aqi.pm25);
        }else {
            aqiText.setText(0);
            pm25Text.setText(0);
        }

        comfortText.setText(weather.MyLifeStyle.comfort);
        carWashText.setText(weather.MyLifeStyle.carWash);
        sportText.setText(weather.MyLifeStyle.sport);
    }


    /*private void loadWeatherPic(String loadPicUrl){
        Glide.with(WeatherActivity.this).load(loadPicUrl).fitCenter().into(weatherPicImg);
    }*/



    public void getMessage(final String weatherId){
        loadBingPic();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("cityName",weatherId);
        editor.apply();

        HeWeather.getWeatherForecast(getBaseContext(), weatherId, Lang.CHINESE_SIMPLIFIED, Unit.METRIC,
                new HeWeather.OnResultWeatherForecastBeanListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.d(TAG, "onError、getWeatherForecast: "+throwable);
                    }

                    @Override
                    public void onSuccess(final List<Forecast> list) {
                        DataSourceParse.handleForecastWeather(list);
                        DataSourceParse.backCallCount++;
                        final Weather weather = DataSourceParse.callBackBuilder(getBaseContext());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (weather != null && weather.basic.status.equals("ok")){
                                    showWeatherInfo(weather);
                                    swipeRefresh.setRefreshing(false);
                                }
                            }
                        });
                    }
                });

        HeWeather.getAirNow(getBaseContext(), weatherId, Lang.CHINESE_SIMPLIFIED, Unit.METRIC,
                new HeWeather.OnResultAirNowBeansListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        DataSourceParse.backCallCount++;
                        final Weather weather = DataSourceParse.callBackBuilder(getBaseContext());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (weather != null){
                                    showWeatherInfo(weather);
                                    swipeRefresh.setRefreshing(false);
                                }
                            }
                        });
                        Log.d(TAG, "onError、getAirNow: "+throwable);
                    }

                    @Override
                    public void onSuccess(final List<AirNow> list) {
                        DataSourceParse.handleAirNow(list);
                        DataSourceParse.backCallCount++;
                        final Weather weather = DataSourceParse.callBackBuilder(getBaseContext());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (weather != null){
                                    showWeatherInfo(weather);
                                    swipeRefresh.setRefreshing(false);
                                }
                            }
                        });
                    }
                });

        HeWeather.getWeatherLifeStyle(getBaseContext(), weatherId, Lang.CHINESE_SIMPLIFIED, Unit.METRIC,
                new HeWeather.OnResultWeatherLifeStyleBeanListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.d(TAG, "onError、getWeatherLifeStyle: "+throwable);
                    }

                    @Override
                    public void onSuccess(final List<Lifestyle> list) {
                        DataSourceParse.handleLifeStyle(list);
                        DataSourceParse.backCallCount++;
                        final Weather weather = DataSourceParse.callBackBuilder(getBaseContext());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (weather != null && weather.basic.status.equals("ok")){
                                    showWeatherInfo(weather);
                                    swipeRefresh.setRefreshing(false);
                                }
                            }
                        });
                    }
                });

        HeWeather.getWeatherNow(getBaseContext(), weatherId, Lang.CHINESE_SIMPLIFIED, Unit.METRIC,
                new HeWeather.OnResultWeatherNowBeanListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onSuccess(final List<Now> list) {
                        DataSourceParse.handleNowWeather(list);
                        DataSourceParse.backCallCount++;
                        final Weather weather = DataSourceParse.callBackBuilder(getBaseContext());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (weather != null && weather.basic.status.equals("ok")){
                                    showWeatherInfo(weather);
                                    swipeRefresh.setRefreshing(false);
                                }
                            }
                        });
                    }
                });
    }


}
