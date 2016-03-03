package com.kshrd.android_akn.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.kshrd.android_akn.R;
import com.kshrd.android_akn.util.AKNNavigationView;
import com.kshrd.android_akn.util.Util;

import java.util.Locale;

public class AboutUsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private AKNNavigationView aknNavigationView;
    private TextView linkNews;
    private Button btnAKNTeam;
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
        getLayoutInflater().inflate(R.layout.activity_about_us, frameLayout);

        aknNavigationView = new AKNNavigationView(this);

        // Set up Toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle(getString(R.string.about_us_title));
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        linkNews = (TextView) findViewById(R.id.link_news);
        linkNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uriUrl = Uri.parse("http://news.khmeracademy.org/");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });

        btnAKNTeam = (Button) findViewById(R.id.btnAKNTeam);
        btnAKNTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AboutUsActivity.this);
                builder.setTitle("Android AKN Team");
                builder.setView(R.layout.about_akn_team);
                builder.setCancelable(true);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.setDefaultTracker(this, "AKN, Screen Name", "AboutUsActivity");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
