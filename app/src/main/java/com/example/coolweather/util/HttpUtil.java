package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

//与服务器交互的类
public class HttpUtil {
    //发起Http请求 只需调用该方法，传入请求地址，并注册一个回调处理服务器响应即可
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
