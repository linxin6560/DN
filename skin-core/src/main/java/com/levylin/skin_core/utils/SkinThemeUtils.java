package com.levylin.skin_core.utils;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * Created by LinXin on 2018/3/20.
 */

public class SkinThemeUtils {

    public static int[] getResId(Context context,int[] attrs){
        int[] resIds = new int[attrs.length];
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        for (int i = 0; i < attrs.length; i++) {
            resIds[i] = typedArray.getResourceId(i,0);
        }
        typedArray.recycle();
        return resIds;
    }
}
