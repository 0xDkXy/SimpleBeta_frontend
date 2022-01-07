package com.example.simpleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(MainActivity.this, "欢迎进入易物流!",
                Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_main);

        EditText userNameIt = findViewById(R.id.usernameIt);
        EditText passwordIt = findViewById(R.id.passwordIt);
        Button loginBt = (Button) findViewById(R.id.loginBt);
        loginBt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                if (userNameIt.getText().toString().equals("") || passwordIt.getText().toString().equals(""))
                    Toast.makeText(MainActivity.this, "用户名及密码不能为空!",
                            Toast.LENGTH_SHORT).show();
                else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            loginRequest(userNameIt.getText().toString(), passwordIt.getText().toString());
                        }
                    }).start();
                }
            }
        });

        Button signUpBt = (Button) findViewById(R.id.signupBt);
        signUpBt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
            EditText userNameIt = findViewById(R.id.usernameIt);
            EditText passwordIt = findViewById(R.id.passwordIt);
            if (userNameIt.getText().toString().equals("") || passwordIt.getText().toString().equals(""))
                Toast.makeText(MainActivity.this, "用户名及密码不能为空!",
                        Toast.LENGTH_SHORT).show();
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        EditText usernameIt = findViewById(R.id.usernameIt);
                        EditText passwordIt = findViewById(R.id.passwordIt);
                        signUp(usernameIt.getText().toString(), passwordIt.getText().toString());
                    }
                }).start();
            }
        }
    });
    }

    public void loginRequest(String user, String password){
        HttpURLConnection connection = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", user);
            params.put("passwd", password);
            JSONObject jsonParam =new JSONObject(params);
            URL url = new URL("http://0xdkxy.top:10000/login/signIn");
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
                // 读取响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String line;
                String res = "";
                while ((line = reader.readLine()) != null) {
                    res += line;
                }
                reader.close();
                JSONObject jo = new JSONObject(res);
                if (user.equals("admin")){
                    Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                    intent.putExtra("extra_data", jo.getString("token"));
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(MainActivity.this, UserActivity.class);
                    intent.putExtra("extra_data", jo.getString("token"));
                    intent.putExtra("UID", user);
                    startActivity(intent);
                }
            } else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED){
                Looper.prepare();
                Toast.makeText(MainActivity.this, "账号或密码错误，请重新输入!",
                        Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void signUp(String user, String password) {
        HttpURLConnection connection = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", user);
            params.put("passwd", password);
            JSONObject jsonParam = new JSONObject(params);
            URL url = new URL("http://0xdkxy.top:10000/login/signIn");
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
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            dos.writeBytes(jsonParam.toString());
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Looper.prepare();
                Toast.makeText(MainActivity.this, "注册成功!",
                        Toast.LENGTH_SHORT).show();
                Looper.loop();
            } else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Looper.prepare();
                Toast.makeText(MainActivity.this, "账号已存在，请重新输入!",
                        Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}