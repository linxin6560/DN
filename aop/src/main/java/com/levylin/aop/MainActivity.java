package com.levylin.aop;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @BehaviorTrace("按钮1")
    public void click1(View view) {
        SystemClock.sleep(new Random().nextInt(2000));
    }

    @BehaviorTrace("按钮3")
    public void click2(View view) {
        SystemClock.sleep(new Random().nextInt(2000));
    }

    @BehaviorTrace("按钮2")
    public void click3(View view) {
        SystemClock.sleep(new Random().nextInt(2000));
    }
}
