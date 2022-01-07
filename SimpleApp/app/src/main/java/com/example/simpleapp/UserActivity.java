package com.example.simpleapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class UserActivity extends AppCompatActivity {

    int index = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Button bt = findViewById(R.id.scanBt);

        Intent intent = getIntent();
        String token = intent.getStringExtra("extra_data");
        String UID = intent.getStringExtra("UID");
        final JSONObject[] json = new JSONObject[1];

        new Thread(new Runnable() {
            @Override
            public void run() {
                json[0] = getJson(UID, token);
            }
        }).start();

        bt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(UserActivity.this,
                        CaptureActivity.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });

        Button prevBt = findViewById(R.id.prevBt);
        Button nextBt = findViewById(R.id.nextBt);

        prevBt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                index -= 1;
                showOneExpress(index, json[0]);
            }
        });

        nextBt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                index += 1;
                showOneExpress(index, json[0]);
            }
        });

    }

    protected void showOneExpress(int i, JSONObject json){
        i = i % json.length();
        TextView nameView = findViewById(R.id.nameCont);
        TextView phoneView = findViewById(R.id.phoneCont);
        TextView addressView = findViewById(R.id.addressCont);
        try {
            JSONObject jo = json.getJSONObject(Integer.toString(i));
            nameView.setText(jo.getString("CName"));
            phoneView.setText(jo.getString("phone"));
            addressView.setText(jo.getString("addres"));
        }catch (Exception e) {
            Looper.prepare();
            Toast.makeText(UserActivity.this, "暂无快递信息!",
                    Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            try {
                TextView nameView = findViewById(R.id.nameCont);
                TextView phoneView = findViewById(R.id.phoneCont);
                TextView addressView = findViewById(R.id.addressCont);

                String str = new String(Base64.decode(result.getContents().getBytes(), Base64.DEFAULT));
                JSONObject jo = new JSONObject(str);
                nameView.setText(jo.getString("CName"));
                phoneView.setText(jo.getString("phone"));
                addressView.setText(jo.getString("addres"));
            } catch (Exception e){ }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected JSONObject getJson(String UID, String token){
        HttpURLConnection connection = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("UID", UID);
            params.put("token", token);
            JSONObject jsonParam =new JSONObject(params);
            URL url = new URL("http://0xdkxy.top:10000/user/getCostInfo");
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            //设置请求方式 GET / POST 一定要大小
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            connection.setRequestProperty("accept", "application/json"); // 设置发送数据的格式
            connection.setDoInput(true);
            connection.setDoOutput(false);
            connection.connect();
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
}