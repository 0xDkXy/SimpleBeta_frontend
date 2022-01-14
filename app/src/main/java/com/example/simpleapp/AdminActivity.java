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
    private boolean isSuc = false;
    private String mAddUserInfo;


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


        initTopBar();
        initData();
        initAddItemBtn();

    }



    private void initAddItemBtn() {
        int qradius= QMUILayoutHelper.RADIUS_OF_HALF_VIEW_WIDTH;
        mAddItemBtnLyt.setRadiusAndShadow(qradius,
                QMUIDisplayHelper.dp2px(AdminActivity.this,10),
                0.8f);


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
                                    mAddUserInfo="用户：" + info[0] +
                                            "\n电话：" + info[1] +
                                            "\n地址：" + info[2] +
                                            "\n派送员：" + info[3];
                                    Thread threadt=new Thread(new Runnable() {
                                        @Override
                                        public void run() {
//                                            addExpress(info[0],info[1],info[2],info[3],token);
                                            isSuc=updateUserInfo(info[0],info[1],info[2],info[3],token);
                                            try {
                                                Thread.sleep(1400);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    threadt.start();
                                    QMUITipDialog tipDialog= new QMUITipDialog.Builder(AdminActivity.this)
                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                                            .setTipWord("正在添加")
                                            .create();
                                    tipDialog.show();
                                    try {
                                        threadt.join();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    tipDialog.dismiss();
//                                    mRecyclerView.postDelayed(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            tipDialog.dismiss();
//                                        }
//                                    },1500);
                                    if(isSuc){
                                        Toast.makeText(AdminActivity.this, "插入快递信息成功！",
                                            Toast.LENGTH_SHORT).show();
                                        onRefreshData();
                                    }else{
                                        Toast.makeText(AdminActivity.this, "插入快递信息失败！",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(AdminActivity.this, "请填入", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        mAddItemBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
//                listDialog.show();
                Builder[0].show();
            }
        });
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

    private void onRefreshData() {
        List<String> data = new ArrayList<>();
        data.add(mAddUserInfo);
        mAdapter.append(data);
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
    private boolean updateUserInfo(String receiverName, String receiverPhone, String receiverAddress, String senderId, String token) {
        String tag="updateUserInfo";
        Map<String, Object> params = new HashMap<>();
        params.put("CName", receiverName);
        params.put("addres", receiverAddress);
        params.put("phone", receiverPhone);
        params.put("UID", senderId);
        params.put("token", token);
        if (DataController.updateUserInfo(params,"http://0xdkxy.top:10000/user/addInfo")) {
            Log.i(tag,"插入快递信息成功！");
//            Looper.prepare();
//            Toast.makeText(AdminActivity.this, "插入快递信息成功！",
//                    Toast.LENGTH_SHORT).show();
//            Looper.loop();
            return true;
        } else {
            Log.e(tag,"插入快递信息失败！");
//            Looper.prepare();
//            Toast.makeText(AdminActivity.this, "插入快递信息失败！",
//                    Toast.LENGTH_SHORT).show();
//            Looper.loop();
            return false;
        }
    }


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