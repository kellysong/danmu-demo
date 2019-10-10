package com.sjl.danmu.test.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.sjl.danmu.test.R;
import com.sjl.danmu.test.danmu.DanmuControl;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.IDanmakus;

public class ViewDanmuActivity extends AppCompatActivity implements View.OnClickListener {

    private IDanmakuView mDanmakuView;
    private Button mBtnSendDanmaku, mBtnSendDanmaku2;
    private EditText editText;
    private DanmuControl danmuControl;
    private VideoView videoView;
    private LinearLayout operationLayout;
    private int contentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_danmu_activity);
        mDanmakuView = (IDanmakuView) findViewById(R.id.sv_danmaku);
        editText = (EditText) findViewById(R.id.editText);
        mBtnSendDanmaku = (Button) findViewById(R.id.btn_send);
        mBtnSendDanmaku2 = (Button) findViewById(R.id.btn_send2);
        mBtnSendDanmaku.setOnClickListener(this);
        mBtnSendDanmaku2.setOnClickListener(this);
        Intent intent = getIntent();
        contentType = intent.getIntExtra("contentType", 0);
        if (contentType == 0) {
            danmuControl = new DanmuControl(0,this, mDanmakuView);
            mBtnSendDanmaku.setVisibility(View.VISIBLE);
            mBtnSendDanmaku2.setVisibility(View.GONE);
        } else {
            danmuControl = new DanmuControl(1,this, mDanmakuView);
            mBtnSendDanmaku.setVisibility(View.GONE);
            mBtnSendDanmaku2.setVisibility(View.VISIBLE);
        }

        operationLayout = (LinearLayout) findViewById(R.id.operation_layout);

        videoView = (VideoView) findViewById(R.id.video_view);
        //设置videoView播放在线视频的路径
        videoView.setVideoPath("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
        videoView.start();
        mDanmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {
            @Override
            public boolean onDanmakuClick(IDanmakus danmakus) {
                return false;
            }

            @Override
            public boolean onDanmakuLongClick(IDanmakus danmakus) {
                return false;
            }

            @Override
            public boolean onViewClick(IDanmakuView view) {
                if (operationLayout.getVisibility() == View.GONE) {
                    operationLayout.setVisibility(View.VISIBLE);
                } else {
                    operationLayout.setVisibility(View.GONE);
                }
                return false;
            }
        });
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    onWindowFocusChanged(true);
                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //回车键
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    if (contentType == 0) {
                        send();
                    }else {
                        send2();
                    }

                }
                return true;
            }
        });

    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                send();
                break;
            case R.id.btn_send2:
                send2();
                break;
            default:
                break;
        }


    }


    private void send2() {
        String content = editText.getText().toString().trim();
        if (content != null && content.length() > 0) {
            danmuControl.addDanmu(content);
//            editText.setText("");
            hideOperationLayout();
            hideSoftInput();

        } else {
            Toast.makeText(this, "发送内容为空", Toast.LENGTH_LONG).show();
        }
    }

    private void hideOperationLayout() {
        if (operationLayout.getVisibility() == View.GONE) {
            operationLayout.setVisibility(View.VISIBLE);
        } else {
            operationLayout.setVisibility(View.GONE);
        }
    }

    private void send() {
        String avator = "http://g.hiphotos.baidu.com/image/h%3D200/sign=9b2f9371992397ddc9799f046983b216/dc54564e9258d1094dc90324d958ccbf6c814d7a.jpg";
        String name = "张三";
        String content = editText.getText().toString().trim();
        if (content != null && content.length() > 0) {
            for (int i = 0; i < 5; i++) {
                danmuControl.addDanmu(avator, "i" + name, content);
            }
            editText.setText("");
            hideOperationLayout();
            hideSoftInput();
        } else {
            Toast.makeText(this, "发送内容为空", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (danmuControl != null) {
            danmuControl.stop();
        }
        if (mDanmakuView != null) {
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}