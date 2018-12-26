package com.sugarsvalley.svpn.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.view.WindowManager;

/**
 * 像素转换工具
 *
 * @author fmz@zniot.com
 */
public class PixelUtil {

    /**
     * The context.
     */
    private static Context mContext;

    /**
     * 获取屏幕宽度
     *
     * @return
     */
    public static int getWindowWidth(Context context) {
        mContext = context;
        Point size = new Point();
        WindowManager wm = (WindowManager) mContext.getSystemService(
                Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(size);

        return size.x;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getWindowHeight(Context context) {
        mContext = context;
        Point size = new Point();
        WindowManager wm = (WindowManager) mContext.getSystemService(
                Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(size);

        return size.y;
    }

    /**
     * dp转 px.
     *
     * @param value the value
     * @return the int
     */
    public static int dp2px(float value) {
        //获取手机屏幕参数
        final float scale = mContext.getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);//密度转换为像素
    }
    /**
     * dp转 px.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int dp2px(float value, Context context) {
        mContext = context;
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) (value * (scale / 160) + 0.5f);
    }

    /**
     * px转dp.
     *
     * @param value the value
     * @return the int
     */
    public static int px2dp(float value) {
        final float scale = mContext.getResources().getDisplayMetrics().densityDpi;
        return (int) ((value * 160) / scale + 0.5f);
    }

    /**
     * px转dp.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int px2dp(float value, Context context) {
        final float scale = context.getResources().getDisplayMetrics().densityDpi;
        return (int) ((value * 160) / scale + 0.5f);
    }

    /**
     * sp转px.
     *
     * @param value the value
     * @return the int
     */
    public static int sp2px(float value) {
        Resources r;
        if (mContext == null) {
            r = Resources.getSystem();
        } else {
            r = mContext.getResources();
        }
        float spvalue = value * r.getDisplayMetrics().scaledDensity;
        return (int) (spvalue + 0.5f);
    }

    /**
     * sp转px.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int sp2px(float value, Context context) {
        Resources r;
        if (context == null) {
            r = Resources.getSystem();
        } else {
            r = context.getResources();
        }
        float spvalue = value * r.getDisplayMetrics().scaledDensity;
        return (int) (spvalue + 0.5f);
    }

    /**
     * px转sp.
     *
     * @param value the value
     * @return the int
     */
    public static int px2sp(float value) {
        final float scale = mContext.getResources().getDisplayMetrics().scaledDensity;
        return (int) (value / scale + 0.5f);
    }

    /**
     * px转sp.
     *
     * @param value   the value
     * @param context the context
     * @return the int
     */
    public static int px2sp(float value, Context context) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (value / scale + 0.5f);
    }
}