package com.example.simpleapp.requests;

import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {

    static public HttpURLConnection getRequest(String Url,String RequestMethod) {
        HttpURLConnection Conn = null;
        try{
            URL url = new URL(Url);
            Conn = (HttpURLConnection) url.openConnection();
            Conn.setConnectTimeout(3000);
            Conn.setReadTimeout(3000);
            //设置请求方式 GET / POST 一定要大小
            Conn.setRequestMethod(RequestMethod);
            Conn.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            Conn.setRequestProperty("accept", "application/json"); // 设置发送数据的格式
            Conn.setDoInput(true);
            Conn.setDoOutput(false);
            Conn.connect();
        }catch (Exception e){
            Log.e("HttpConn",e.getMessage());
        }
        return Conn;
    }
}
