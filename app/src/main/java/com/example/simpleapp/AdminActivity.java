package com.example.simpleapp;

import static android.app.PendingIntent.getActivity;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

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

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

//        EditText receiverNameIt = findViewById(R.id.receiverNameIt);
//        EditText receiverPhoneIt = findViewById(R.id.reeciverPhoneIt);
//        EditText receiverAddressIt = findViewById(R.id.receiverAddressIt);
//        EditText senderIdIt = findViewById(R.id.senderIdIt);

        Intent intent = getIntent();
        String token = intent.getStringExtra("extra_data");

//        Button confirmBt = (Button) findViewById(R.id.confirmBt);
        Button addBt=(Button) findViewById(R.id.addInfo);
        final JSONObject[] json = new JSONObject[1];
        List<JSONObject> jslist = new ArrayList<JSONObject>();
        Thread reqThread = new Thread(new Runnable() {
            @Override
            public void run() {
//                jsobj=getJson(UID,token);
                json[0] = getJson("admin", token);
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
                showitem.put("UID", item.getString("UID"));
                Log.d("test",showitem.toString());
                listitem.add(showitem);
            }catch (Exception e){
                Log.d("test",e.toString());
            }
        }
        SimpleAdapter userListAdapter=new SimpleAdapter(
                getApplicationContext(),
                listitem,
                R.layout.list_item_admin,
                new String[]{
                        "CName",
                        "phone",
                        "addres",
                        "UID"
                },
                new int[]{
                        R.id.CName,
                        R.id.phone,
                        R.id.addres,
                        R.id.UID
                });
        ListView listView = (ListView) findViewById(R.id.list_admin);
        listView.setAdapter(userListAdapter);
        String CName,UID,addres,phone;
        String temp[] = {"CName","phone","addres","UID"};
        final String info[] = new String[4];
        final QMUIDialog.EditTextDialogBuilder Builder[] = new QMUIDialog.EditTextDialogBuilder[4];
        for(int i=0;i<4;++i){
            Builder[i]=new QMUIDialog.EditTextDialogBuilder(AdminActivity.this);
        }

        for(int i=0;i<4;++i){
            final int ii=i;
            Builder[i].setTitle("添加信息")
                    .setPlaceholder(temp[i])
                    .setInputType(InputType.TYPE_CLASS_TEXT)
                    .addAction("取消", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .addAction("确定", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            CharSequence text = Builder[ii].getEditText().getText();
                            if (text != null && text.length() > 0) {
//                                Toast.makeText(AdminActivity.this, "您的昵称: " + text, Toast.LENGTH_SHORT).show();
                                info[ii]=text.toString();
//                                Toast.makeText(AdminActivity.this, info[ii], Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                if(ii !=3)
                                    Builder[ii+1].show();
                                else {
                                    Thread threadt=new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addExpress(info[0],info[1],info[2],info[3],token);
                                        }
                                    });

                                    threadt.start();
                                    QMUITipDialog tipDialog= new QMUITipDialog.Builder(AdminActivity.this)
                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                                            .setTipWord("正在加载")
                                            .create();
                                    try {
                                        threadt.join(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
//                                    while(threadt.isAlive()){;}
                                    tipDialog.dismiss();
                                }
                            } else {
                                Toast.makeText(AdminActivity.this, "请填入", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        addBt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
//                listDialog.show();
                Builder[0].show();
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