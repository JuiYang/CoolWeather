package com.example.tclxa.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {

    // @SerializedName()注解关联json字段和java对象字段

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update{

        @SerializedName("loc")
        public String updateTime;
    }
}
