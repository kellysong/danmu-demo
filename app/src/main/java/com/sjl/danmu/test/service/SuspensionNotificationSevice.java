package com.sjl.danmu.test.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.sjl.danmu.test.R;

import java.util.Random;

/**
 * 悬浮通知
 *
 * @author Kelly
 * @version 1.0.0
 * @filename SuspensionNotificationSevice.java
 * @time 2019/9/16 16:18
 * @copyright(C) 2019 song
 */
public class SuspensionNotificationSevice extends Service {

    private WindowManager wm;
    private WindowManager.LayoutParams wmParams;
    private View view;
    private TextView tv_sum;
    private Random r = new Random();
    private int delayMillis = 2000;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("NotificationDanmu", "onCreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                createView();
                handler.postDelayed(task, delayMillis);
            }
        } else {
            createView();
            handler.postDelayed(task, delayMillis);
        }
    }

    private void createView() {
        view = LayoutInflater.from(this).inflate(R.layout.view_notice, null);
        tv_sum = view.findViewById(R.id.tv_sum);
        ImageView iv_close = view.findViewById(R.id.iv_close);
        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.setVisibility(View.GONE);
            }
        });
        //https://www.cnblogs.com/yaowen/p/4912232.html
        //创建实现悬浮窗口效果，并把弹幕控件添加到窗口中显示
        // 获取WindowManager
        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        wmParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;//2002
        }
        wmParams.flags |= 8;
        /*
        * 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
        */
//        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        wmParams.gravity = Gravity.RIGHT | Gravity.TOP; // 调整悬浮窗口至左上角

        // 以屏幕左上角为原点，设置x、y初始值
        wmParams.x = 0;
        wmParams.y = 0;
        // 设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.format = 1;

        wm.addView(view, wmParams);

    }

    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        public void run() {
            dataRefresh();
            handler.postDelayed(this, delayMillis);
            wm.updateViewLayout(view, wmParams);
        }
    };

    public void dataRefresh() {
        int sum = r.nextInt(5) + 1;
        tv_sum.setText(sum + "个审核任务待处理");
        if (view.getVisibility() == View.GONE) {
            view.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onStart(Intent intent, int startId) {
        Log.d("NotificationDanmu", "onStart");
//		setForeground(true);
        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(task);
        Log.d("NotificationDanmu", "onDestroy");
        if (wm != null) {
            wm.removeView(view);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
