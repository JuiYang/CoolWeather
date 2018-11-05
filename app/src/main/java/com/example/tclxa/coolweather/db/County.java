package com.example.tclxa.coolweather.db;

import org.litepal.crud.DataSupport;


// http://guolin.tech/api/china/provinceId/cityId
public class County extends DataSupport {

    private int id;
    private String countyName;
    private int cityId;
    private String weatherId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherI) {
        this.weatherId = weatherI;
    }
}
