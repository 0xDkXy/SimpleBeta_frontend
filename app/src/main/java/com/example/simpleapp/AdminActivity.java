package com.example.simpleapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import com.example.simpleapp.model.DataController;
import com.qmuiteam.qmui.QMUIInterpolatorStaticHolder;
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
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.popup.QMUIFullScreenPopup;
import com.qmuiteam.qmui.widget.popup.QMUIPopups;
import com.qmuiteam.qmui.widget.pullLayout.QMUIPullLayout;

import com.example.simpleapp.adaptor.QDRecyclerViewAdapter;
import com.example.simpleapp.model.DataController;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;

import org.json.JSONException;
import org.json.JSONObject;

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
    private String mTopBarTitle = "????????????";
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

    private void onRefreshUserInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                allUserInfo = getUserInfo("admin",token);
            }
        }).start();
    }

    private void initAddItemBtn() {
        int qradius= QMUILayoutHelper.RADIUS_OF_HALF_VIEW_WIDTH;
        mAddItemBtnLyt.setRadiusAndShadow(qradius,
                QMUIDisplayHelper.dp2px(AdminActivity.this,10),
                0.8f);

        String temp[] = {"CName","phone","addres","UID"};
        final String info[] = new String[4];
        final QMUIDialog.EditTextDialogBuilder Builder[] = new QMUIDialog.EditTextDialogBuilder[4];
        for(int i=0;i<4;++i){
            Builder[i]=new QMUIDialog.EditTextDialogBuilder(AdminActivity.this);
        }


        for(int i=0;i<4;++i){
            final int ii=i;
            Builder[i].setTitle("????????????")
                    .setPlaceholder(temp[i])
                    .setInputType(InputType.TYPE_CLASS_TEXT)
                    .addAction("??????", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .addAction("??????", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            CharSequence text = Builder[ii].getEditText().getText();
                            if (text != null && text.length() > 0) {
                                info[ii]=text.toString();
                                dialog.dismiss();
                                if(ii !=3)
                                    Builder[ii+1].show();
                                else {
                                    mAddUserInfo="?????????" + info[0] +
                                            "\n?????????" + info[1] +
                                            "\n?????????" + info[2] +
                                            "\n????????????" + info[3];
                                    Thread threadt=new Thread(new Runnable() {
                                        @Override
                                        public void run() {
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
                                            .setTipWord("????????????")
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
                                        Toast.makeText(AdminActivity.this, "???????????????????????????",
                                            Toast.LENGTH_SHORT).show();
                                        onRefreshData();
                                        onRefreshUserInfo();
                                    }else{
                                        Toast.makeText(AdminActivity.this, "???????????????????????????",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(AdminActivity.this, "?????????", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        mAddItemBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Builder[0].show();
            }
        });
    }

    private Bitmap mQrCodeBitmap;
    class Adapter extends QDRecyclerViewAdapter {

        public Adapter(JSONObject res) {
            super();
            super.mItems = DataController.JSON_to_list(res);
        }

        @Override
        public void removeItem(int position) throws JSONException {
            if (position >= mItems.size()) return;
            String CID=allUserInfo.getJSONObject(String.valueOf(position)).getString("CID");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DataController.deleteUserInfo("http://0xdkxy.top:10000/user/deleteByCID","admin",CID,token);
                }
            }).start();
            mItems.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View root = inflater.inflate(R.layout.recycler_view_item, viewGroup, false);
            final ViewHolder vh = new ViewHolder(root,this);
            root.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    int itemPos = vh.getAdapterPosition();
                    Toast.makeText(AdminActivity.this,
                            "click position=" + vh.getAdapterPosition(),
                            Toast.LENGTH_SHORT).show();
                    try {
                        onPopupViewShow(v,itemPos);
                    } catch (JSONException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            return vh;
        }

        /**
         * ????????????
         * @param v
         * @param Pos ????????????
         */
        private void onPopupViewShow(View v,int Pos) throws JSONException, InterruptedException {
            QMUISkinValueBuilder builder = QMUISkinValueBuilder.acquire();
            QMUIFrameLayout frameLayout = new QMUIFrameLayout(AdminActivity.this);
            frameLayout.setBackground(
                    QMUIResHelper.getAttrDrawable(AdminActivity.this, R.attr.qmui_skin_support_popup_bg));
            builder.background(R.attr.qmui_skin_support_popup_bg);
            QMUISkinHelper.setSkinValue(frameLayout, builder);
            frameLayout.setRadius(QMUIDisplayHelper.dp2px(AdminActivity.this, 12));
            int padding = QMUIDisplayHelper.dp2px(AdminActivity.this, 20);
            frameLayout.setPadding(padding, padding, padding, padding);

            ImageView imageView = new ImageView(AdminActivity.this);
//            imageView.setLineSpacing(QMUIDisplayHelper.dp2px(AdminActivity.this, 4), 1.0f);
            imageView.setPadding(padding, padding, padding, padding);
//            imageView.setText("??????????????????????????????");
//            imageView.setTextColor(
//                    QMUIResHelper.getAttrColor(AdminActivity.this, R.attr.app_skin_common_title_text_color));

            builder.clear();
            builder.textColor(R.attr.app_skin_common_title_text_color);
            QMUISkinHelper.setSkinValue(imageView, builder);
//            imageView.setGravity(Gravity.CENTER);
//            imageView.

            Thread getQR = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mQrCodeBitmap = DataController.getQRCode("admin",
                                allUserInfo.getJSONObject(String.valueOf(Pos)).getString("CID"),
                                token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            getQR.start();
            getQR.join();
            imageView.setImageBitmap(mQrCodeBitmap);
            builder.release();

            int size = QMUIDisplayHelper.dp2px(AdminActivity.this, 200);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(size, size);
            frameLayout.addView(imageView, lp);

            QMUIPopups.fullScreenPopup(AdminActivity.this)
                    .addView(frameLayout)
                    .closeBtn(true)
                    .skinManager(QMUISkinManager.defaultInstance(AdminActivity.this))
                    .onBlankClick(new QMUIFullScreenPopup.OnBlankClickListener() {
                        @Override
                        public void onBlankClick(QMUIFullScreenPopup popup) {
                            Toast.makeText(AdminActivity.this, "?????????????????????", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .onDismiss(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            Toast.makeText(AdminActivity.this, "onDismiss", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            viewHolder.setText(super.mItems.get(i));
        }

    }

    /**
     * ??????recyclerView
     */
    private void onRefreshData() {
        List<String> data = new ArrayList<>();
        data.add(mAddUserInfo);
        mAdapter.append(data);
    }

    /**
     * ??????????????????,????????????????????????
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
     * ?????????RecyclerView????????????
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
                try {
                    mAdapter.removeItem(viewHolder.getAdapterPosition());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            /**
             * ????????????????????????
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
                    mTopBar.setTitle("????????????");
                    selected.itemView.animate()
                            .scaleX(1.02f)
                            .scaleY(1.02f)
                            .setInterpolator(QMUIInterpolatorStaticHolder.ACCELERATE_INTERPOLATOR)
                            .setDuration(250)
                            .start();

                    // ??????
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
     * ????????????????????????
     * @param receiverName ??????????????????
     * @param receiverPhone ??????????????????
     * @param receiverAddress ??????????????????
     * @param senderId ?????????id
     * @param token ???????????????
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
            Log.i(tag,"???????????????????????????");
            return true;
        } else {
            Log.e(tag,"???????????????????????????");
            return false;
        }
    }

    /**
     * ???????????????????????????
     * @param UID ???????????????
     * @param token ?????????????????????
     * @return ??????????????????
     */
    private JSONObject getUserInfo(String UID, String token){
        Map<String, Object> params = new HashMap<>();
        params.put("UID", UID);
        params.put("token", token);
        return DataController.getJson(params,"http://0xdkxy.top:10000/user/getCostInfo");
    }
}