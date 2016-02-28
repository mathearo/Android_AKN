package com.kshrd.android_akn.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.adapter.ArticleAdapter;
import com.kshrd.android_akn.model.Article;
import com.kshrd.android_akn.util.AKNNavigationView;
import com.kshrd.android_akn.util.Session;
import com.kshrd.android_akn.util.Setting;
import com.kshrd.android_akn.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

public class SearchActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ArticleAdapter mAdapter;
    /*private ParallaxArticleAdapter mAdapter;*/
    private List<Article> mArticleList = new ArrayList<>();
    private Article mArticle;
    private boolean isLoading = false;
    private ProgressDialog proDialog;
    private Toolbar mToolbar;
    private ScaleInAnimationAdapter scaleInAnimationAdapter;
    private LinearLayout searchNotFoundLayout;
    private AKNNavigationView aknNavigationView;
    private Tracker mTracker;
    private ImageButton btnScrollUp;

    private int categoryId, sourceId;
    private String keyword;
    private int userId;

    private int page = 1;
    private int row = 20;
    private int totalPages;
    private int totalRecords;

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
        getLayoutInflater().inflate(R.layout.activity_search, frameLayout);
        aknNavigationView = new AKNNavigationView(this);

        Session.readUserSession(this);
        userId = Session.USER_ID;

        // Init Progress Dialog
        proDialog = new ProgressDialog(this, R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        // Google Analytics
        //Util.setDefaultTracker(this, "AKN, Screen Name", "SearchActivity");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name SearchActivity");
        mTracker.setScreenName("AKN, Screen Name SearchActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        mToolbar.setTitle(getResources().getString(R.string.search_activity));
        mToolbar.setTitleTextColor(Color.WHITE);
        //setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get Category ID and Source ID
        categoryId = getIntent().getExtras().getInt("CATEGORY_ID", 0);
        sourceId = getIntent().getExtras().getInt("SOURCE_ID", 0);
        keyword = getIntent().getStringExtra("KEY_WORD");

        searchNotFoundLayout = (LinearLayout) findViewById(R.id.layout_search_not_found);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_search_activity);
        /*mAdapter = new ParallaxArticleAdapter(mArticleList);*/
        mAdapter = new ArticleAdapter(mArticleList);
        scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
        scaleInAnimationAdapter.setDuration(300);
        mLinearLayoutManager = new LinearLayoutManager(getBaseContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(scaleInAnimationAdapter);

        // Check Enable or Disable Scroll Animation
        setScrollAnimation();
        //mRecyclerView.setAdapter(mAdapter);

        btnScrollUp = (ImageButton) findViewById(R.id.btnScrollUp);
        btnScrollUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                super.onScrolled(recyclerView, dx, dy);
                super.onScrolled(recyclerView, dx, dy);
                int totalItem = mLinearLayoutManager.getItemCount();
                int lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();

                // Enable or Disable Scroll Up Button
                if (dy > 0) {
                    btnScrollUp.setVisibility(View.VISIBLE);
                } else {
                    btnScrollUp.setVisibility(View.GONE);
                }

                if (!isLoading && lastVisibleItem == totalItem - 1) {
                    isLoading = true;
                    // Scrolled to bottom. Do something here.
                    //Toast.makeText(ListBySourceActivity.this, "olo", Toast.LENGTH_SHORT).show();
                    loadMoreArticles();
                    isLoading = false;
                }
            }
        });

        searchArticle();
    }

    public void loadMoreArticles() {
        if (page < totalPages) {
            page ++;
            searchArticle();
        }
    }

    // Check Enable or Disable Scroll Animation
    public void setScrollAnimation() {
        Setting.readSetting(this);
        if (!Setting.IS_ANIMATED) {
            if (scaleInAnimationAdapter != null) {
                scaleInAnimationAdapter.setDuration(0);
            } else {
                scaleInAnimationAdapter.setDuration(300);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Util.setDefaultTracker(this, "AKN, Screen Name", "ProfileActivity");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name SearchActivity");
        mTracker.setScreenName("AKN, Screen Name SearchActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void searchArticle() {
        proDialog.show();
        String url = Util.BASE_URL + "/api/article/search";
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("key", keyword);
            json.put("page", page);
            json.put("row", row);
            json.put("cid", categoryId);
            json.put("sid", sourceId);
            json.put("uid", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        //Toast.makeText(SearchActivity.this, "" + response, Toast.LENGTH_SHORT).show();
                        Log.d("rosearch", response.toString());

                        try {

                            // For Pagination
                            totalPages = response.getInt("TOTAL_PAGES");
                            totalRecords = response.getInt("TOTAL_RECORDS");

                            JSONArray jsonArray = response.getJSONArray("RESPONSE_DATA");
                            for(int i = 0; i  < jsonArray.length(); i++) {
                                mArticle = new Article();
                                mArticle.setId(jsonArray.getJSONObject(i).getInt("id"));
                                mArticle.setTitle(jsonArray.getJSONObject(i).getString("title"));
                                mArticle.setViewCount((jsonArray.getJSONObject(i).getInt("hit")));
                                mArticle.setDate(jsonArray.getJSONObject(i).getString("date"));
                                mArticle.setImageUrl(jsonArray.getJSONObject(i).getString("image"));
                                mArticle.setUrl(jsonArray.getJSONObject(i).getString("url"));
                                mArticle.setSiteLogo(jsonArray.getJSONObject(i).getJSONObject("site").getString("logo"));
                                mArticle.setSaved(jsonArray.getJSONObject(i).getBoolean("saved"));
                                mArticleList.add(mArticle);
                            }


                            if (mArticleList.size() <= 0) {
                                mRecyclerView.setVisibility(View.GONE);
                                searchNotFoundLayout.setVisibility(View.VISIBLE);
                                TextView tvSearchNotFound = (TextView) findViewById(R.id.tv_search_not_found);
                                tvSearchNotFound.setText(getString(R.string.no_result_for) + " \"" + keyword + "\".");
                            } else {
                                mRecyclerView.setVisibility(View.VISIBLE);
                                searchNotFoundLayout.setVisibility(View.GONE);
                            }

                        } catch(JSONException e) {
                            e.printStackTrace();
                        } finally {
                            scaleInAnimationAdapter.notifyDataSetChanged();

                            if (proDialog != null) proDialog.dismiss();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                if (proDialog != null) proDialog.dismiss();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return Util.getHeaders();
            }
        };

        AppController.getInstance().addToRequestQueue(request);
    }

}
