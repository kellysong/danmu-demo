package com.sjl.danmu.test.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.sjl.danmu.test.R;
import com.sjl.danmu.test.service.NotificationDanmuService;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename GlobalNotificationDanmuActivity.java
 * @time 2019/9/15 16:19
 * @copyright(C) 2019 song
 */
public class GlobalNotificationDanmuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.global_notification_danmu__activity);
        Button start = (Button) findViewById(R.id.btn_start);
        start.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent();
                service.setClass(GlobalNotificationDanmuActivity.this, NotificationDanmuService.class);
                startService(service);
            }
        });

        Button stop = (Button) findViewById(R.id.btn_stop);
        stop.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceStop = new Intent();
                serviceStop.setClass(GlobalNotificationDanmuActivity.this, NotificationDanmuService.class);
                stopService(serviceStop);
            }
        });
    }
}
