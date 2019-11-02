package com.example.coolweather.db;

import org.litepal.crud.DataSupport;

public class County extends DataSupport {
    //县
    private int id;
    //县名字
    private String countyName;

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

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    //当前县所对应的市id
    private int cityId;
    //对应的天气id
    private String weatherId;


}
