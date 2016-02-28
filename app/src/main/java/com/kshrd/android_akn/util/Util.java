package com.kshrd.android_akn.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RetryPolicy;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.app.AppController;
import com.kshrd.android_akn.app.LoginActivity;
import com.kshrd.android_akn.app.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Buth Mathearo on 1/18/2016.
 */

public class Util {
    private static SweetAlertDialog pDialog;
    private static ProgressDialog proDialog;
    private static AppController appController;
    private static Tracker mTracker;

    // API KEYS
    public static final String API_KEY = "Authorization";
    public static final String API_KEY_VALUE = "Basic YXBpOmFrbm5ld3M=";
    public static final String BASE_URL_WITH_SLASH = "http://akn.khmeracademy.org/";
    public static final String BASE_URL = "http://akn.khmeracademy.org";
    public static final String BASE_LOGO_URL = BASE_URL + "/resources/images/logo/";

    public static final String BASE_PROFILE_IMAGE_URL = "http://api.khmeracademy.org/resources/upload/file/";

    // No need to use this url now
    //public static final String BASE_PROFILE_IMAGE_URL = "http://akn.khmeracademy.org/resources/images/user/";

    public static final String SAVE_LIST_ACTIVITY = "com.kshrd.android_akn.app.SaveListActivity";

    /*public static final String BASE_URL_WITH_SLASH = "http://api-akn.herokuapp.com/";
    public static final String BASE_URL = "http://api-akn.herokuapp.com";
    public static final String BASE_LOGO_URL = BASE_URL + "/resources/images/logo/";
*/
    //public static final String BASE_URL_WITH_SLASH = "http://192.168.178.144:8080/AKNnews/";

    /*public static final String BASE_URL_WITH_SLASH = "http://192.168.178.254:8080/AKNnews/";
    public static final String BASE_URL = "http://192.168.178.254:8080/AKNnews";
    public static final String BASE_LOGO_URL = BASE_URL + "/resources/images/logo/";*/

    public static final int MAX_TIME_OUT = 30000;
    public static final int DEFAULT_MAX_RETRIES = 3;

    // Add Request Headers
    public static HashMap<String, String> getHeaders() {
        HashMap<String, String> header = new HashMap<>();
        header.put(API_KEY, API_KEY_VALUE);
        return header;
    }

    public static RetryPolicy getRetryPolicy() {
        return new DefaultRetryPolicy(Util.MAX_TIME_OUT, Util.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    // Convert String  (Long Data type) to Date
    public static String convertToDate(String rawDate) {
        if (!rawDate.equals(null)) {
            long val = Long.parseLong(rawDate);
            Date date = new Date(val);
            SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
            return df2.format(date);
        }
        return "N/A";

    }

    // Convert long format to Date
    public static String convertToDate(long rawDate) {
        Date date=new Date(rawDate);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
        return df2.format(date);
    }

    // Generate Bitmap File from online url
    public static Bitmap getBitmapFromURL(String src) {
        try {
            Log.e("src",src);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.e("Bitmap","returned");
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception",e.getMessage());
            return null;
        }
    }

    /*imageView.post(new Runnable() { public void run() { imageView.setImageBitmap(result); } }); */

    // Show Popup SweetAlertDialog
    public static void showAlertDialog(Activity activity,String title, String msg,
                                       boolean showCancelbutton, int dialogType) {
        pDialog = new SweetAlertDialog(activity, dialogType);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(title);
        pDialog.setContentText(msg);
        pDialog.setCancelable(true);
        pDialog.showCancelButton(showCancelbutton);
        pDialog.show();
    }

    // Show Popup SweetAlertDialog
    public static void showAlertDialog(Activity activity, String msg,
                                       boolean showCancelbutton, int dialogType) {
        pDialog = new SweetAlertDialog(activity, dialogType);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(msg);
        pDialog.setCancelable(true);
        pDialog.showCancelButton(showCancelbutton);
        pDialog.show();
    }

    // This dialog is for sign up success in SignUpActivity
    public static void showAlertDialogForSignUp(final Activity activity, String msg,
                                                boolean showCancelbutton, int dialogType) {
        pDialog = new SweetAlertDialog(activity, dialogType);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(msg);
        pDialog.setCancelable(true);
        pDialog.showCancelButton(showCancelbutton);
        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
                activity.finish();
            }
        });
        pDialog.show();
    }


    // Close SweetAlertDialog
    public static void hideAlertDialog() {
        if (pDialog != null) {
            pDialog.cancel();
        }
    }

    public static void showUserConfirmDialog(final Activity activity, String title,
                                             String contentText,
                                             String cancelText, String confirmText,
                                             boolean showCancelButton) {
        pDialog = new SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE);
        pDialog.setTitle(title);
        pDialog.setContentText(contentText);
        pDialog.setCancelText(cancelText);
        pDialog.setConfirmText(confirmText);
        pDialog.showCancelButton(showCancelButton);
        pDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                pDialog.cancel();
            }
        });

        pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                Session.cleanSession();
                Intent intent = new Intent(activity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(intent);
                activity.finish();
            }
        });

    }

    // Show ProgressDialog
    public static void showProgressDialog(Activity activity, boolean cancelable) {
        proDialog = new ProgressDialog(activity, R.style.MyProgressDialogTheme);
        proDialog.setCancelable(cancelable);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
        proDialog.show();
    }

    // Close ProgressDialog
    public static void hideProgressDialog() {
        if (proDialog != null) {
            Log.d("testing_dialog", "dimiss");
            proDialog.dismiss();
        }
    }

    // Google Analytics
    public static void setDefaultTracker(Activity activity, String strDesc, String screenName) {
        if (appController == null) {
            appController = (AppController) activity.getApplication();
            Log.d("google_analytics", "appController first initialized.");
        }

        mTracker = appController.getDefaultTracker();

        mTracker.setScreenName(strDesc + ": " + screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        Log.i("google_analytics", strDesc + ": " + screenName);
    }



    public static boolean isConnectingToInternet(Context context){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= 23){
            if (connectivity != null)
            {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();

                if (info != null)
                    for (int i = 0; i < info.length; i++)
                        if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        {
                            return true;
                        }
            }
        }else{
            if (connectivity != null)
            {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();

                if (info != null)
                    for (int i = 0; i < info.length; i++)
                        if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        {
                            return true;
                        }

            }
        }

        return false;
    }

}
