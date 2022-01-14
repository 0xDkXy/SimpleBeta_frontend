package com.example.simpleapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.simpleapp.adaptor.QDRecyclerViewAdapter;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.qmuiteam.qmui.layout.QMUIFrameLayout;
import com.qmuiteam.qmui.layout.QMUILayoutHelper;
import com.qmuiteam.qmui.layout.QMUILinearLayout;
import com.qmuiteam.qmui.recyclerView.QMUIRVItemSwipeAction;
import com.qmuiteam.qmui.skin.QMUISkinHelper;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.skin.QMUISkinValueBuilder;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIResHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

import com.example.simpleapp.model.DataController;
import com.qmuiteam.qmui.widget.popup.QMUIFullScreenPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.qmuiteam.qmui.widget.pullLayout.QMUIPullLayout;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

public class UserActivity extends AppCompatActivity {

    private QMUIRoundButton QRbtn;
    private QMUILinearLayout QRbtnLyt;
    private String token,UID;
    private QMUITopBarLayout mTopBar;
    private JSONObject UserInfo;
    private QMUIPullLayout mPullLayout;
    private QDRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private List<String> UserInfoList;
    private String mTopBarTitle = "派送列表";


    private static final String sb="simplebeta";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pull_vertical_layout);
        Intent intent = getIntent();

        mRecyclerView = findViewById(R.id.VrecyclerView);
        mPullLayout = findViewById(R.id.V_pull_layout);
        mTopBar = findViewById(R.id.Vtopbar);
        QRbtnLyt = findViewById(R.id.QRcodeBtnLyt);
        QRbtn = findViewById(R.id.QRcodeBtn);
        token = intent.getStringExtra("extra_data");
        UID = intent.getStringExtra("UID");


        initTopbar();
        initQRcodeBtn();
        initData();

    }

    class Adapter extends QDRecyclerViewAdapter {

        public Adapter(JSONObject res) {
            super();
            super.mItems = DataController.JSON_to_list(res);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.setText(super.mItems.get(i));
        }


        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View root = inflater.inflate(R.layout.recycler_view_item_0, viewGroup, false);
            final ViewHolder vh = new ViewHolder(root, this);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int itemPos = vh.getAdapterPosition();
                    Toast.makeText(UserActivity.this,
                            "click position=" + vh.getAdapterPosition(),
                            Toast.LENGTH_SHORT).show();
                    try {
                        onPopupViewShow(v, itemPos);
                    } catch (JSONException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            return vh;
        }
    }

    /**
     * 显示浮层
     * @param v
     * @param Pos 卡片序号
     */
    private void onPopupViewShow(View v,int Pos) throws JSONException, InterruptedException {
        QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
        QMUIFrameLayout frameLayout = new QMUIFrameLayout(UserActivity.this);
        frameLayout.setBackground(
                QMUIResHelper.getAttrDrawable(UserActivity.this, R.attr.qmui_skin_support_popup_bg));
        builder.background(R.attr.qmui_skin_support_popup_bg);
        QMUISkinHelper.setSkinValue(frameLayout, builder);
        frameLayout.setRadius(QMUIDisplayHelper.dp2px(UserActivity.this, 12));
        int padding = QMUIDisplayHelper.dp2px(UserActivity.this, 20);
        frameLayout.setPadding(padding, padding, padding, padding);

        TextView textView = new TextView(UserActivity.this);
//            imageView.setLineSpacing(QMUIDisplayHelper.dp2px(AdminActivity.this, 4), 1.0f);
        textView.setPadding(padding, padding, padding, padding);
        textView.setText(UserInfoList.get(Pos));
//            imageView.setText("这是自定义显示的内容");
        textView.setTextColor(
                QMUIResHelper.getAttrColor(UserActivity.this, R.attr.app_skin_common_title_text_color));

        builder.clear();
        builder.textColor(R.attr.app_skin_common_title_text_color);
        QMUISkinHelper.setSkinValue(textView, builder);
//            imageView.setGravity(Gravity.CENTER);
//            imageView.


        builder.release();

        int size = QMUIDisplayHelper.dp2px(UserActivity.this, 200);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
        frameLayout.addView(textView, lp);

        QMUIPopups.fullScreenPopup(UserActivity.this)
                .addView(frameLayout)
                .closeBtn(true)
                .skinManager(QMUISkinManager.defaultInstance(UserActivity.this))
                .onBlankClick(new QMUIFullScreenPopup.OnBlankClickListener() {
                    @Override
                    public void onBlankClick(QMUIFullScreenPopup popup) {
                        Toast.makeText(UserActivity.this, "点击到空白区域", Toast.LENGTH_SHORT).show();
                    }
                })
                .onDismiss(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Toast.makeText(UserActivity.this, "onDismiss", Toast.LENGTH_SHORT).show();
                    }
                })
                .show(v);
    }


    private void initData() {
        Thread mGetInfo = new Thread(new Runnable() {
            @Override
            public void run() {
                UserInfo = getUserInfo(UID,token);
            }
        });
        mGetInfo.start();
        try {
            mGetInfo.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        UserInfoList = DataController.JSON_to_list(UserInfo);
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(layoutManager);
        new PagerSnapHelper().attachToRecyclerView(mRecyclerView);

        mAdapter = new Adapter(UserInfo);
        mRecyclerView.setAdapter(mAdapter);

        QMUIRVItemSwipeAction swipeAction = new QMUIRVItemSwipeAction(true,
                new QMUIRVItemSwipeAction.Callback() {});
        swipeAction.setPressTimeToSwipe(300);
        swipeAction.attachToRecyclerView(mRecyclerView);
    }


    /**+
     * 扫码button阴影
     */
    private void initQRcodeBtn() {
        int qradius= QMUILayoutHelper.RADIUS_OF_HALF_VIEW_WIDTH;
        QRbtnLyt.setRadiusAndShadow(qradius,
                QMUIDisplayHelper.dp2px(UserActivity.this,10),
                0.8f);
        // **************拉起相机扫码******************
        QRbtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(UserActivity.this,
                        CaptureActivity.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });
        // **************拉起相机扫码******************
    }

    /**
     * 设置topbar标题 和返回button
     */
    private void initTopbar() {
        mTopBar.setTitle("用户列表");
        // back button
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                UserActivity.this.finish();
            }
        });

        /**
         * 沉浸式状态栏
         */
        QMUIStatusBarHelper.translucent(this);
    }



    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            try {

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

    /**
     * 获取派送员对应的用户数据
     * @param UID 派送员账号
     * @param token 令牌
     * @return 用户数据
     */
    private JSONObject getUserInfo(String UID, String token) {
        Map<String,Object> params = new HashMap<>();
        params.put("UID", UID);
        params.put("token", token);
        return DataController.getJson(params,"http://0xdkxy.top:10000/user/getCostInfo");
    }

}