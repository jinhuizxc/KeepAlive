package com.example.live_library.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.live_library.R;
import com.example.live_library.pro_sp.PreferenceUtil;

public class SPUtils {

    private static Context mContext;
    private static SharedPreferences mPreferences;

    public static SPUtils getInstance(Context context, String spName) {
        mContext = context;
        init(mContext);
        return new SPUtils();
    }

    public static void init(Context context) {
        if (mPreferences == null)
            mPreferences = PreferenceUtil.getSharedPreference(context, "DEV_YKUN");
    }


    public void putString(@NonNull final String key, final String value) {
//        put(key, value, false);
        putS(key, value);
    }

    public static void putS(String key, String def) {
        mPreferences.edit().putString(key, def).apply();
    }


    public String getString(@NonNull final String key) {
        return getS(key);
    }

    private static String getS(String key) {
        return mPreferences.getString(key, "");
    }


    public int getInt(@NonNull final String key) {
        return getI(key, R.drawable.ic_launcher);
    }

    public int getInt(@NonNull final String key, final int defaultValue) {
        return getI(key, defaultValue);
    }

    private static int getI(String key, int def) {
        return mPreferences.getInt(key, def);
    }

    public void putInt(@NonNull final String key, final int value) {
//        put(key, value, false);
        putI(key, value);
    }

    public static void putI(String key, int def) {
        mPreferences.edit().putInt(key, def).apply();
    }



}
