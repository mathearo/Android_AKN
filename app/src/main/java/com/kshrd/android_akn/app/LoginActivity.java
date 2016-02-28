package com.kshrd.android_akn.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.util.Session;
import com.kshrd.android_akn.util.Util;
import com.kshrd.android_akn.util.Validation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import eu.inmite.android.lib.validations.form.annotations.MinLength;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.annotations.RegExp;

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin;
    private TextView tvSignUp, tvForgetPassword;
    private ProgressDialog proDialog;
    private Tracker mTracker;

    @NotEmpty(messageId = R.string.validation_not_empty, order = 1)
    @RegExp(value = Validation.EMAIL_PATTERN, messageId = R.string.validation_valid_email,order = 2)
    private EditText etEmail;

    @NotEmpty(messageId = R.string.validation_not_empty, order = 1)
    @MinLength(value = 5, messageId = R.string.validation_pass_min_length, order = 2)
    private EditText etPassword;

    private Intent intent;

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

        setContentView(R.layout.activity_login);

        // Google Analytics
        //Util.setDefaultTracker(this, "AKN, Screen Name", "LoginActivity");
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name LoginActivity");
        mTracker.setScreenName("AKN, Screen Name LoginActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        initWidget();

    }

    // Initialize Widget (View Object)
    public void initWidget() {
        btnLogin = (Button) findViewById(R.id.btn_login);
        tvSignUp = (TextView) findViewById(R.id.tv_signup);
        //tvForgetPassword = (TextView) findViewById(R.id.tv_forget_password);
        etPassword = (EditText) findViewById(R.id.et_password_login);
        etEmail = (EditText) findViewById(R.id.et_email_login);

        // Init Progress Dialog
        proDialog = new ProgressDialog(this, R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        // Set Event
        initEvent();
    }

    // Add Event to Widget
    public void initEvent() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Util.setDefaultTracker(this,"AKN, Screen Name", "LoginActivity");
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name LoginActivity");
        mTracker.setScreenName("AKN, Screen Name LoginActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    // Login
    public void login() {
        // Show Progress Dialog
        proDialog.show();

        String url = Util.BASE_URL + "/api/user/login";
        JSONObject param;
        try {
            param = new JSONObject();
            param.put("email", etEmail.getText().toString().trim());
            param.put("password", etPassword.getText().toString().trim());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, param,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Toast.makeText(LoginActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                            try {
                                // 302 means SUCCESS
                                if (response.getInt("STATUS") == 302) {

                                    JSONObject jsonObject = response.getJSONObject("DATA");

                                    // Save Session in SharedPreferences
                                    Session.saveUserSession(LoginActivity.this,
                                            jsonObject.getInt("id"),
                                            jsonObject.getString("enid"),
                                            jsonObject.getString("username"),
                                            jsonObject.getString("email"),
                                            jsonObject.getString("image"));

                                    if (proDialog != null) proDialog.dismiss();
                                    intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    if (proDialog != null) proDialog.dismiss();
                                    Util.showAlertDialog(LoginActivity.this, getString(R.string.login_failed),
                                            getString(R.string.email_password_not_correct), true, SweetAlertDialog.WARNING_TYPE);
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
                    if (proDialog != null) proDialog.dismiss();
                    Util.showAlertDialog(LoginActivity.this, "Login failed",
                            "Network Error", true, SweetAlertDialog.ERROR_TYPE);
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return Util.getHeaders();
                }

                @Override
                public RetryPolicy getRetryPolicy() {
                    return Util.getRetryPolicy();
                }
            };

            // Add to request queue
            AppController.getInstance().addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        } /*finally {
            if (proDialog != null) proDialog.dismiss();
        }*/
    }

}
