package com.sjl.danmu.test.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.sjl.danmu.test.R;
import com.sjl.danmu.test.service.NotificationDanmuService;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename MainActivity.java
 * @time 2019/9/15 16:28
 * @copyright(C) 2019 song
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                //若未授权则请求权限
                getOverlayPermission();//getOverlayPermission
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1){
            if (Activity.RESULT_OK == resultCode){
                Toast.makeText(this,"悬浮窗授权通过",Toast.LENGTH_LONG).show();
            }
        }
    }

    //请求悬浮窗权限
    @TargetApi(Build.VERSION_CODES.M)
    private void getOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 1);
    }

    public void videoDanmu(View view) {
        Intent intent = new Intent(this, ViewDanmuActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("contentType", 0);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    public void videoDanmu2(View view) {
        Intent intent = new Intent(this, ViewDanmuActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("contentType", 1);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void notificationDanmu(View view) {
        startActivity(new Intent(this, GlobalNotificationDanmuActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop(NotificationDanmuService.class);
        stop(NotificationDanmuService.class);
    }

    private void stop(Class<?> clz) {
        Intent serviceStop = new Intent();
        serviceStop.setClass(this, clz);
        stopService(serviceStop);
    }

    public void test(View view) {
        startActivity(new Intent(this, TestActivity.class));
    }

    public void suspensionNotification(View view) {
        startActivity(new Intent(this, SuspensionNotificationActivity.class));

    }


}
