package com.levylin.skin_core;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created by LinXin on 2018/3/19.
 */
public class ActivityLifeCycle implements Application.ActivityLifecycleCallbacks {

    private HashMap<Activity, SkinFactory> mHashMap = new HashMap<>();

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        try {
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(inflater, false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        SkinFactory factory = new SkinFactory();
        mHashMap.put(activity, factory);
        LayoutInflaterCompat.setFactory2(inflater, factory);
        SkinManager.getInstance().addObserver(factory);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        SkinFactory factory = mHashMap.remove(activity);
        SkinManager.getInstance().deleteObserver(factory);
    }
}
