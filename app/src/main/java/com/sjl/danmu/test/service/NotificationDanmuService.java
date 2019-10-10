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

import com.sjl.danmu.test.R;
import com.sjl.danmu.test.danmu.DanmuControl;

import master.flame.danmaku.controller.IDanmakuView;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename NotificationDanmuService.java
 * @time 2019/9/15 16:10
 * @copyright(C) 2019 song
 */
public class NotificationDanmuService extends Service {

    private IDanmakuView mDanmakuView;
    private DanmuControl danmuControl;

    private WindowManager wm;
    private WindowManager.LayoutParams wmParams;
    private View view;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("NotificationDanmu", "onCreate");
        //检查是否已经授予权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                //若未授权则请求权限
                createView();
                handler.postDelayed(task, 1000);
            }
        } else {
            createView();
            handler.postDelayed(task, 1000);
        }

    }

    private void createView() {
        view = LayoutInflater.from(this).inflate(R.layout.view_danmaku, null);
        mDanmakuView = view.findViewById(R.id.containerView);
        danmuControl = new DanmuControl(this, mDanmakuView);

        //https://www.cnblogs.com/yaowen/p/4912232.html
        //创建实现悬浮窗口效果，并把弹幕控件添加到窗口中显示
        // 获取WindowManager
        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);


        wmParams = new WindowManager.LayoutParams();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;//2002
        }

        wmParams.flags |= 8;
        /*
        * 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
        */
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        wmParams.gravity = Gravity.LEFT | Gravity.TOP; // 调整悬浮窗口至左上角
        // 以屏幕左上角为原点，设置x、y初始值
        wmParams.x = 0;
        wmParams.y = 0;
        // 设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.format = 1;

        wm.addView(view, wmParams);

    }

    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        public void run() {
            dataRefresh();
            handler.postDelayed(this, 1000);
            wm.updateViewLayout(view, wmParams);
        }
    };

    public void dataRefresh() {
        String avator = "http://g.hiphotos.baidu.com/image/h%3D200/sign=9b2f9371992397ddc9799f046983b216/dc54564e9258d1094dc90324d958ccbf6c814d7a.jpg";
        String name = "张三";
        String content = "收到礼物:" + System.currentTimeMillis();
        danmuControl.addDanmu(avator, name, content);
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
        if (danmuControl != null) {
            danmuControl.stop();
        }
        if (mDanmakuView != null) {
            mDanmakuView.release();
            mDanmakuView = null;
        }
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
