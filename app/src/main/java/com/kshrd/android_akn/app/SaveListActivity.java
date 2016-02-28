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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.adapter.ArticleAdapter;
import com.kshrd.android_akn.model.Article;
import com.kshrd.android_akn.util.AKNNavigationView;
import com.kshrd.android_akn.util.AKNStringRequest;
import com.kshrd.android_akn.util.Session;
import com.kshrd.android_akn.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

public class SaveListActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArticleAdapter mAdapter;
    /*private ParallaxArticleAdapter mAdapter;*/
    private List<Article> mArticles = new ArrayList<>();
    private ProgressDialog proDialog;
    private LinearLayoutManager mLinearLayoutManager;
    private LinearLayout layoutSavedListsEmpty;
    private ScaleInAnimationAdapter scaleInAnimationAdapter;
    private Tracker mTracker;
    private ImageButton btnScrollUp;

    private boolean mLoading = false;
    private boolean isResponseDataNull = false;

    // Pagination
    int page = 1;
    int row = 5;

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

        // Init Progress Dialog
        proDialog = new ProgressDialog(this, R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        // Google Analytics
        //Util.setDefaultTracker(this, "AKN, Screen Name", "SaveListActivity");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name SaveListActivity");
        mTracker.setScreenName("AKN, Screen Name SaveListActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_save_list, frameLayout);

        AKNNavigationView aknNavigationView = new AKNNavigationView(SaveListActivity.this);

        mToolbar = (Toolbar)findViewById(R.id.savelist_toolbar);
        mToolbar.setTitle(R.string.save_list);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        layoutSavedListsEmpty = (LinearLayout) findViewById(R.id.layout_saved_lists_empty);
        mRecyclerView = (RecyclerView)findViewById(R.id.list_save_list);

        /*mAdapter = new ParallaxArticleAdapter(mArticles);*/
        mAdapter = new ArticleAdapter(mArticles);
        /*scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
        scaleInAnimationAdapter.setDuration(300);*/

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        /*mRecyclerView.setAdapter(scaleInAnimationAdapter);*/
        mRecyclerView.setAdapter(mAdapter);

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
                int totalItem = mLinearLayoutManager.getItemCount();
                int lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();

                // Enable or Disable Scroll Up Button
                if (dy > 0 ) {
                    btnScrollUp.setVisibility(View.VISIBLE);
                } else {
                    btnScrollUp.setVisibility(View.GONE);
                }

                if (!mLoading && lastVisibleItem == totalItem - 1) {
                    mLoading = true;
                    // Scrolled to bottom. Do something here.
                    //Toast.makeText(ListByCategoryActivity.this, "olo", Toast.LENGTH_SHORT).show();
                    loadMore();
                    mLoading = false;
                }
            }
        });

        loadSavedList();
    }

    public void loadMore(){
        if (!isResponseDataNull) {
            page++;
            loadSavedList();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Util.setDefaultTracker(this, "AKN, Screen Name", "ProfileActivity");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name SaveListActivity");
        mTracker.setScreenName("AKN, Screen Name SaveListActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void loadSavedList(){
        //Toast.makeText(SaveListActivity.this, "LoadSavedList", Toast.LENGTH_SHORT).show();
        proDialog.show();
        Session.readUserSession(SaveListActivity.this);
        int userId = Session.USER_ID;
        //GET /api/article/savelist/{userid}/{row}/{page}/{day} ; day = 0 means list all news
        String url = Util.BASE_URL+"/api/article/savelist/" + userId + "/" + row + "/" + page + "/0";
        //String url = "http://192.168.178.254:8080/AKNnews/api/article/savelist/9089/5/1/0";
        AKNStringRequest request = new AKNStringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //Toast.makeText(SaveListActivity.this, response, Toast.LENGTH_SHORT).show();
                    
                    JSONArray articleList = new JSONObject(response).getJSONArray("RESPONSE_DATA");

                    if (articleList.length() == 0) {
                        isResponseDataNull = true;
                        if (proDialog != null) proDialog.dismiss();

                        if (mArticles.size() == 0) {
                            mRecyclerView.setVisibility(View.GONE);
                            layoutSavedListsEmpty.setVisibility(View.VISIBLE);
                        }

                        return;
                    }

                    mRecyclerView.setVisibility(View.VISIBLE);
                    layoutSavedListsEmpty.setVisibility(View.GONE);
                    isResponseDataNull = false;

                    for(int i=0;i<articleList.length();i++){
                        JSONObject jarticle = new JSONObject();
                        jarticle = articleList.getJSONObject(i);
                        Article article = new Article();
                        article.setId(jarticle.getInt("id"));
                        article.setTitle(jarticle.getString("title"));
                        article.setDate(jarticle.getString("date"));
                        article.setImageUrl(jarticle.getString("image"));
                        //article.setUrl( Util.BASE_URL_WITH_SLASH + jarticle.getJSONObject("site").getString("logo"));
                        article.setViewCount(jarticle.getInt("hit"));
                        article.setUrl(jarticle.getString("url"));
                        article.setSiteLogo(jarticle.getJSONObject("site").getString("logo"));

                        // Check if we have redundant item in list.
                        // Must heavy if list gets big size
                        if (mArticles.contains(article)) {
                            Log.d("test_saved_list", "true, redundant");
                           continue;
                        }
                        mArticles.add(article);
                    }

                    /*if (mArticles.size() <= 0) {
                        mRecyclerView.setVisibility(View.GONE);
                        layoutSavedListsEmpty.setVisibility(View.VISIBLE);
                    } else {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        layoutSavedListsEmpty.setVisibility(View.GONE);
                    }*/

                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    //Toast.makeText(SaveListActivity.this, "do notify", Toast.LENGTH_SHORT).show();
                    /*if (mArticles.size() > 0) scaleInAnimationAdapter.notifyDataSetChanged();*/
                    if (mArticles.size() > 0) mAdapter.notifyDataSetChanged();
                    if (proDialog != null) proDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (proDialog != null) proDialog.dismiss();
                error.printStackTrace();
            }
        });
        AppController.getInstance().addToRequestQueue(request);
    }

}
