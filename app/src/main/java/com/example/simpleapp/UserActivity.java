package com.example.simpleapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.qmuiteam.qmui.layout.QMUILayoutHelper;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

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

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class UserActivity extends AppCompatActivity {

    private static final String sb="simplebeta";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Button bt = findViewById(R.id.QRcode);
        Intent intent = getIntent();
        String token = intent.getStringExtra("extra_data");
        String UID = intent.getStringExtra("UID");

        /**
         * 设置topbar标题 和返回button
         */
        QMUITopBarLayout userTopBar = findViewById(R.id.userTopBar);
        userTopBar.setTitle("用户列表");
        // back button
        userTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                UserActivity.this.finish();
            }
        });

        /**
         * 沉浸式状态栏
         */
        QMUIStatusBarHelper.translucent(this);

        /**+
         * 扫码button阴影
         */
        QMUILinearLayout QRBtnLayout=findViewById(R.id.QRBtnLayout);
        int qradius= QMUILayoutHelper.RADIUS_OF_HALF_VIEW_WIDTH;
        QRBtnLayout.setRadiusAndShadow(qradius,
                QMUIDisplayHelper.dp2px(UserActivity.this,10),
                0.8f);

        // **************listView********************
        final JSONObject[] json = new JSONObject[1];
        List<JSONObject> jslist = new ArrayList<JSONObject>();
        Thread reqThread = new Thread(new Runnable() {
            @Override
            public void run() {
//                jsobj=getJson(UID,token);
                json[0] = getJson(UID, token);
            }
        });
        reqThread.start();
        while(reqThread.isAlive()) { ; }
        int len=json[0].length();
        for(int i=0;i<len;++i){
            try{
                jslist.add(json[0].getJSONObject(String.valueOf(i)));
            }catch (Exception e){
                Log.d("Error",e.toString());
            }
        }
        List<Map<String, Object>> listitem = new ArrayList<Map<String, Object>>();

        for (JSONObject item:jslist){
            Map<String, Object> showitem = new HashMap<String, Object>();
            try {
                showitem.put("CName", item.getString("CName"));
                showitem.put("phone", item.getString("phone"));
                showitem.put("addres", item.getString("addres"));
                Log.d("test",showitem.toString());
                listitem.add(showitem);
            }catch (Exception e){
                Log.d(sb,e.toString());
            }
        }
        SimpleAdapter userListAdapter=new SimpleAdapter(
                getApplicationContext(),
                listitem,
                R.layout.list_item,
                new String[]{
                        "CName",
                        "phone",
                        "addres"
                },
                new int[]{
                        R.id.CName,
                        R.id.phone,
                        R.id.addres
                });
        ListView listView = (ListView) findViewById(R.id.list_test);
        listView.setAdapter(userListAdapter);
        // ****************listView**************************

        // **************拉起相机扫码******************
        bt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(UserActivity.this,
                        CaptureActivity.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });
        // **************拉起相机扫码******************

    }



    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            try {
//                TextView nameView = findViewById(R.id.nameCont);
//                TextView phoneView = findViewById(R.id.phoneCont);
//                TextView addressView = findViewById(R.id.addressCont);

                String str = new String(Base64.decode(result.getContents().getBytes(), Base64.DEFAULT));
                JSONObject jo = new JSONObject(str);
                String Cname=(jo.getString("CName"));
                String phone=(jo.getString("phone"));
                String addres=(jo.getString("addres"));
                final String item[]=new String[]{Cname,phone,addres};
                // *******************扫码信息展示 Dialog***********************
                new QMUIDialog.MenuDialogBuilder(UserActivity.this)
                        .addItems(item, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                Toast.makeText(getActivity(), "你选择了 " + items[which], Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .show();
                // ******************扫码信息展示 Dialog****************
            } catch (Exception e){ }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // ******************** 获取json **********************
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