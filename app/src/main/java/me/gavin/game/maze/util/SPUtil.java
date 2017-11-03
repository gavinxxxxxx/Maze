package me.gavin.game.maze.util;

import android.content.Context;

import me.gavin.game.maze.app.App;

/**
 * SharedPreferences 数据存储工具类
 *
 * @author gavin.xiong
 */
public class SPUtil {

    private static String PREFERENCE = "PREFERENCE";

    public static void saveInt(String key, int value) {
        App.get().getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE)
                .edit()
                .putInt(key, value)
                .apply();
    }

    public static int getInt(String key, int defVal) {
        return App.get().getSharedPreferences(PREFERENCE, Context.MODE_PRIVATE)
                .getInt(key, defVal);
    }

}
