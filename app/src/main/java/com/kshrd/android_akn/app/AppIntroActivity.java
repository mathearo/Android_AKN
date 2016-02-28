package com.kshrd.android_akn.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.util.Setting;
import com.kshrd.android_akn.util.Util;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Buth Mathearo on 12/28/2015.
 */
public class AppIntroActivity extends AppIntro2 {

    /**
     *  Do not override OnCreate(); The AppIntro Library will handle
     *  this.
      */
    @Override
    public void init(@Nullable Bundle bundle) {
        // Check if user just opened App for the first time.
        if (Setting.isFirstUsed(AppIntroActivity.this) == false) {
            openApp();
            return;
        }
        //Util.showAlertDialog(this, "Note", "This is my first release (first built). So if you see any bug, please let me know.", true, SweetAlertDialog.WARNING_TYPE);
        Util.showAlertDialog(this, "Note", "This is Second Built. Many bugs have been fixed.", true, SweetAlertDialog.WARNING_TYPE);
        //addSlide(AppIntroFragment.newInstance("AKN (All Khmer News)", "View all khmer news"));
        addSlide(SampleSlide.newInstance(R.layout.app_intro1));
        addSlide(SampleSlide.newInstance(R.layout.app_intro2));
        /*addSlide(AppIntroFragment.newInstance("AKN", "First", R.mipmap.ic_launcher, Color.MAGENTA));
        addSlide(AppIntroFragment.newInstance("AKN", "Second", R.mipmap.ic_launcher, Color.GRAY));*/
        setZoomAnimation();
        //setDepthAnimation();
    }

    public void openApp() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDonePressed() {
        Toast.makeText(AppIntroActivity.this, "Done, Hey.", Toast.LENGTH_SHORT).show();
        Setting.setAsNotFirstUsed(AppIntroActivity.this);
        openApp();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {

    }
}
