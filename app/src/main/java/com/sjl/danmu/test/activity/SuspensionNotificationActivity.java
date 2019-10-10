package com.sjl.danmu.test.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.sjl.danmu.test.R;
import com.sjl.danmu.test.service.SuspensionNotificationSevice;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename SuspensionNotificationActivity.java
 * @time 2019/9/16 16:15
 * @copyright(C) 2019 song
 */
public class SuspensionNotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suspension_notification_activity);
        Button start = (Button) findViewById(R.id.btn_start);
        start.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent service = new Intent();
                service.setClass(SuspensionNotificationActivity.this, SuspensionNotificationSevice.class);
                startService(service);
            }
        });

        Button stop = (Button) findViewById(R.id.btn_stop);
        stop.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceStop = new Intent();
                serviceStop.setClass(SuspensionNotificationActivity.this, SuspensionNotificationSevice.class);
                stopService(serviceStop);
            }
        });
    }
}
