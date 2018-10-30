package com.example.tclxa.coolweather.utils;

import android.text.TextUtils;

import com.example.tclxa.coolweather.db.City;
import com.example.tclxa.coolweather.db.County;
import com.example.tclxa.coolweather.db.Province;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// 解析api返回的数据
public class Utility {

    // 解析服务器返回的Province数据
    public static boolean handleProvinceResponse(String response){
        // 判断服务器是否返回数据
        if (!TextUtils.isEmpty(response)){
            try{
                // 将数据解析为json对象
                JSONArray provinces = new JSONArray(response);
                for (int i =0 ;i < provinces.length(); i++) {
                    JSONObject provinceObject = provinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));
                    province.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的城市数据
     * */
    public static boolean handleCityResponse(String response, int provinceId){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray cites = new JSONArray(response);
                for (int i=0; i<cites.length(); i++){
                    JSONObject cityObject = cites.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(cityObject.getInt("id"));
                    city.setCityName(cityObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的县区数据
     * */
    public static boolean handleCountyResponse(String response, int cityId){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray counties = new JSONArray(response);
                for (int i=0; i<counties.length(); i++){
                    JSONObject countyObject = counties.getJSONObject(i);
                    County county = new County();
                    county.setCityId(cityId);
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getInt("weather_id"));
                    county.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
}
