package com.example.tclxa.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.tclxa.coolweather.gson.Weather;
import com.example.tclxa.coolweather.utils.HttpUtil;
import com.example.tclxa.coolweather.utils.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateWeather extends Service {
    public AutoUpdateWeather() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long hours = 8*60*60*1000;
        long triggerTime = SystemClock.currentThreadTimeMillis()+hours;
        Intent intentAlarm = new Intent(this,AutoUpdateWeather.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intentAlarm, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
        manager.cancel(pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    // 获取天气数据并将数据保存在本地文件数据库
    private void updateWeather(){
        // 存储存储在本地的天气ID
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sharedPreferences.getString("weather", null);
        if (weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String uri = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=7c6047fdbf5a44dfa6b7b25e531474fb";
            HttpUtil.sendOkHttpRequest(uri, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather1 = Utility.handleWeatherResponse(responseText);
                    if (weather1 != null && "ok".equals(weather1.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateWeather.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }

    }

    // 获取背景图片数据保存在本地文件数据库
    private void updateBingPic(){
        HttpUtil.sendOkHttpRequest("http://guolin.tech/api/bing_pic", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateWeather.this).edit();
                editor.putString("bingPic", responseText);
                editor.apply();
            }
        });
    }
}
