package com.kshrd.android_akn.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.util.AKNNavigationView;
import com.kshrd.android_akn.util.AKNStringRequest;
import com.kshrd.android_akn.util.Session;
import com.kshrd.android_akn.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ArticleDetailActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private TextView title,date,view, document_content;
    private NetworkImageView thumnail, logo;
    private CollapsingToolbarLayout collapsingToolbar;
    //private CoordinatorLayout coordinatorLayout;
    private LinearLayout layoutCannotLoadContent, layoutContent;
    private CoordinatorLayout coordinatorLayout;
    private AKNNavigationView aknNavigationView;
    private Menu mMenu;
    private MenuItem mMenuItemSave;
    private boolean isSaved;

    private Tracker mTracker;
    private ProgressDialog proDialog;
    private boolean isArticleLoaded = false;
    private int i;
    private int intScaleType = 0;
    private Typeface tf;

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
        //getLayoutInflater().inflate(R.layout.activity_article_detail, frameLayout);
        getLayoutInflater().inflate(R.layout.activity_article_detail, frameLayout);
        aknNavigationView = new AKNNavigationView(this);

        // Init Progress Dialog
        proDialog = new ProgressDialog(this, R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        // Google Analytics
        //Util.setDefaultTracker(ArticleDetailActivity.this, "AKN, Screen Name", "ArticleDetailActivity");

        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name ArticleDetail");
        mTracker.setScreenName("AKN, Screen Name ArticleDetail");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        // Set up Toolbar
        //mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar = (Toolbar)findViewById(R.id.new_toolbar);
        //mToolbar.setTitle(getResources().getString(R.string.article_detail_activity));
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        //mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitleEnabled(false);

        layoutCannotLoadContent = (LinearLayout) findViewById(R.id.layout_cannot_load_content);
        layoutContent = (LinearLayout) findViewById(R.id.layout_content);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        // Get Font from assets
        tf = Typeface.createFromAsset(getAssets(), "fonts/Nokora-Regular.ttf");

        title =(TextView)findViewById(R.id.article_detail_title_new);
        title.setTypeface(tf);
        title.setText(getIntent().getStringExtra("TITLE"));
        document_content = (TextView) findViewById(R.id.document_content);
        document_content.setTypeface(tf);

        date = (TextView)findViewById(R.id.detail_date);
        date.setText(Util.convertToDate(getIntent().getStringExtra("DATE")));
        view = (TextView)findViewById(R.id.view_count);

        thumnail = (NetworkImageView) findViewById(R.id.thumnail);
        thumnail.setDefaultImageResId(R.drawable.no_image_available_big);
        thumnail.setImageUrl(getIntent().getStringExtra("THUMNAIL_URL"), AppController.getInstance().getImageLoader());
        thumnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intScaleType <3) intScaleType++;
                else intScaleType = 0;
                Log.d("test_img", "" + intScaleType);
                switch (intScaleType) {
                    case 0:
                        thumnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        break;
                    case 1:
                        thumnail.setScaleType(ImageView.ScaleType.FIT_XY);
                        break;
                    case 2:
                        thumnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        break;
                    case 3:
                        thumnail.setScaleType(ImageView.ScaleType.CENTER);
                        break;
                }

            }
        });
        logo = (NetworkImageView) findViewById(R.id.detail_source);
        logo.setDefaultImageResId(R.drawable.ic_action_globe);
        logo.setImageUrl(getIntent().getStringExtra("SOURCE_LOGO"), AppController.getInstance().getImageLoader());

        // Load Article
        loadArticleDetail();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_article_detail, menu);
        mMenuItemSave = menu.findItem(R.id.action_save);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                saveArticle();
                break;
            case R.id.action_share:
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("text/plain");
                myIntent.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra("URL"));
                myIntent.putExtra(Intent.EXTRA_SUBJECT,"Check this link out!");
                startActivity(Intent.createChooser(myIntent, getString(R.string.share_via)));
                break;
            case R.id.action_open_in_browser:
                if (getIntent().getStringExtra("URL") != null) {
                    Uri uriUrl = Uri.parse(getIntent().getStringExtra("URL"));
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                } else {
                    Util.showAlertDialog(this, "URL not found.", true, SweetAlertDialog.WARNING_TYPE);
                }
                break;
        }

        return true;
    }

    // Save Article
    public void saveArticle() {
        Session.readUserSession(ArticleDetailActivity.this);
        //int userId = 18;
        int userId = Session.USER_ID;
        if(userId != 0){
            int newsId = getIntent().getIntExtra("ARTICLE_ID", 0);
            String url = Util.BASE_URL +"/api/article/savelist";
            JSONObject object = new JSONObject();

            try {
                object.put("newsid", newsId);
                object.put("userid", userId);
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,object, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Util.showAlertDialog(ArticleDetailActivity.this,
                                getString(R.string.added_to_saved_lists), true, SweetAlertDialog.SUCCESS_TYPE);
                        mMenuItemSave.setVisible(false);
                    }
                } ,new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return Util.getHeaders();
                    }
                };
                AppController.getInstance().addToRequestQueue(request);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Util.showAlertDialog(this, getString(R.string.please_login_to_save), true, SweetAlertDialog.WARNING_TYPE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Util.setDefaultTracker(this, "AKN, Screen Name", "ArticleDetailActivity");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name ArticleDetail");
        mTracker.setScreenName("AKN, Screen Name ArticleDetail");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    // Load Article base on ID
    public void loadArticleDetail(){
        proDialog.show();
        int articleID = getIntent().getIntExtra("ARTICLE_ID", 0);

        // Read User Data from SharedPreferences
        Session.readUserSession(ArticleDetailActivity.this);
        int userID = Session.USER_ID;
        String url = Util.BASE_URL + "/api/article/" + articleID + "/" + userID;
        AKNStringRequest request = new AKNStringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                isArticleLoaded = true;
                Log.d("detail_response",response);
                JSONObject object;
                try {
                    object = new JSONObject(response).getJSONObject("RESPONSE_DATA");

                    /*layoutCannotLoadContent.setVisibility(View.GONE);
                    layoutContent.setVisibility(View.VISIBLE);*/

                    // Prevent showing null article
                    //String strTitle = object.getString("title");
                    String strDocumentContent = object.getString("content");


                    //title.setText(strTitle);
                    //title.setText(getIntent().getStringExtra("TITLE"));
                    document_content.setText(strDocumentContent);

                    isSaved = object.getBoolean("saved");

                    // Disable Save Menu Item
                    if (isSaved) mMenuItemSave.setVisible(false);

                    //date.setText(Util.convertToDate(object.getString("date")));
                    //date.setText(Util.convertToDate(getIntent().getStringExtra("DATE")));
                    //thumnail.setImageUrl(object.getString("image"), AppController.getInstance().getImageLoader());
                    //thumnail.setImageUrl(getIntent().getStringExtra("THUMNAIL_URL"), AppController.getInstance().getImageLoader());
                    //logo.setImageUrl(getIntent().getStringExtra("SOURCE_LOGO"), AppController.getInstance().getImageLoader());
                    //logo.setImageUrl(Util.BASE_LOGO_URL + object.getJSONObject("site").getString("logo"), AppController.getInstance().getImageLoader());

                    String strViewCount;
                    if (object.getInt("hit") <= 0) {
                        strViewCount = "0 View";
                    } else strViewCount = object.getInt("hit") + " Views";
                    view.setText(strViewCount);
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
                Log.d("Error",error.toString());
            }
        });

        // Add Request to Request Queue
        AppController.getInstance().addToRequestQueue(request);

    }

    // Update Save Status in List
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
