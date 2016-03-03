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
import android.widget.Toast;

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

public class ListBySourceActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private SearchView.OnQueryTextListener mSearchViewListener;
    private MenuItem mMenuItem;
    private int sourceId;
    private List<Article> mArticleList = new ArrayList<>();
    private ArticleAdapter mAdapter;
    /*private ParallaxArticleAdapter mAdapter;*/
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isLoading = false;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressDialog proDialog;
    private ScaleInAnimationAdapter scaleInAnimationAdapter;
    private Tracker mTracker;
    private ImageButton btnScrollUp;

    private int userId;

    // Pagination
    private int page = 1;
    private int row = 15;
    int totalPages;
    int totalRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_list_by_source, frameLayout);
        AKNNavigationView aknNavigationView = new AKNNavigationView(this);

        // Init Progress Dialog
        proDialog = new ProgressDialog(this, R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        Session.readUserSession(this);
        userId = Session.USER_ID;

        // Google Analytics
        //Util.setDefaultTracker(this, "AKN, Screen Name", "ListBySourceActivity");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name ListBySourceActivity");
        mTracker.setScreenName("AKN, Screen Name ListBySourceActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        // Set up Toolbar
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mToolbar.setTitle(getIntent().getStringExtra("SOURCE_NAME")); //getIntent().getStringExtra("SOURCE_NAME")
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sourceId = getIntent().getIntExtra("SOURCE_ID", 0);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setEnabled(false);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mArticleList.clear();
                //mAdapter.notifyDataSetChanged();
                scaleInAnimationAdapter.notifyDataSetChanged();
                page = 1;
                listArticleBySource();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mRecyclerView = (RecyclerView)findViewById(R.id.list_by_source_activity);

        mLinearLayoutManager  = new LinearLayoutManager(ListBySourceActivity.this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        // Optimize RecyclerView
        mRecyclerView.setHasFixedSize(true);
        /*mAdapter = new ParallaxArticleAdapter(mArticleList);*/
        mAdapter = new ArticleAdapter(mArticleList);
        scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
        scaleInAnimationAdapter.setDuration(300);
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

        listArticleBySource();

        // Handle SearchView
        mSearchViewListener = new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {

                Intent intent  = new Intent(getBaseContext(), SearchActivity.class);
                intent.putExtra("KEY_WORD", query);
                intent.putExtra("SOURCE_ID", sourceId);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };

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

        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        // Disable Fullscreen editing mode when enters text to SearchView
        //searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);

        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI|EditorInfo.IME_ACTION_SEARCH);

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
        //Util.setDefaultTracker(this,"AKN, Screen Name", "ListBySourceActivity");
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name ListBySourceActivity");
        mTracker.setScreenName("AKN, Screen Name ListBySourceActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void loadMoreArticles() {
        if (page < totalPages) {
            page++;
            listArticleBySource();
        }
    }

    // List All Article By Specific Source
    public void listArticleBySource(){
        proDialog.show();
        String url = Util.BASE_URL + "/api/article/"+ page +"/"+ row +"/0/" + sourceId + "/"+ userId +"/";

        AKNStringRequest request = new AKNStringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    mSwipeRefreshLayout.setEnabled(true);
                    // For Pagination
                    JSONObject obj = new JSONObject(response);
                    totalPages = obj.getInt("TOTAL_PAGES");
                    totalRecords = obj.getInt("TOTAL_RECORDS");

                    JSONArray jsonArray = new JSONObject(response).getJSONArray("RESPONSE_DATA");

                    for(int i=0; i < jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        Article article = new Article();

                        article.setId(jsonObject.getInt("id"));
                        article.setTitle(jsonObject.getString("title"));
                        article.setDescription(jsonObject.getString("description"));
                        article.setDate(jsonObject.getString("date"));
                        article.setImageUrl(jsonObject.getString("image"));
                        article.setUrl( Util.BASE_URL_WITH_SLASH + jsonObject.getJSONObject("site").getString("logo"));
                        article.setViewCount(jsonObject.getInt("hit"));
                        //article.setStatus(jsonObject.getBoolean("status"));
                        article.setSaved(jsonObject.getBoolean("saved"));
                        article.setContent(jsonObject.getString("content"));
                        article.setUrl(jsonObject.getString("url"));
                        Log.d("test_url", jsonObject.getString("url"));
                        article.setSiteLogo(jsonObject.getJSONObject("site").getString("logo"));
                        mArticleList.add(article);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }finally {
                    /*scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
                    scaleInAnimationAdapter.setDuration(300);
                    mRecyclerView.setAdapter(scaleInAnimationAdapter);*/
                    scaleInAnimationAdapter.notifyDataSetChanged();

                    if (proDialog != null) proDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (proDialog != null) proDialog.dismiss();
                Toast.makeText(ListBySourceActivity.this, "ERR: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Add Request to Request Queue
        AppController.getInstance().addToRequestQueue(request);
    }

}
