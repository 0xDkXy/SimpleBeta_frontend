package com.example.simpleapp.model;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.simpleapp.AdminActivity;
import com.example.simpleapp.requests.HttpRequest;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataController {
    private static String teg = "Datacontroller";
    public static JSONObject getJson(Map<String,Object> params, String Url){
        HttpURLConnection connection = null;
        try {
            JSONObject jsonParam =new JSONObject(params);
            connection = HttpRequest.getRequest(Url,"POST");
            DataOutputStream dos=new DataOutputStream(connection.getOutputStream());
            dos.writeBytes(jsonParam.toString());
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String line;
                String res = "";
                while ((line = reader.readLine()) != null) {
                    res += line;
                }
                reader.close();
                JSONObject jo = new JSONObject(res);
                return jo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean updateUserInfo(Map<String,Object> params, String Url){
        HttpURLConnection connection = null;
        try {
            JSONObject jsonParam =new JSONObject(params);
            connection = HttpRequest.getRequest(Url,"POST");
            DataOutputStream dos=new DataOutputStream(connection.getOutputStream());
            dos.writeBytes(jsonParam.toString());
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.i(teg,"插入快递信息成功！");
                return true;
            }else{
                Log.e(teg,"插入快递信息失败！");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(teg,"插入快递信息失败！");
            return false;
        }
    }

    static public List<String> JSON_to_list(JSONObject res) {
        String tag = "json2list";
        List<JSONObject> jslist = new ArrayList<JSONObject>();
        int len=res.length();
        for(int i=0;i<len;++i){
            try{
                jslist.add(res.getJSONObject(String.valueOf(i)));
            }catch (Exception e){
                Log.d(tag,e.toString());
            }
        }
        List<String> listitem = new ArrayList<String>();

        for (JSONObject item:jslist){
//            Map<String, Object> showitem = new HashMap<String, Object>();
            try {
                String temp = "用户：" + item.getString("CName") +
                        "\n电话：" + item.getString("phone") +
                        "\n地址：" + item.getString("addres") +
                        "\n派送员：" + item.getString("UID");
//                        showitem.put("CName", item.getString("CName"));
//                showitem.put("phone", item.getString("phone"));
//                showitem.put("addres", item.getString("addres"));
//                showitem.put("UID", item.getString("UID"));
                Log.d(tag,temp);
                listitem.add(temp);
            }catch (Exception e){
                Log.d(tag,e.toString());
            }
        }
        return listitem;
    }
}
