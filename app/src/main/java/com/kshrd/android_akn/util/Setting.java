package com.kshrd.android_akn.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;

import java.util.Locale;

/**
 * Created by Buth Mathearo on 2/4/2016.
 */
public class Setting {
    public static boolean IS_FIRST_USED;
    public static String LANGUAGE;
    public static boolean IS_ANIMATED;

    private static String sharedPrefName = "setting";
    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor sharedPrefEditor;
    private static Context mContext;

    private static Locale locale;
    private static Configuration config;

    public static String languages[] = {"en", "km"};

    // Check whether App is first used.
    public static boolean isFirstUsed(Context mContext) {
        sharedPref = mContext.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        return sharedPref.getBoolean("IS_FIRST_USED", true);
    }

    public static void setAsFirstUsed(Context mContext) {
        sharedPref = mContext.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putBoolean("IS_FIRST_USED", true);
        sharedPrefEditor.commit();
    }

    public static void setAsNotFirstUsed(Context mContext) {
        sharedPref = mContext.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putBoolean("IS_FIRST_USED", false);
        sharedPrefEditor.commit();
    }

    public static void checkInternetConnection(Activity activity) {
        
    }

    public static void loadLanguage(Context context) {
        if (locale == null) locale = new Locale(getLanguage(context));
        Locale.setDefault(locale);
        if (config == null) config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    public static void setLanguage(Context context, String language) {
        sharedPref = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putString("LANGUAGE", language);
        sharedPrefEditor.commit();
    }

    public static String getLanguage(Context context) {
        sharedPref = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        return sharedPref.getString("LANGUAGE", "en"); // en -> english
    }

    public static Typeface getTypeface(Context context) {
        if (getLanguage(context).equals("km")) {
            Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/KhmerOS.ttf");
            return font;
        }
        return Typeface.createFromAsset(context.getAssets(), "fonts/ubuntu.ttf");
    }


    public static void save(Context context, String language, boolean animate) {
        sharedPref = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putString("LANGUAGE", language);
        sharedPrefEditor.putBoolean("IS_ANIMATED", animate);
        sharedPrefEditor.commit();
    }

    public static void readSetting(Context context) {
        sharedPref = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        LANGUAGE = sharedPref.getString("LANGUAGE", "en");
        IS_ANIMATED = sharedPref.getBoolean("IS_ANIMATED", true);
    }

}
