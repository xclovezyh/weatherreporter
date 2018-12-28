package com.wb.weatherreporter.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Http工具类
 *用于发起一条Http请求，传入地址并注册一个回调来处理服务器的响应
 */
public class HttpUtil {
    public static void sendOKHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
