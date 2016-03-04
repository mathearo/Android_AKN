package com.kshrd.android_akn.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.app.AboutUsActivity;
import com.kshrd.android_akn.app.AppController;
import com.kshrd.android_akn.app.LoginActivity;
import com.kshrd.android_akn.app.MainActivity;
import com.kshrd.android_akn.app.ProfileActivity;
import com.kshrd.android_akn.app.SaveListActivity;
import com.kshrd.android_akn.app.SettingDialogFragment;
import com.kshrd.android_akn.app.SignUpActivity;
import com.kshrd.android_akn.app.StatisticActivity;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Buth Mathearo on 1/21/2016.
 */
public class AKNNavigationView {
    private Activity mActivity;
    private String mCanonicalName = "com.kshrd.android_akn.app.MainActivity";
    private Intent mIntent;

    private FrameLayout contentFrame;

    private Button btnNavHome, btnNavSaveList, btnNavSetting, btnNavLogout, btnNavAboutUs,
            btnStatistic, btnSetting;
    private Button btnSignUp;
    private TextView tvLogin, tvNavProfileName, tvNavEmail;
    private ProgressBar progressSpinning;
    private CircularImageView imgProfilePic;
    private FragmentManager mFragmentManager;
    private SettingDialogFragment mSettingDialogFragment;

    private String cacheFile;

    public AKNNavigationView(Activity mActivity) {
        this.mActivity = mActivity;
        cacheFile =  mActivity.getCacheDir() + "/mathearo_avatar.png";
        // Read User Session. User must login first to create Session File.
        Session.readUserSession(mActivity);

        if (Session.IS_LOGIN) {
            contentFrame = (FrameLayout) mActivity.findViewById(R.id.content_frame_nav_login);
            contentFrame.setVisibility(View.VISIBLE);
            mActivity.getLayoutInflater().inflate(R.layout.navigation_drawer_login, contentFrame);
            initWidgetForNavLogin();
        } else {
            contentFrame = (FrameLayout) mActivity.findViewById(R.id.content_frame_nav_logout);
            contentFrame.setVisibility(View.VISIBLE);
            mActivity.getLayoutInflater().inflate(R.layout.navigation_drawer_log_out, contentFrame);
            initWidgetForNavLogout();
        }
    }

    public void initWidgetForNavLogin() {
        //btnNavHome, btnNavSaveList, btnNavSetting, btnNavLogout, btnNavAboutUs;
        btnNavHome = (Button) mActivity.findViewById(R.id.btn_nav_home);
        btnNavSaveList = (Button) mActivity.findViewById(R.id.btn_nav_saved_lists);
        btnNavSetting = (Button) mActivity.findViewById(R.id.btn_nav_setting);
        btnNavLogout = (Button) mActivity.findViewById(R.id.btn_nav_logout);
        btnNavAboutUs = (Button) mActivity.findViewById(R.id.btn_nav_about_us);

        btnStatistic = (Button) mActivity.findViewById(R.id.btn_nav_statistic);

        // Nav Header
        tvNavEmail = (TextView) mActivity.findViewById(R.id.nav_email);
        tvNavProfileName = (TextView) mActivity.findViewById(R.id.nav_profile_name);
        imgProfilePic = (CircularImageView) mActivity.findViewById(R.id.nav_profile_picture);

        progressSpinning = (ProgressBar) mActivity.findViewById(R.id.progressSpinning);

        initEventForNavLogin();

        Session.readUserSession(mActivity);

        /*if (!Session.PROFILE_IMAGE_URL.equals("user.jgp")) {
            updateProfileOnNavHeader();
        }*/

        updateProfileOnNavHeader();

    }

    public void initEventForNavLogin() {
        btnNavHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mActivity.getClass().getCanonicalName().equals(mCanonicalName)) {
                    mIntent = new Intent(mActivity, MainActivity.class);
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mActivity.startActivity(mIntent);
                    mActivity.finish();
                }
            }
        });

        btnNavSaveList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mActivity.getClass().equals(SaveListActivity.class)) {
                    mIntent = new Intent(mActivity, SaveListActivity.class);
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mActivity.startActivity(mIntent);
                }
            }
        });


        /*btnNavSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mActivity.getClass().equals(ProfileActivity.class)) {
                    mIntent = new Intent(mActivity, ProfileActivity.class);
                    mActivity.startActivity(mIntent);
                }
            }
        });*/

        btnNavSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentManager = mActivity.getFragmentManager();
                mSettingDialogFragment = new SettingDialogFragment();
                mSettingDialogFragment.show(mFragmentManager, "setting_dialog_fragment");
            }
        });

        btnNavLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Util.showUserConfirmDialog(mActivity, "Confirm", "Do you want to logout?",
                        "No", "Yes", true);*/
                /*Toast.makeText(mActivity, "Logout?", Toast.LENGTH_SHORT).show();*/
                mIntent = new Intent(mActivity, MainActivity.class);
                // Clear User Session
                Session.cleanSession();

                deleteUserProfileImageCacheFile();

                mActivity.startActivity(mIntent);
                mActivity.finish();
            }
        });

        btnNavAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mActivity, "Developed by AKN Team", Toast.LENGTH_SHORT).show();
                mIntent = new Intent(mActivity, AboutUsActivity.class);
                mActivity.startActivity(mIntent);
            }
        });

        // Nav Header
        imgProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent(mActivity, ProfileActivity.class);
                mActivity.startActivity(mIntent);
            }
        });

        btnStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(mActivity.getClass().equals(StatisticActivity.class))) {
                    Intent intent = new Intent(mActivity, StatisticActivity.class);
                    mActivity.startActivity(intent);
                }
            }
        });

    }

    // Update User Profile on Nav Header
    public void updateProfileOnNavHeader() {
        Session.readUserSession(mActivity);

        /*if (tvNavEmail == null || tvNavProfileName == null) {
            tvNavEmail = (TextView) mActivity.findViewById(R.id.nav_email);
            tvNavProfileName = (TextView) mActivity.findViewById(R.id.nav_profile_name);
        }*/

        tvNavEmail.setText(Session.EMAIL);
        tvNavProfileName.setText(Session.USER_NAME);

        String userimg = Util.BASE_PROFILE_IMAGE_URL + Session.PROFILE_IMAGE_URL;
        Log.d("nav_test", userimg);
        try {
            File f = new File(cacheFile);
            if (f.exists()) {
                Bitmap tmp = BitmapFactory.decodeFile(cacheFile);
                imgProfilePic.setImageBitmap(tmp);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        progressSpinning.setVisibility(View.VISIBLE);
        imageLoader.get(userimg, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    imgProfilePic.setImageBitmap(response.getBitmap());
                    progressSpinning.setVisibility(View.GONE);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
    }

    public void updateProfileOnNavHeader(Bitmap userProfileBitmap) {
        Session.readUserSession(mActivity);
        tvNavEmail.setText(Session.EMAIL);
        tvNavProfileName.setText(Session.USER_NAME);

        if (userProfileBitmap != null) {
            try {
                FileOutputStream out = new FileOutputStream(cacheFile);
                userProfileBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        imgProfilePic.setImageBitmap(userProfileBitmap);

    }

    public void deleteUserProfileImageCacheFile() {
        try {
            File f = new File(cacheFile);
            if (f.exists()) {
                f.delete();
                Log.d("test", "Delete User Profile Image Cache File");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initWidgetForNavLogout() {
        btnSignUp = (Button) mActivity.findViewById(R.id.btn_sign_up_in_logout_form);
        tvLogin = (TextView) mActivity.findViewById(R.id.tv_login);
        btnStatistic = (Button) mActivity.findViewById(R.id.btnStatistic);
        btnSetting = (Button) mActivity.findViewById(R.id.btn_nav_setting);
        initEventForNavLogout();
    }

    public void initEventForNavLogout() {
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent(mActivity, SignUpActivity.class);
                mActivity.startActivity(mIntent);
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent = new Intent(mActivity, LoginActivity.class);
                mActivity.startActivity(mIntent);
            }
        });

        btnStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(mActivity.getClass().equals(StatisticActivity.class))) {
                    Intent intent = new Intent(mActivity, StatisticActivity.class);
                    mActivity.startActivity(intent);
                }
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFragmentManager = mActivity.getFragmentManager();
                mSettingDialogFragment = new SettingDialogFragment();
                mSettingDialogFragment.show(mFragmentManager, "setting_dialog_fragment");
            }
        });
    }

}
