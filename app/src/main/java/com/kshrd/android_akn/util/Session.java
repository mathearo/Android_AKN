package com.kshrd.android_akn.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.FileOutputStream;

/**
 * Created by Buth Mathearo on 1/25/2016.
 *
 * This Class uses to keep user info when user login.
 */
public class Session {
    public static int USER_ID;
    public static String ENCRYPTED_USER_ID;
    public static String USER_NAME;
    public static String EMAIL;
    public static String PROFILE_IMAGE_URL;
    public static boolean IS_LOGIN;

    public static boolean IS_FIRST_USED;

    private static String sharedPrefName = "user_session";
    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor sharedPrefEditor;
    private static Context mContext;

    public static void setContext(Context ctx) {
        mContext = ctx;
    }

    // Save User info to SharedPreferenceFile
    public static void saveUserSession(Context context, int userId, String enUserId, String userName, String email,
                                       String profileUrl) {
        mContext = context;

        USER_ID = userId;
        ENCRYPTED_USER_ID = enUserId;
        USER_NAME = userName;
        EMAIL = email;
        PROFILE_IMAGE_URL = profileUrl;

        sharedPref = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putInt("USER_ID", userId);
        sharedPrefEditor.putString("ENCRYPTED_USER_ID", enUserId);
        sharedPrefEditor.putString("USER_NAME", userName);
        sharedPrefEditor.putString("EMAIL", email);
        sharedPrefEditor.putString("PROFILE_IMAGE_URL", profileUrl);
        sharedPrefEditor.putBoolean("IS_LOGIN", true);
        sharedPrefEditor.commit();
    }

    // Read User info to SharedPreferenceFile
    public static void readUserSession(Context context) {
        mContext = context;
        sharedPref = mContext.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        USER_ID = sharedPref.getInt("USER_ID", 0);
        ENCRYPTED_USER_ID = sharedPref.getString("ENCRYPTED_USER_ID", null);

        USER_NAME = sharedPref.getString("USER_NAME", null);
        EMAIL = sharedPref.getString("EMAIL", null);
        PROFILE_IMAGE_URL = sharedPref.getString("PROFILE_IMAGE_URL", null);
        IS_LOGIN = sharedPref.getBoolean("IS_LOGIN", false);
    }

    // Clear Session or clean all data from SharedPreferenceFile
    public static void cleanSession() {
        sharedPref = mContext.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.clear();
        sharedPrefEditor.commit();
    }


    public static void updateName(String newName) {
        sharedPref = mContext.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putString("USER_NAME", newName);
        sharedPrefEditor.commit();
    }

    public static void updateProfileImageUrl(String imgUrl) {
        sharedPref = mContext.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putString("PROFILE_IMAGE_URL", imgUrl);
        sharedPrefEditor.commit();
    }

    public static void updateProfileImageUrl(String imgUrl, Bitmap bitmap) {
        sharedPref = mContext.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();
        sharedPrefEditor.putString("PROFILE_IMAGE_URL", imgUrl);
        sharedPrefEditor.commit();
        if (bitmap != null) {
            Log.d("session_test", "save bitmap in session.");
            try {
                String cacheFile = mContext.getCacheDir() + "/mathearo_avatar.png";
                FileOutputStream out = new FileOutputStream(cacheFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    // user/1ab739c6-0691-40de-8add-1fb802a0c0a2.jpg

}
