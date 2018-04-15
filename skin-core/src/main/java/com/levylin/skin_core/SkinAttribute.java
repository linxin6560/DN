package com.levylin.skin_core;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.levylin.skin_core.utils.SkinResources;
import com.levylin.skin_core.utils.SkinThemeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 皮肤属性
 * Created by LinXin on 2018/3/20.
 */
public class SkinAttribute {

    private static final List<String> mAttributes = new ArrayList<>();

    static {
        mAttributes.add("background");
        mAttributes.add("src");
        mAttributes.add("tint");
        mAttributes.add("textColor");
        mAttributes.add("drawableLeft");
        mAttributes.add("drawableRight");
        mAttributes.add("drawableTop");
        mAttributes.add("drawableBottom");
    }

    private List<SkinView> mSkinViews = new ArrayList<>();

    public void load(View view, AttributeSet attrs) {
        List<SkinPair> skinPairs = new ArrayList<>();
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String name = attrs.getAttributeName(i);
            if (mAttributes.contains(name)) {
                //是否包含需要换肤的属性
                String value = attrs.getAttributeValue(i);
                if (value.contains("#")) {
                    continue;
                }
                int resId;
                if (value.contains("?")) {
                    int attrId = Integer.parseInt(value.substring(1));
                    resId = SkinThemeUtils.getResId(view.getContext(), new int[]{attrId})[0];
                } else {
                    //@
                    resId = Integer.parseInt(value.substring(1));
                }
                if (resId != 0) {
                    skinPairs.add(new SkinPair(name, resId));
                }
            }
        }
        //将View与可动态替换的属性集合保存起来
        if (!skinPairs.isEmpty()) {
            SkinView skinView = new SkinView(view, skinPairs);
            skinView.applySkin();
            mSkinViews.add(skinView);
        }
    }

    public void applySkin() {
        for (SkinView skinView : mSkinViews) {
            skinView.applySkin();
        }
    }

    static class SkinView {
        View mView;
        List<SkinPair> mSkinPairs;

        SkinView(View view, List<SkinPair> skinPairs) {
            mView = view;
            mSkinPairs = skinPairs;
        }

        void applySkin() {
            for (SkinPair skinPair : mSkinPairs) {
                Drawable left = null, top = null, right = null, bottom = null;
                switch (skinPair.attributeName) {
                    case "background":
                        Object background = SkinResources.getInstance().getBackground(skinPair.resId);
                        //Color
                        if (background instanceof Integer) {
                            mView.setBackgroundColor((Integer) background);
                        } else {
                            ViewCompat.setBackground(mView, (Drawable) background);
                        }
                        break;
                    case "src":
                        background = SkinResources.getInstance().getBackground(skinPair.resId);
                        if (background instanceof Integer) {
                            ((ImageView) mView).setImageDrawable(new ColorDrawable((Integer) background));
                        } else {
                            ((ImageView) mView).setImageDrawable((Drawable) background);
                        }
                        break;
                    case "textColor":
                        ((TextView) mView).setTextColor(SkinResources.getInstance().getColorStateList(skinPair.resId));
                        break;
                    case "drawableLeft":
                        left = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableTop":
                        top = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableRight":
                        right = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableBottom":
                        bottom = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    default:
                        break;
                }
                if (null != left || null != right || null != top || null != bottom) {
                    ((TextView) mView).setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
                }
            }
        }
    }

    static class SkinPair {
        String attributeName;
        int resId;

        public SkinPair(String attributeName, int resId) {
            this.attributeName = attributeName;
            this.resId = resId;
        }
    }
}
