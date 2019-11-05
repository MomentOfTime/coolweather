package com.example.coolweather.Activitys;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.R;
import com.example.coolweather.db.County;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;

    private Button navButton;

    public SwipeRefreshLayout swipeRefresh;

    private ImageView bingPicImg;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    /**
     * 先获取一些控件的实例，然后尝试从本地缓存中读取天气数据。
     * 那么第一次肯定是没有缓存的，因此就会从Intent中取出天气id
     * 调用requstWeather（）从服务器请求天气数据。
     * ：请求数据时 先将ScrollView进行隐藏，不然空数据的界面看上去很奇怪
     *
     * requestWeather（）方法中先使用了参数中传入的天气id 和 之前申请好的API  key拼装
     * 出一个接口地址，接着调用HttpUtil.sendOkHttpRequest()方法向该地址发出请求
     * 服务器会将相应的城市天气信息 以JSON格式返回。
     * 然后 我们在onResponse（）回调中先调用handleWeatherResponse（）方法将返回的JSON数据转换
     * 成Weather对象，再将当前主线程切换到主线程 。
     *
     * 进行判断，如果服务器返回status 状态是ok，说明请求天气成功。
     * 此时将返回的数据缓存到SharedPreferences当中，并调用showWeatherInfo（）方法来显示内容
     *
     * showWeatherInfo()方法
     * 从Weather对象中获取数据，显示到相应的控件上去。
     *
     * 未来几天天气预报部分 使用了一个for循环的天气信息，在循环中动态加载forecast_item.xml布局并
     * 设置相应数据，然后添加到父布局中。
     *
     * 最后将ScrollView变成可见。
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * 由于背景图和状态栏融合在一起的效果
         * 只有Android 5.0 极其以上的系统才支持，因此我们现在代码中
         * 做了一个系统版本号的控制。只有大于等于21
         *
         * 调用getWindow（）.getDecorView()拿到当前活动的
         * DecorView 再调用它的setSystemUiVisibility()改变系统UI显示
         * 传入的ViewSYSTEM_UI_FLAG_FULLSCREEN和
         * View.SYSTEM_UI_FLAG_LAYOUT_STABLE
         * 表示活动的布局显示在状态栏上面，最后调用一些setStatusBarColor()
         * 将状态栏颜色设置成透明色。
         *
         * 在onCreate方法中 获得DrawerLayout 和 Button 实例
         * 在button的点击事件中调用DrawerLayout的openDrawer()方法打开
         * 滑动菜单
         *
         * 打开菜单之后，需要处理切换城市的逻辑
         */
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        initView();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        final String weatherId;

        String weatherString = prefs.getString("weather",null);

        //okHttp 返回的JSON 保存在SharedPreference，从中取出
        if (weatherString != null) {

            //有数据 直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存时 去服务器查询
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.VISIBLE);
            requestWeather(weatherId);
        }

        // 滑动刷新
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        // 加载必应一图
        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

        // 侧滑按钮
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void initView () {
        drawerLayout = findViewById(R.id.drawer_layout);

        navButton = findViewById(R.id.nav_button);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(Color.GREEN);

        bingPicImg = findViewById(R.id.bing_pic_img);

        weatherLayout = findViewById(R.id.weather_layout);

        titleCity = findViewById(R.id.title_city);

        titleUpdateTime = findViewById(R.id.title_update_time);

        degreeText = findViewById(R.id.degree_text);

        weatherInfoText = findViewById(R.id.weather_info_text);

        forecastLayout = findViewById(R.id.forecast_layout);

        aqiText = findViewById(R.id.aqi_text);

        pm25Text = findViewById(R.id.pm25_text);

        comfortText = findViewById(R.id.comfort_text);

        carWashText = findViewById(R.id.car_wash_text);

        sportText = findViewById(R.id.sport_text);




//        String weatherId = getIntent().getStringExtra("weather_id");
//        weatherLayout.setVisibility(View.VISIBLE);
//        requestWeather(weatherId);
    }


    /**
     * 根据天气id 请求城市天气信息
     * 并且showWeatherInfo
     * @param weatherId
     */
    public void requestWeather (final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +weatherId +
               "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this
                        ,"获取天气信息失败"
                        ,Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败"
                            ,Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });

        loadBingPic();
    }

    /**
     * 加载必应每日一图
     */

    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 处理展示Weather 实体类中数据
     * @param weather
     */
    private void showWeatherInfo (Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split("")[1];
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item
                    ,forecastLayout ,false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);


            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }

        if (weather.aqi != null) {

            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度： "+weather.suggestion.comfort.info;
        String carWash = "洗车指数："+weather.suggestion.carWash.info;
        String sport = "运动建议："+weather.suggestion.sport.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

}
