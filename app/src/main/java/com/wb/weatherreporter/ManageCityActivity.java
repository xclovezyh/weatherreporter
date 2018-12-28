package com.wb.weatherreporter;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.wb.weatherreporter.db.ConcernedCity;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class ManageCityActivity extends AppCompatActivity {

    private ListView manageCities;

    private List<ConcernedCity> concernedCityList = new ArrayList<>();

    private List<String> dataList = new ArrayList<>();

    private ArrayAdapter adapter;

    private Button reBackButton;

    private int currentListItemIndex;

    private String currentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_city);

        findAllViewById();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        Intent intent = getIntent();
        currentCity = intent.getStringExtra("cityName");

        getData();
        adapter = new ArrayAdapter(ManageCityActivity.this,android.R.layout.simple_list_item_1,dataList);
        manageCities.setAdapter(adapter);



        reBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        manageCities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ManageCityActivity.this,WeatherActivity.class);
                intent.putExtra("weather_id",dataList.get(position));
                startActivity(intent);
                finish();
            }
        });

        manageCities.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                currentListItemIndex = position;
                showFilterPopup(view);
                return true;
            }
        });
    }

    private void findAllViewById(){
        manageCities = findViewById(R.id.manage_cities);
        reBackButton = findViewById(R.id.re_back_button);
    }

    //从数据库中获取关注的城市名
    private void getData(){
        concernedCityList = LitePal.findAll(ConcernedCity.class);
        for (ConcernedCity city : concernedCityList)
            dataList.add(city.getCityName());
    }

    //弹出长按菜单
    private void showFilterPopup(final View v){
        //新建PopupMenu对象，展开相应的代码
        PopupMenu popup = new PopupMenu(this,v);
        popup.inflate(R.menu.alert_menu);
        popup.show();
        //设置监听事件
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_to_top:
                        String city = dataList.get(currentListItemIndex);
                        dataList.remove(currentListItemIndex);
                        dataList.add(0, city);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(ManageCityActivity.this, "置顶成功", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu_delete:
                        LitePal.deleteAll(ConcernedCity.class,"cityName = ?",dataList.get(currentListItemIndex));
                        dataList.remove(currentListItemIndex);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(ManageCityActivity.this, "取消关注成功", Toast.LENGTH_LONG).show();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }


}
