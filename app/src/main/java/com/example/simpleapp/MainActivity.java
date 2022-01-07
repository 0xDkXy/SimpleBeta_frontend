package com.example.simpleapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.layout.QMUILayoutHelper;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUIFloatLayout;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

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
        Toast.makeText(MainActivity.this, "Welcomt to SimpleBeta",
                Toast.LENGTH_SHORT).show();
        setContentView(R.layout.activity_main);

        initTopBar();
        initLogo();
        initLoginEditText();

    }
    private void initLogin(String UID,String passwd){
        /**
         *
         */
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

    private void initTopBar(){
        /**
         * 初始化topbar
         */
        // 设置topbar title
        QMUITopBarLayout loginTopbar=findViewById(R.id.loginTopbar);
        loginTopbar.setTitle("LOGIN");

        // 沉浸式状态栏
        QMUIStatusBarHelper.translucent(this);
    }

    private void initLogo(){
        /**
         *
         */
        // 设置 QMUILinearLayout 阴影颜色和圆角
        QMUILinearLayout qmuiLayout = findViewById(R.id.qmuiLayout);
//        qmuiLayout.setShadowColor(0xff0000ff); // 蓝色阴影
        int qradius=QMUILayoutHelper.RADIUS_OF_HALF_VIEW_WIDTH;
        qmuiLayout.setRadius(qradius);
        qmuiLayout.setRadiusAndShadow(qradius,
                QMUIDisplayHelper.dp2px(MainActivity.this,11),
                0.6f);
    }

    private void initLoginEditText(){
        /**
         *
         */
        // 设置登陆文本输入框阴影
        QMUILinearLayout loginQMUILL = findViewById(R.id.loginQMUILL);
        int LoginRadius=15;
//        loginQMUILL.setRadius(LoginRadius);
        loginQMUILL.setRadiusAndShadow(LoginRadius,
                QMUIDisplayHelper.dp2px(MainActivity.this,11),
                0.6f);
    }

    private HttpURLConnection initConn(String UrlApi){
        /**
         *
         */
        HttpURLConnection conn = null;
        try{
            URL url = new URL(UrlApi);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            //设置请求方式 GET / POST 一定要大小
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
            conn.setRequestProperty("accept", "application/json"); // 设置发送数据的格式
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.connect();
        }catch (Exception e){
            Log.e("HttpConn",e.getMessage());
        }
        return conn;
    }

    private void loginRequest(String user, String password){
        HttpURLConnection connection = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", user);
            params.put("passwd", password);
            JSONObject jsonParam =new JSONObject(params);
            connection = initConn("http://0xdkxy.top:10000/login/signIn");
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



    private void signUp(String user, String password) {
        HttpURLConnection connection = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", user);
            params.put("passwd", password);
            JSONObject jsonParam = new JSONObject(params);
            connection = initConn("http://0xdkxy.top:10000/login/signUp");
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