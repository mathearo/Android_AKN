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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kshrd.android_akn.R;
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

public class SignUpActivity extends AppCompatActivity {
    private Button btnSignUp;
    private Spinner mSpinnerGender;
    private ArrayAdapter<String> mAdapter;
    private ProgressDialog proDialog;

    private String gender[] = {"Male", "Female"};

    @NotEmpty(messageId = R.string.validation_not_empty, order = 1)
    @RegExp(value = Validation.EMAIL_PATTERN, messageId = R.string.validation_valid_email,order = 2)
    private EditText etUsername;

    @NotEmpty(messageId = R.string.validation_not_empty, order = 1)
    @MinLength(value = 5, messageId = R.string.validation_pass_min_length, order = 2)
    private EditText etPassword;

    @NotEmpty(messageId = R.string.validation_not_empty, order = 1)
    @RegExp(value = Validation.EMAIL_PATTERN, messageId = R.string.validation_valid_email,order = 2)
    private EditText etEmail;
    private Intent intent;

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

        setContentView(R.layout.activity_sign_up);

        // Init Progress Dialog
        proDialog = new ProgressDialog(this, R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        // Gender Spinner
        mSpinnerGender = (Spinner) findViewById(R.id.gender_spinner);
        gender[0] = getString(R.string.male);
        gender[1] = getString(R.string.female);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, gender);
        mSpinnerGender.setAdapter(mAdapter);

        // Google Analytics
        //Util.setDefaultTracker(this, "AKN, Screen Name", "SignUpActivity");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name SignUpActivity");
        mTracker.setScreenName("AKN, Screen Name SignUpActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        initWidget();
    }

    // Initialize all widgets here
    public void initWidget() {
        btnSignUp = (Button) findViewById(R.id.btn_sign_up);
        etUsername = (EditText) findViewById(R.id.et_username_sign_up);
        etPassword = (EditText) findViewById(R.id.et_password_sign_up);
        etEmail = (EditText) findViewById(R.id.et_email_sign_up);

        initEvent();
    }

    // Set events to all widgets here
    public void initEvent() {
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Util.setDefaultTracker(this, "AKN, Screen Name", "ProfileActivity");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name SignUpActivity");
        mTracker.setScreenName("AKN, Screen Name SignUpActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    // Sign up
    public void signUp() {
        proDialog.show();
        String url = Util.BASE_URL + "/api/user/";

        JSONObject param;
        /*{
          "username": "mathearo",
          "email": "mathearo.buth@gmail.com",
          "password": "kokimarket",
          "image": ""
        }*/
        try {
            param = new JSONObject();
            param.put("username", etUsername.getText().toString().trim());
            param.put("email", etEmail.getText().toString().trim());
            param.put("password", etPassword.getText().toString().trim());
            param.put("gender", mSpinnerGender.getSelectedItemPosition() == 0? "male":"female");

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, param,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getInt("STATUS") == 302) {
                                    Util.showAlertDialogForSignUp(SignUpActivity.this, getString(R.string.sign_up_success), true, SweetAlertDialog.SUCCESS_TYPE);
                                } else {
                                    Util.showAlertDialog(SignUpActivity.this,
                                            getString(R.string.sign_up_failed),
                                            getString(R.string.complete_all_fills), true, SweetAlertDialog.WARNING_TYPE);
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
                    Util.showAlertDialog(SignUpActivity.this, getString(R.string.sign_up_failed),
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
