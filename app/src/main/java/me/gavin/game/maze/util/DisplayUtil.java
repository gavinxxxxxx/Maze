package me.gavin.game.maze.util;

import android.content.res.Resources;

/**
 * 屏幕工具类
 *
 * @author gavin.xiong 2016/9/10
 */
public class DisplayUtil {

    public static int dp2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(float spValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (spValue * scale + 0.5f);
    }

    public static int px2sp(float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

}
