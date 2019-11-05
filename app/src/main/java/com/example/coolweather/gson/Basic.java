package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {

//在JSON中 一些字段不适合直接作为JAVA字段命名，使用@SerializedName建立映射关系
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
