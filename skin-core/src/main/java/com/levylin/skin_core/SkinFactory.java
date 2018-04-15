package com.levylin.skin_core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by LinXin on 2018/3/19.
 */
public class SkinFactory implements LayoutInflater.Factory2 ,Observer{

    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.webkit.",
            "android.app."
    };

    static final Class<?>[] mConstructorSignature = new Class[]{
            Context.class, AttributeSet.class};

    private static final HashMap<String, Constructor<? extends View>> sConstructorMap = new HashMap<>();

    private SkinAttribute mSkinAttribute;

    public SkinFactory() {
        mSkinAttribute = new SkinAttribute();
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = createViewFromTag(name, context, attrs);//android源生View
        if (view == null) {
            view = createView(name, context, attrs);//自定义View
        }
        mSkinAttribute.load(view, attrs);
        return view;
    }

    private View createViewFromTag(String name, Context context, AttributeSet attrs) {
        if (name.contains(".")) {
            return null;
        }
        View view;
        for (String prefix : sClassPrefixList) {
            view = createView(prefix + name, context, attrs);
            if (view != null) {
                return view;
            }
        }
        return null;
    }

    private View createView(String name, Context context, AttributeSet attrs) {
        Constructor<? extends View> constructor = sConstructorMap.get(name);
        if (constructor == null) {
            try {
                Class<? extends View> clazz = context.getClassLoader().loadClass(name).asSubclass(View.class);
                constructor = clazz.getConstructor(mConstructorSignature);
                constructor.setAccessible(true);
                sConstructorMap.put(name, constructor);
            } catch (Exception e) {
            }
        }
        if (constructor != null) {
            try {
                return constructor.newInstance(context, attrs);
            } catch (Exception e) {
            }
        }
        return null;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        mSkinAttribute.applySkin();
    }
}
