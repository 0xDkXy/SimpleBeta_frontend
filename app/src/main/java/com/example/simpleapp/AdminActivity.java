package com.example.simpleapp;

import static android.app.PendingIntent.getActivity;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.os.Looper;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.simpleapp.model.DataController;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.qmuiteam.qmui.QMUIInterpolatorStaticHolder;
import com.qmuiteam.qmui.layout.QMUILayoutHelper;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.recyclerView.QMUIRVItemSwipeAction;
import com.qmuiteam.qmui.recyclerView.QMUISwipeAction;
import com.qmuiteam.qmui.recyclerView.QMUISwipeViewHolder;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.pullLayout.QMUIPullLayout;

import com.example.simpleapp.model.DataController;
import com.example.simpleapp.adaptor.QDRecyclerViewAdapter;
import com.example.simpleapp.requests.HttpRequest;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

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

    QMUITopBarLayout mTopBar;
    QMUIPullLayout mPullLayout;
    RecyclerView mRecyclerView;
    QMUILinearLayout mAddItemBtnLyt;
    QMUIRoundButton mAddItemBtn;

    private QDRecyclerViewAdapter mAdapter;
    private JSONObject allUserInfo;
    private String mTopBarTitle = "所有用户";
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pull_horizontal_layout);
        Intent intent = getIntent();
        token = intent.getStringExtra("extra_data");

        mTopBar=findViewById(R.id.topbar);
        mPullLayout=findViewById(R.id.pull_layout);
        mRecyclerView=findViewById(R.id.recyclerView);
        mAddItemBtn = findViewById(R.id.addItemBtn);
        mAddItemBtnLyt = findViewById(R.id.addItemBtnLyt);

        initAddItemBtn();
        initTopBar();
        initData();

    }

    private void initAddItemBtn() {
        int qradius= QMUILayoutHelper.RADIUS_OF_HALF_VIEW_WIDTH;
        mAddItemBtnLyt.setRadiusAndShadow(qradius,
                QMUIDisplayHelper.dp2px(AdminActivity.this,10),
                0.8f);
    }

    class Adapter extends QDRecyclerViewAdapter {

        public Adapter(JSONObject res) {
            super();
            super.mItems = DataController.JSON_to_list(res);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View root = inflater.inflate(R.layout.recycler_view_item, viewGroup, false);
            final ViewHolder vh = new ViewHolder(root,this);
            root.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Toast.makeText(AdminActivity.this,
                            "click position=" + vh.getAdapterPosition(),
                            Toast.LENGTH_SHORT).show();
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.setText(super.mItems.get(i));
        }

    }
    /**
     * 初始化状态栏,设置沉浸式状态栏
     */
    private void initTopBar() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { AdminActivity.this.finish();}
        });
        mTopBar.setTitle(mTopBarTitle);
        QMUIStatusBarHelper.translucent(this);
    }

    /**
     * 初始化listView以及数据
     */
    private void initData() {

        Thread mGetInfo = new Thread(new Runnable() {
            @Override
            public void run() {
                allUserInfo = getUserInfo("admin",token);
            }
        });
        mGetInfo.start();
        try {
            mGetInfo.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        mPullLayout.setActionListener(new QMUIPullLayout.ActionListener() {
            @Override
            public void onActionTriggered(@NonNull QMUIPullLayout.PullAction pullAction) {
                mPullLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullLayout.finishActionRun(pullAction);
                    }
                },1000);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        new PagerSnapHelper().attachToRecyclerView(mRecyclerView);

//        mAdapter = new QDRecyclerViewAdapter();
//        mAdapter.setItemCount(10);
//        mRecyclerView.setAdapter(mAdapter);


        mAdapter = new Adapter(allUserInfo);
        mRecyclerView.setAdapter(mAdapter);

        QMUIRVItemSwipeAction swipeAction = new QMUIRVItemSwipeAction(true, new QMUIRVItemSwipeAction.Callback() {
            @Override
            public void onClickAction(QMUIRVItemSwipeAction swipeAction, RecyclerView.ViewHolder selected, QMUISwipeAction action) {
                super.onClickAction(swipeAction, selected, action);
                Looper.prepare();
                Toast.makeText(AdminActivity.this,
                        "你点击了第 " + selected.getAdapterPosition() + " 个 item 的" + action.getText(),
                        Toast.LENGTH_SHORT).show();
                Looper.loop();
//                if(action == mAdapter.mDeleteAction){
//                    mAdapter.remove(selected.getAdapterPosition());
//                }else{
//                    swipeAction.clear();
//                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,int direction) {
                mAdapter.removeItem(viewHolder.getAdapterPosition());
            }

            /**
             * 设置向上滑动删除
             * @param recyclerView
             * @param viewHolder
             * @return
             */
            @Override
            public int getSwipeDirection(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return QMUIRVItemSwipeAction.SWIPE_UP;
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.3f;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder selected) {
                super.onSelectedChanged(selected);
                if (selected != null) {
                    mTopBar.setTitle("上滑删除");
                    selected.itemView.animate()
                            .scaleX(1.02f)
                            .scaleY(1.02f)
                            .setInterpolator(QMUIInterpolatorStaticHolder.ACCELERATE_INTERPOLATOR)
                            .setDuration(250)
                            .start();

                    // 震动
                    Vibrator vibrator = (Vibrator) AdminActivity.this.getSystemService(Service.VIBRATOR_SERVICE);
                    vibrator.vibrate(10);
                } else {
                    mTopBar.setTitle(mTopBarTitle);
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                View itemView = viewHolder.itemView;
                if (itemView.getScaleX() != 1f || itemView.getScaleY() != 1f) {
                    itemView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setInterpolator(QMUIInterpolatorStaticHolder.DECELERATE_INTERPOLATOR)
                            .setDuration(250)
                            .start();
                } else {
                    itemView.animate().cancel();
                }
            }
        });
        swipeAction.setPressTimeToSwipe(300);
        swipeAction.attachToRecyclerView(mRecyclerView);
    }

    /**
     * 新增用户派件信息
     * @param receiverName 插入用户姓名
     * @param receiverPhone 插入用户电话
     * @param receiverAddress 插入用户地址
     * @param senderId 派送员id
     * @param token 管理员令牌
     */
    private void updateUserInfo(String receiverName, String receiverPhone, String receiverAddress, String senderId, String token) {
        Map<String, Object> params = new HashMap<>();
        params.put("CName", receiverName);
        params.put("addres", receiverAddress);
        params.put("phone", receiverPhone);
        params.put("UID", senderId);
        params.put("token", token);
        if (DataController.updateUserInfo(params,"http://0xdkxy.top:10000/user/addInfo")) {
            Looper.prepare();
            Toast.makeText(AdminActivity.this, "插入快递信息成功！",
                    Toast.LENGTH_SHORT).show();
            Looper.loop();
        } else {
            Looper.prepare();
            Toast.makeText(AdminActivity.this, "插入快递信息失败！",
                    Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

//    public void addExpress(String receiverName, String receiverPhone, String receiverAddress, String senderId, String token){
//        HttpURLConnection connection = null;
//        try {
//            Map<String, Object> params = new HashMap<>();
//            params.put("CName", receiverName);
//            params.put("addres", receiverAddress);
//            params.put("phone", receiverPhone);
//            params.put("UID", senderId);
//            params.put("token", token);
//            JSONObject jsonParam =new JSONObject(params);
//            URL url = new URL("http://0xdkxy.top:10000/user/addInfo");
//            connection = (HttpURLConnection) url.openConnection();
//            connection.setConnectTimeout(3000);
//            connection.setReadTimeout(3000);
//            //设置请求方式 GET / POST 一定要大小
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
//            connection.setRequestProperty("accept", "application/json"); // 设置发送数据的格式
//            connection.setDoInput(true);
//            connection.setDoOutput(false);
//            connection.connect();
//            DataOutputStream dos=new DataOutputStream(connection.getOutputStream());
//            dos.writeBytes(jsonParam.toString());
//            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                Looper.prepare();
//                Toast.makeText(AdminActivity.this, "插入快递信息成功！",
//                        Toast.LENGTH_SHORT).show();
//                Looper.loop();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 获取全部用户的数据
     * @param UID 管理员账户
     * @param token 管理员对应令牌
     * @return 全部用户数据
     */
    private JSONObject getUserInfo(String UID, String token){
        Map<String, Object> params = new HashMap<>();
        params.put("UID", UID);
        params.put("token", token);
        return DataController.getJson(params,"http://0xdkxy.top:10000/user/getCostInfo");
    }

}