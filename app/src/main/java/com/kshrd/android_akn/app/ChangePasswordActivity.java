package com.kshrd.android_akn.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.util.AKNNavigationView;
import com.kshrd.android_akn.util.Session;
import com.kshrd.android_akn.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ChangePasswordActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView oldPass, newPass, conPass;
    private Button btnChangePass;
    private ProgressDialog proDialog;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if user changed language
        SharedPreferences sharedPref = getSharedPreferences("setting",
                Context.MODE_PRIVATE);
        Locale locale = null;
        Configuration config = null;

        if (locale == null) locale = new Locale(sharedPref.getString("LANGUAGE", "en"));
        Locale.setDefault(locale);
        if (config == null) config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());

        setContentView(R.layout.drawer_layout);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_change_password, frameLayout);
        AKNNavigationView aknNavigationView = new AKNNavigationView(this);

        // Init Progress Dialog
        proDialog = new ProgressDialog(this, R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        // Google Analytics
        //Util.setDefaultTracker(this, "AKN, Screen Name", "ChangePasswordActivity");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name ChangePasswordActivity");
        mTracker.setScreenName("AKN, Screen Name ChangePasswordActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        // Set up Toolbar
        mToolbar = (Toolbar)findViewById(R.id.change_password_toolbar);
        mToolbar.setTitle(R.string.chang_password);
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        oldPass = (TextView) findViewById(R.id.edit_old_pass);
        newPass = (TextView) findViewById(R.id.edit_new_pass);
        conPass = (TextView) findViewById(R.id.edit_confirm_pass);
        btnChangePass = (Button)findViewById(R.id.btn_change);
        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPass.getText().toString();
                String newPassword = newPass.getText().toString();
                String conPassword = conPass.getText().toString();

                // Read User Data from SharedPreferences
                Session.readUserSession(ChangePasswordActivity.this);
               // int userId = Session.USER_ID;
                String userEnId = Session.ENCRYPTED_USER_ID;   // = "OTA4Nw==";
                // Verify New Password and Confirm Password
                if(newPassword.equals(conPassword)) {
                    requestResponse(userEnId, newPassword, oldPassword);
                }else{
                    //Toast.makeText(ChangePasswordActivity.this, getResources().getString(R.string.new_and_con_password_not_match), Toast.LENGTH_SHORT).show();
                    Util.showAlertDialog(ChangePasswordActivity.this, "Alert", getString(R.string.new_and_con_password_not_match),
                            true, SweetAlertDialog.WARNING_TYPE);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Util.setDefaultTracker(this, "AKN, Screen Name", "ChangePasswordActivity");
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name ChangePasswordActivity");
        mTracker.setScreenName("AKN, Screen Name ChangePasswordActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    // Change User Password
    public void requestResponse(String userId,String newPassword,String oldPassword){
        proDialog.show();
        String url = "http://api.khmeracademy.org/api/user/changepassword";
        JSONObject object = new JSONObject();
        try {
            object.put("userId",userId);
            object.put("newPassword",newPassword);
            object.put("oldPassword",oldPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                boolean status = false;
                try {
                    status = response.getBoolean("STATUS");
                    if( status) {
                        Util.showAlertDialog(ChangePasswordActivity.this,
                                getString(R.string.success_change_password), true, SweetAlertDialog.SUCCESS_TYPE);
                    }else{
                        Util.showAlertDialog(ChangePasswordActivity.this,
                                getString(R.string.failed_change_password), true, SweetAlertDialog.WARNING_TYPE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (proDialog != null) proDialog.dismiss();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                header.put("Authorization","Basic S0FBUEkhQCMkOiFAIyRLQUFQSQ==");
                return header;
            }
        };
        // Add Request to Request Queue
        AppController.getInstance().addToRequestQueue(request);
    }
}
