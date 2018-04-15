package com.levylin.skin_core;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.levylin.skin_core.utils.SkinPreference;
import com.levylin.skin_core.utils.SkinResources;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Observable;

/**
 * Created by LinXin on 2018/3/18.
 */
public class SkinManager extends Observable {

    private static SkinManager mInstance;
    private final Application application;

    private SkinManager(Application application) {
        this.application = application;
        SkinPreference.init(application);
        SkinResources.init(application);
        application.registerActivityLifecycleCallbacks(new ActivityLifeCycle());
        loadSkin(SkinPreference.getInstance().getSkin());
    }

    public static void init(Application application) {
        synchronized (SkinManager.class) {
            if (mInstance == null) {
                mInstance = new SkinManager(application);
            }
        }
    }

    public static SkinManager getInstance() {
        return mInstance;
    }

    /**
     * 皮肤路径
     *
     * @param path
     */
    public void loadSkin(String path) {
        if (TextUtils.isEmpty(path)) {
            SkinPreference.getInstance().setSkin("");
            SkinResources.getInstance().reset();
        } else {
            try {
                AssetManager assetManager = AssetManager.class.newInstance();
                Method method = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
                method.setAccessible(true);
                method.invoke(assetManager, path);

                Resources appResources = application.getResources();
                Resources skinResources = new Resources(assetManager, appResources.getDisplayMetrics(), appResources.getConfiguration());

                File file = new File(path);
                Log.i("Skin", "file.exist=" + file.exists());

                PackageManager pm = application.getPackageManager();
                PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
                SkinResources.getInstance().applySkin(skinResources, info.packageName);
                SkinPreference.getInstance().setSkin(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setChanged();
        notifyObservers();
    }
}
