package com.example.tclxa.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forescast {

    @SerializedName("date")
    public String date;

    @SerializedName("cond")
    public More more;

    @SerializedName("tmp")
    public Temperature temperature;


    public class Temperature{
        @SerializedName("max")
        public String max;

        @SerializedName("min")
        public String min;
    }

    public class More{
        @SerializedName("txt_d")
        public String info;
    }

}
