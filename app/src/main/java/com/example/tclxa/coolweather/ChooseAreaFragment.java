package com.example.tclxa.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.tclxa.coolweather.db.City;
import com.example.tclxa.coolweather.db.County;
import com.example.tclxa.coolweather.db.Province;
import com.example.tclxa.coolweather.utils.HttpUtil;
import com.example.tclxa.coolweather.utils.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private Button backButton;
    private TextView titleTextView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    // 省份列表
    private List<Province> provinces;
    // 城市列表
    private List<City> cities;
    // 县区列表
    private List<County> counties;

    // 选中的省份
    private Province selectProvince;
    // 选中的城市
    private City selectCity;
    // 当前选中的级别
    private int selectLevel;



    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        // inflater将碎片布局动态加载进来
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleTextView = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectLevel == LEVEL_PROVINCE){
                    selectProvince = provinces.get(position);
                    queryCity();
                }else if (selectLevel == LEVEL_CITY){
                    selectCity = cities.get(position);
                    queryCounty();
                }else if (selectLevel == LEVEL_COUNTY){
                    // 显示本地区天气信息
                    // 获取天气ID
                    String weather_id = counties.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", weather_id);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectLevel == LEVEL_COUNTY){
                    queryCity();
                }else if (selectLevel == LEVEL_CITY){
                    queryProvince();
                }
            }
        });

        // 默认显示省份列表
        queryProvince();
    }

    /**
     * 查询全国所有省份，优先从数据库查询， 数据库没有的话再从服务器查询
     * */
    private void queryProvince(){
        titleTextView.setText("中国");
        backButton.setVisibility(View.GONE);
        provinces = DataSupport.findAll(Province.class);
        if (provinces.size() > 0){
            // 从数据库查询数据
            // 将省份列表数据装入List中
            dataList.clear();
            for (Province province: provinces) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            // 默认选择第一个
            listView.setSelection(0);
            selectLevel = LEVEL_PROVINCE;
        }else{
            // 从服务器查询数据
            String uri = "http://guolin.tech/api/china";
            queryFromServer(uri, "Province");
        }
    }

    /**
     * 查询该省所有城市，优先从数据库查询， 数据库没有的话再从服务器查询
     * */
    private void queryCity(){
        titleTextView.setText(selectProvince.getProvinceName());
        backButton.setVisibility(View.GONE);
        cities = DataSupport.where("provinceId = ?", String.valueOf(selectProvince.getProvinceCode()))
                .find(City.class);
        if (cities.size() > 0){
            dataList.clear();
            for (City city:cities) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            selectLevel = LEVEL_CITY;
        }else{
            String uri = "http://guolin.tech/api/china/"+selectProvince.getProvinceCode();
            queryFromServer(uri, "City");
        }
    }

    private void queryCounty(){
        titleTextView.setText(selectCity.getCityName());
        backButton.setVisibility(View.GONE);
        counties = DataSupport.where("cityId = ?",
                String.valueOf(selectCity.getCityCode())).find(County.class);
        if (counties.size() > 0){
            dataList.clear();
            for (County county: counties) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            selectLevel = LEVEL_COUNTY;
        }else{
            String uri = "http://guolin.tech/api/china/"+selectProvince.getProvinceCode()+
                    "/"+selectCity.getCityCode();
            queryFromServer(uri, "County");
        }
    }

    // 从服务器上获取数据
    private void queryFromServer(String uri, final String type){
        Log.i("wwww", "queryFromServer: "+uri);
        // 显示进度条
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(uri, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 回到主线成处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    closeProgressDialog();
                                                    Toast.makeText(getContext(), "get data failure",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("Province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("City".equals(type)){
                    result = Utility.handleCityResponse(responseText, selectProvince.getProvinceCode());
                }else if("County".equals(type)){
                    result = Utility.handleCountyResponse(responseText, selectCity.getCityCode());
                }

                // 解析完服务器返回数据后，进行重新加载数据
                if (result){
                    // runOnUiThread()对UI操作，实现将从子线程切换到主线程
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("Province".equals(type)){
                                queryProvince();
                            }else if ("City".equals(type)){
                                queryCity();
                            }else if ("County".equals(type)){
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });
    }

    // 显示进度
    private void showProgressDialog(){
        if (progressDialog == null){
            // 创建进度条对象
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载,请稍等...");
            // 设置不让点击结束加载进度
            progressDialog.setCanceledOnTouchOutside(false);
        }
        // 显示进度
        progressDialog.show();
    }

    // 关闭进度条
    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
