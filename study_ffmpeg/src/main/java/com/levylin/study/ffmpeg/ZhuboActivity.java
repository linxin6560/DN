package com.levylin.study.ffmpeg;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ZhuboActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhubo);
    }

    public void zhubo_test(View view) {
        ZhuBo zhuBo = new ZhuBo();
        zhuBo.test();
    }
}
