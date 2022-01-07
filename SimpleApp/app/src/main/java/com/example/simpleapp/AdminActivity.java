package com.example.simpleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        EditText receiverNameIt = findViewById(R.id.receiverNameIt);
        EditText receiverPhoneIt = findViewById(R.id.reeciverPhoneIt);
        EditText receiverAddressIt = findViewById(R.id.receiverAddressIt);
        EditText senderIdIt = findViewById(R.id.senderIdIt);

        Intent intent = getIntent();
        String token = intent.getStringExtra("extra_data");

        Button confirmBt = (Button) findViewById(R.id.confirmBt);
        confirmBt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (receiverNameIt.getText().toString().equals("") || receiverPhoneIt.getText().toString().equals("") || receiverAddressIt.getText().toString().equals("") || senderIdIt.getText().toString().equals(""))
                    Toast.makeText(AdminActivity.this, "用户名及密码不能为空!",
                            Toast.LENGTH_SHORT).show();
                else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            addExpress(receiverNameIt.getText().toString(), receiverPhoneIt.getText().toString(), receiverAddressIt.getText().toString(), senderIdIt.getText().toString(), token);
                        }
                    }).start();
                }
            }
        });

        Button cancelBt = (Button) findViewById(R.id.cancelBt);
        cancelBt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                receiverNameIt.setText("");
                receiverPhoneIt.setText("");
                receiverAddressIt.setText("");
                senderIdIt.setText("");
            }
        });
    }

    public void addExpress(String receiverName, String receiverPhone, String receiverAddress, String senderId, String token){
        HttpURLConnection connection = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("CName", receiverName);
            params.put("addres", receiverAddress);
            params.put("phone", receiverPhone);
            params.put("UID", senderId);
            params.put("token", token);
            JSONObject jsonParam =new JSONObject(params);
            URL url = new URL("http://0xdkxy.top:10000/user/addInfo");
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
                Looper.prepare();
                Toast.makeText(AdminActivity.this, "插入快递信息成功！",
                        Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}