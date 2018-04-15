package com.levylin.skin.demo;

import android.app.Application;

import com.levylin.skin_core.SkinManager;


/**
 * @author Lance
 * @date 2018/3/8
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SkinManager.init(this);
    }
}
