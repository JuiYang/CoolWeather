package com.example.tclxa.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public Carsh carsh;

    @SerializedName("sport")
    public Sport sport;

    public class Sport{

        @SerializedName("txt")
        public String info;
    }

    public class Carsh{

        @SerializedName("txt")
        public String info;
    }

    public class Comfort{

        @SerializedName("txt")
        public String info;
    }
}
