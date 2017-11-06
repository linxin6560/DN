package com.levylin.study.ffmpeg;

import android.app.Application;

/**
 * Created by LinXin on 2017/11/5.
 */

public class MyAppliacation extends Application {

    private static MyAppliacation appliacation;

    @Override
    public void onCreate() {
        super.onCreate();
        appliacation = this;
    }

    public static MyAppliacation getAppliacation() {
        return appliacation;
    }
}
