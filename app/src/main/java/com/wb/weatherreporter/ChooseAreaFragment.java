package com.wb.weatherreporter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wb.weatherreporter.db.City;
import com.wb.weatherreporter.db.ConcernedCity;
import com.wb.weatherreporter.db.County;
import com.wb.weatherreporter.db.Province;
import com.wb.weatherreporter.util.HttpUtil;
import com.wb.weatherreporter.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    //省、市、县对应的等级
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 3;

    private ProgressDialog progressDialog;
    //界面控件
    private TextView titleText;
    private Button backButton;
    private ListView listView;

    //数组适配器，将List集合与ListView绑定数据源
    private ArrayAdapter<String> adapter;

    //用于存储当前显示的数据集合
    private List<String> dataList = new ArrayList<>();
    /**
     * 从数据库查询出来的数据集合
     * provinceList：省列表
     * cityList：市列表
     * countyList：县列表
     */
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    /**
     * selectedProvince：选中的省份
     * selectedCity：选中的城市
     */
    private Province selectedProvince;
    private City selectedCity;
    //当前选中的级别
    private int currentLevel;


    private List<ConcernedCity> concernedCityList = new ArrayList<ConcernedCity>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_area,container,false);
        findAllViewById(view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    /**
     * 从界面获取控件
     * @param view
     */
    private void findAllViewById(View view) {
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
    }

    /**
     *当活动创建成功时执行：对ListView中的元素添加点击事件，对返回按钮注册点击事件
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(i);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(i);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    final String weatherId = countyList.get(i).getCountyName();
                    queryConcernedCity();

                    /**
                     * （确定选择完城市的时候）
                     * 当碎片加载在MainActivity的时候、通过Intent进行跳转
                     * 当碎片加载在WeatherActivity的时候，关闭滑动菜单，显示下拉刷新进度条，然后请求新的城市信息
                     */
                    if (getActivity() instanceof MainActivity){
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.getMessage(weatherId);
                    }

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private boolean checkContains(String weatherId){
        for (ConcernedCity city : concernedCityList){
            if (city.getCityName().equals(weatherId)){
                return true;
            }
        }
        return false;
    }

    private void queryConcernedCity(){
        concernedCityList = LitePal.findAll(ConcernedCity.class);
        for (ConcernedCity city : concernedCityList)
            Log.d("ChooseAreaFragment", "queryConcernedCity: "+city.getCityName());
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces(){
        titleText.setText("中国");
        //让返回按钮不可见
        backButton.setVisibility(View.GONE);
        //通过LitePal读取数据库中的所有数据
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0 ){
            //如果查询出的数据集合不为空，则清空当前显示界面的数据集
            dataList.clear();
            //将需要显示的数据重新添加到dataList数据集中
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            //数据源的数据发生改变了，通知适配器，更新界面
            adapter.notifyDataSetChanged();
            //通过下标定位ListView显示的位置，设置为0，从头开始显示
            listView.setSelection(0);
            //设置当前的等级
            currentLevel = LEVEL_PROVINCE;
        }else {
            /**
             * 如果查询数据库为空，则从服务器上获取对应的数据
             */
            //设置查询的URL
            String address = "http://guolin.tech/api/china";
            //调用从数据库查询的方法
            queryFromServer(address,"province");
        }
    }

    /**
     * 查询选中省内的所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size() > 0 ){
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询选中市内的所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size() > 0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省、市、县数据
     * @param address 查询的URL地址
     * @param type 查询的类型
     */
    private void queryFromServer(String address,final String type) {
        //开启ProgressDialog
        showProgressDialog();
        HttpUtil.sendOKHttpRequest(address, new Callback() {
            /**
             * 请求失败时执行该方法
             */
            @Override
            public void onFailure(Call call,final IOException e) {
                /**
                 * 通过runOnUiThread()方法回到主线程的处理逻辑
                 */
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * 从服务器查询数据失败，关闭ProgressDialog，显示加载失败信息
                         */
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            /**
             * 请求成功时执行
             */
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //获取响应体中的字符串数据
                String responseText = response.body().string();
                boolean result = false;
                /**
                 * 根据对应的等级调用对应的字符串解析工具中的方法
                 */
                if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    /**
                     * 通过runOnUiThread()方法回到主线程的处理逻辑
                     */
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 创建、展示ProgressDialog
     */
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 如果ProgressDialog不为空，调用该方法时关闭ProgressDialog
     */
    private void closeProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}
