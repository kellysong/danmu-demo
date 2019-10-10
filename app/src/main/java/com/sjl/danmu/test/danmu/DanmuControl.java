package com.sjl.danmu.test.danmu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.util.IOUtils;

/**
 * 弹幕控制器
 *
 * @author Kelly
 * @version 1.0.0
 * @filename DanmuControl.java
 * @time 2019/9/3 16:42
 * @copyright(C) 2019 song
 */
public class DanmuControl {
    private static final String TAG = "DanmuControl";
    private Context mContext;
    private IDanmakuView mDanmakuView;
    private DanmakuContext mDanmakuContext;
    private MyDanmakuParser danmakuParser;
    private Thread danmuThread;
    private Object objects = new Object();
    private volatile boolean runningFlag = false;
    private LinkedList<DanmuBean> danmuBeans = new LinkedList<>();
    private Map<String, Bitmap> cacheHeaders = new ConcurrentHashMap<>();

    private int contentType = 0;


    public DanmuControl(Context context, IDanmakuView danmakuView) {
        this(0, context, danmakuView);
    }

    /**
     * @param contentType 0图文，1普通文本
     * @param context
     * @param danmakuView
     */
    public DanmuControl(int contentType, Context context, IDanmakuView danmakuView) {
        if (contentType < 0 || contentType > 1) {
            throw new RuntimeException("非法contentType:" + contentType);
        }
        this.contentType = contentType;
        this.mContext = context;
        this.mDanmakuView = danmakuView;
        initDanmuConfig();
        if (contentType == 0) {
            initDanmuConsumer();
        }
    }

    /**
     * 初始化配置
     */
    private void initDanmuConfig() {
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        danmakuParser = new MyDanmakuParser();
        mDanmakuContext = DanmakuContext.create();
        mDanmakuContext
                .setDanmakuStyle(IDisplayer.DANMAKU_STYLE_NONE)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(2f)//越大速度越慢
                .setScaleTextSize(1.2f)
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair);
        if (contentType == 0) {
            mDanmakuContext.setCacheStuffer(new MyCacheStuffer(mContext), mCacheStufferAdapter);
        }

        if (mDanmakuView != null) {
            mDanmakuView.setCallback(new DrawHandler.Callback() {
                @Override
                public void prepared() {
                    mDanmakuView.start();
                }

                @Override
                public void updateTimer(DanmakuTimer timer) {
                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
                }

                @Override
                public void drawingFinished() {
                }
            });
        }
        //设置解析器
        mDanmakuView.prepare(danmakuParser, mDanmakuContext);
        mDanmakuView.enableDanmakuDrawingCache(true);
    }

    private static class MyDanmakuParser extends BaseDanmakuParser {
        @Override
        protected Danmakus parse() {
            return new Danmakus();
        }
    }

    private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {

        @Override
        public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
            // tag包含bitmap，一定要清空
            danmaku.tag = null;
        }
    };

    /**
     * 初始化弹幕消费者
     */
    private void initDanmuConsumer() {
        runningFlag = true;
        danmuThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!runningFlag) {
                        break;
                    }
                    if (cacheHeaders.size() > 100) {
                        cacheHeaders.clear();
                    }
                    //开始消费弹幕
                    synchronized (objects) {
                        if (danmuBeans.size() > 0) {
                            DanmuBean danmuBean = danmuBeans.removeFirst();
                            Bitmap bitmap = cacheHeaders.get(danmuBean.avatorUrl);
                            if (bitmap == null) {
                                InputStream inputStream = null;
                                try {
                                    // 从网络获取图片并且保存到一个bitmap里或者使用glide获取bitmap
                                    URLConnection urlConnection = new URL(danmuBean.avatorUrl).openConnection();
                                    inputStream = urlConnection.getInputStream();
                                    bitmap = BitmapFactory.decodeStream(inputStream);

                                    bitmap = makeRoundCorner(bitmap);
                                    crateDanmu(danmuBean, bitmap);
                                    cacheHeaders.put(danmuBean.avatorUrl, bitmap);
                                } catch (Exception e) {
                                    Log.e("SIMPLE_LOGGER", "获取头像异常", e);
                                } finally {
                                    IOUtils.closeQuietly(inputStream);
                                }
                            } else {
                                crateDanmu(danmuBean, bitmap);
                            }

                        }
                    }
                }
                Log.i(TAG, "弹幕控制器运行结束：" + danmuBeans.size());
                danmuBeans.clear();
                danmuBeans = null;
            }
        });
        danmuThread.start();
    }

    private void crateDanmu(DanmuBean danmuBean, Bitmap bitmap) {
        BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        //组装需要传递给danmaku的数据
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", danmuBean.name);
        map.put("content", danmuBean.content);
        map.put("bitmap", bitmap);
        danmaku.tag = map;
        danmaku.text = "";
        danmaku.padding = 0;
        danmaku.priority = 1;  // 设置最大行数无效
        danmaku.isLive = true;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 0;
        mDanmakuView.addDanmaku(danmaku);
        Log.i(TAG, "剩余弹幕数量：" + danmuBeans.size());

    }


    /**
     * 添加弹幕(生产弹幕)
     *
     * @param avatorUrl
     * @param name
     * @param content
     */
    public void addDanmu(final String avatorUrl, final String name, final String content) {
        checkContentType(0);
        DanmuBean danmuBean = new DanmuBean();
        danmuBean.avatorUrl = avatorUrl;
        danmuBean.name = name;
        danmuBean.content = content;
        synchronized (objects) {//生产弹幕
            danmuBeans.add(danmuBean);
        }
    }

    /**
     * 添加一条普通的弹幕
     *
     * @param content
     */
    public void addDanmu(String content) {
        checkContentType(1);
        BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        danmaku.text = content;
        danmaku.padding = 5;
        danmaku.borderColor = Color.RED;//弹幕边框
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 20f * (danmakuParser.getDisplayer().getDensity() - 0.6f);
//        danmaku.priority = 1;
        danmaku.textColor = Color.argb(new Random().nextInt(256), new Random().nextInt(256),
                new Random().nextInt(256), new Random().nextInt(256));
        danmaku.setTime(mDanmakuView.getCurrentTime());
        mDanmakuView.addDanmaku(danmaku);
    }

    private void checkContentType(int contentType) {
        if (this.contentType != contentType) {
            throw new RuntimeException("非法contentType:" + this.contentType);
        }
    }


    @Deprecated
    public void addDanmu2(final String avatorUrl, final String name, final String content) {
        final BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        new Thread() {
            @Override
            public void run() {
                InputStream inputStream = null;
                try {
                    // 从网络获取图片并且保存到一个bitmap里
                    URLConnection urlConnection = new URL(avatorUrl).openConnection();
                    inputStream = urlConnection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmap = makeRoundCorner(bitmap);

                    // 组装需要传递给danmaku的数据
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("name", name);
                    map.put("content", content);
                    map.put("bitmap", bitmap);
                    danmaku.tag = map;
                } catch (Exception e) {
                    Log.e("SIMPLE_LOGGER", "获取头像异常", e);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
                danmaku.text = "";
                danmaku.padding = 0;
                danmaku.priority = 1;  // 一定会显示, 一般用于本机发送的弹幕
                danmaku.isLive = true;
                danmaku.setTime(mDanmakuView.getCurrentTime() + 1000);
                danmaku.textSize = 0;
                mDanmakuView.addDanmaku(danmaku);
            }
        }.start();
    }

    /**
     * 将图片变成圆形
     *
     * @param bitmap
     * @return
     */
    private static Bitmap makeRoundCorner(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int left = 0, top = 0, right = width, bottom = height;
        float roundPx = height / 2;
        if (width > height) {
            left = (width - height) / 2;
            top = 0;
            right = left + height;
            bottom = height;
        } else if (height > width) {
            left = 0;
            top = (height - width) / 2;
            right = width;
            bottom = top + width;
            roundPx = width / 2;
        }
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(left, top, right, bottom);
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * 停止运行
     */
    public void stop() {
        runningFlag = false;
        if (danmuThread != null) {
            danmuThread = null;
        }
    }
}
