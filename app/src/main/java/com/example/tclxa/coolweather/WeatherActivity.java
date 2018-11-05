package com.example.tclxa.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tclxa.coolweather.gson.Forescast;
import com.example.tclxa.coolweather.gson.Weather;
import com.example.tclxa.coolweather.utils.HttpUtil;
import com.example.tclxa.coolweather.utils.Utility;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.example.tclxa.coolweather.utils.Utility.handleWeatherResponse;

public class WeatherActivity extends AppCompatActivity {

    private ImageView imageView;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView updateTime;
    private TextView nowTemp;
    private TextView weatherInfo;
    private LinearLayout forcastLayout;
    private TextView AQText;
    private TextView PM25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        // 解决背景图和状态栏没有完全融合问题
        if (Build.VERSION.SDK_INT > 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        // 初始化控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_id);
        titleCity = (TextView) findViewById(R.id.title_city);
        updateTime = (TextView) findViewById(R.id.title_update_time);
        nowTemp = (TextView) findViewById(R.id.degree_text);
        weatherInfo = (TextView) findViewById(R.id.weather_info_text);
        forcastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        AQText = (TextView) findViewById(R.id.aqi_text);
        PM25Text = (TextView) findViewById(R.id.pm_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        imageView = (ImageView) findViewById(R.id.bing_pic_id);

        // 从本地文件存储数据库中获取读取数据
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = preferences.getString("weather",null);
        if (weatherString != null){
            // 本地有缓存则直接处理数据
            Weather weather = handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{
            // 从服务器获取数据
            // 获取ChooseAreaFragment 活动传递的数据：天气ID
            String weather_id = getIntent().getStringExtra("weather_id");
            Log.i("wwww", "weather id: "+weather_id);
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeatherData(weather_id);
        }

        // 加载图片
        String picPath = preferences.getString("bingPic", null);
        if (picPath != null){
            // 显示图片
            Glide.with(this).load(picPath).into(imageView);
        }else{
            loadBingPic();
        }
    }

    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        Log.i("wwww", "showWeatherInfo: "+cityName);
        titleCity.setText(cityName);
        updateTime.setText(weather.basic.update.updateTime.split(" ")[1]);
        nowTemp.setText(weather.now.temperature+"℃");
        weatherInfo.setText(weather.now.more.info);
        forcastLayout.removeAllViews();
        for (Forescast forescast: weather.forescastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_items,
                    forcastLayout,false);
            TextView dataText = (TextView) view.findViewById(R.id.data_text);
            TextView weatherInfo = (TextView) view.findViewById(R.id.info_text);
            TextView maxTemp = (TextView) view.findViewById(R.id.max_info);
            TextView minTemp = (TextView) view.findViewById(R.id.min_info);
            dataText.setText(forescast.date);
            weatherInfo.setText(forescast.more.info);
            maxTemp.setText(forescast.temperature.max);
            minTemp.setText(forescast.temperature.min);
            // 将view对象加入到LinearLayout中
            forcastLayout.addView(view);
        }
        if (weather.aqi != null) {
            Log.i("wwww", "showWeatherInfo: "+weather.aqi.city);
            AQText.setText(weather.aqi.city.aqi);
            PM25Text.setText(weather.aqi.city.pm25);
        }
        String comfortString = "舒适指数： "+ weather.suggestion.comfort.info;
        comfortText.setText(comfortString);
        String carWashString = "洗车指数： "+ weather.suggestion.carsh.info;
        carWashText.setText(carWashString);
        String sportString = "运动建议： " + weather.suggestion.sport.info;
        sportText.setText(sportString);
        // 显示数据
        weatherLayout.setVisibility(View.VISIBLE);
    }

    // 根据天气ID获取城市天气信息
    private void requestWeatherData(final String weather_id){
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weather_id+"&key=7c6047fdbf5a44dfa6b7b25e531474fb";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气失败",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String weatherText = response.body().string();
                Log.i("wwww", "onResponse: "+weatherText);
                final Weather weather = Utility.handleWeatherResponse(weatherText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 判断获取天气信息的状态: ok表示获取成功， 其他表示失败
                        if (weather != null && "ok".equals(weather.status)){
                            // 将天气数据存储在文本数据库中
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                                    WeatherActivity.this).edit();
                            editor.putString("weather", weatherText);
                            editor.apply();
                            // 显示天气信息
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气失败",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    // 加载必应每日一图
    private void loadBingPic(){
        HttpUtil.sendOkHttpRequest("http://guolin.tech/api/bing_pic", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this, "图片加载失败", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String uri = response.body().string();
                // 将图片URL存储到本地
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                        WeatherActivity.this).edit();
                editor.putString("bingPic", uri);
                editor.apply();
                runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      Glide.with(WeatherActivity.this).load(uri).into(imageView);
                                  }
                              }
                );
            }
        });
    }

}
