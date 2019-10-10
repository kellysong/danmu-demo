package com.sjl.danmu.test.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.sjl.danmu.test.R;

/**
 * TODO
 *
 * @author Kelly
 * @version 1.0.0
 * @filename TestActivity.java
 * @time 2019/9/15 16:26
 * @copyright(C) 2019 song
 */
public class TestActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
    }
}
