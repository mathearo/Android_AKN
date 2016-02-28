package com.kshrd.android_akn.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

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
import com.kshrd.android_akn.util.Setting;
import com.kshrd.android_akn.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

public class ListByCategoryActivity extends AppCompatActivity {
    // For Google Analytics
    private Tracker mTracker;

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private Article mArticle;
    private List<Article> mArticles = new ArrayList<>();
    private ArticleAdapter mAdapter;
    /*private ParallaxArticleAdapter mAdapter;*/

    private SearchView mSearchView;
    private SearchView.OnQueryTextListener mSearchViewListener;
    private MenuItem mMenuItem;
    private ProgressDialog proDialog;
    private ScaleInAnimationAdapter scaleInAnimationAdapter;
    private int categoryId = 0;
    private int userId;

    private boolean mLoading = false;
    private boolean flag = false;
    private ImageButton btnScrollUp;

    // Pagination
    int page = 1;
    int row = 15;
    int totalPages;
    int totalRecords;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_list_by_category, frameLayout);
        AKNNavigationView aknNavigationView = new AKNNavigationView(this);

        // Init Progress Dialog
        proDialog = new ProgressDialog(this, R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        Session.readUserSession(this);
        userId = Session.USER_ID;

        // For Google Analytics
        //Util.setDefaultTracker(this,"AKN, Screen Name", "ListByCategoryActivity");
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name ListByCategoryActivity");
        mTracker.setScreenName("AKN, Screen Name ListByCategoryActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        // Set up Toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle(getIntent().getStringExtra("CATEGORY_NAME"));
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get Category ID from Intent
        categoryId = getIntent().getExtras().getInt("CATEGORY_ID", 0);

        //mAdapter.setParallaxHeader(null, null);

        // Handle SearchView
        mSearchViewListener = new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {

                Intent intent  = new Intent(getBaseContext(), SearchActivity.class);
                intent.putExtra("KEY_WORD", query);
                intent.putExtra("CATEGORY_ID", categoryId);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };



        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mArticles.clear();
                scaleInAnimationAdapter.notifyDataSetChanged();
                page = 1;
                requestResponse();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mAdapter = new ArticleAdapter(mArticles);
        mAdapter.setHasStableIds(true);
        scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
        //mAdapter = new ParallaxArticleAdapter(mArticles);

        mRecyclerView = (RecyclerView)findViewById(R.id.list_by_category_new);
        mLayoutManager = new LinearLayoutManager(ListByCategoryActivity.this){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Optimize RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(scaleInAnimationAdapter);
        // Check Enable or Disable Scroll Animation
        setScrollAnimation();

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
                int totalItem = mLayoutManager.getItemCount();
                int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();

                // Enable or Disable Scroll Up Button
                if (dy > 0) {
                    btnScrollUp.setVisibility(View.VISIBLE);
                } else {
                    btnScrollUp.setVisibility(View.GONE);
                }

                if (!mLoading && lastVisibleItem == totalItem - 1) {
                    mLoading = true;
                    // Scrolled to bottom. Do something here.
                    //Toast.makeText(ListByCategoryActivity.this, "olo", Toast.LENGTH_SHORT).show();
                    loadMoreArticles();
                    mLoading = false;
                }
            }
        });

        // List Article by category
        requestResponse();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        mMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) mMenuItem.getActionView();
        // Set White Color to SearchView Hint
        EditText searchViewEditText = ((EditText) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        searchViewEditText.setHint(getString(R.string.search_hint));
        searchViewEditText.setHintTextColor(Color.WHITE);
        searchViewEditText.setTextColor(Color.WHITE);
        // Make SearchView stretch its parent
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        // Disable Fullscreen editing mode when enters text to SearchView
        //searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);

        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        mSearchView.setOnQueryTextListener(mSearchViewListener);
        return true;
    }

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
        //Util.setDefaultTracker(this,"AKN, Screen Name", "ListByCategoryActivity");
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name ListByCategoryActivity");
        mTracker.setScreenName("AKN, Screen Name ListByCategoryActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    // List Articles by Category
    public void requestResponse(){
        proDialog.show();
        String url = Util.BASE_URL + "/api/article/"+ page +"/"+ row +"/" + categoryId + "/0/"+ userId +"/";

        AKNStringRequest request = new AKNStringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {

                    // For Pagination
                    JSONObject obj = new JSONObject(response);
                    totalPages = obj.getInt("TOTAL_PAGES");
                    totalRecords = obj.getInt("TOTAL_RECORDS");

                    JSONArray articleList = new JSONObject(response).getJSONArray("RESPONSE_DATA");

                    for(int i=0;i<articleList.length();i++){
                        JSONObject jarticle = articleList.getJSONObject(i);
                        Article article = new Article();
                        article.setId(jarticle.getInt("id"));
                        article.setTitle(jarticle.getString("title"));
                        article.setDescription(jarticle.getString("description"));
                        article.setDate(jarticle.getString("date"));
                        article.setImageUrl(jarticle.getString("image"));
                        //article.setUrl( Util.BASE_URL_WITH_SLASH + jarticle.getJSONObject("site").getString("logo"));
                        article.setViewCount(jarticle.getInt("hit"));
                        //article.setStatus(jarticle.getBoolean("status"));
                        article.setSaved(jarticle.getBoolean("saved"));
                        article.setContent(jarticle.getString("content"));
                        article.setUrl(jarticle.getString("url"));
                        article.setSiteLogo(jarticle.getJSONObject("site").getString("logo"));
                        mArticles.add(article);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    /*scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
                    scaleInAnimationAdapter.setDuration(300);*/
                    scaleInAnimationAdapter.notifyDataSetChanged();
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

        // Add Request to Request Queue
        AppController.getInstance().addToRequestQueue(request);
    }

    public void loadMoreArticles() {
        if (page < totalPages) {
            page++;
            requestResponse();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}