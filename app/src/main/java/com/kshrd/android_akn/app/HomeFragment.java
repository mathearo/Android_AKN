package com.kshrd.android_akn.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.adapter.ParallaxArticleAdapter;
import com.kshrd.android_akn.model.Article;
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

/**
 * Created by Buth Mathearo on 12/31/2015.
 */
public class HomeFragment extends Fragment implements Communication {
    private MySlider slide;
    private SliderLayout mSliderLayout;
    private RecyclerView mRecyclerView;

    private Article mArticle;
    private List<Article> mArticles = new ArrayList<>();
    //private ArticleAdapter mAdapter;
    private ParallaxArticleAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLinearLayoutManager;
    private Tracker mTracker;

    private boolean isLoading = false;

    private ProgressDialog proDialog;
    private ScaleInAnimationAdapter scaleInAnimationAdapter;

    // Pagination
    private int page = 1;
    private int row = 15;
    private int totalPages = 0, totalRecords = 0;

    private ImageButton btnScrollUp;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Init Progress Dialog
        proDialog = new ProgressDialog(getActivity(), R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        //Util.setDefaultTracker(getActivity(), "AKN, Screen Name", "Tab Name: HOME");

        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name Tab Name: HOME");
        mTracker.setScreenName("AKN, Screen Name Tab Name: HOME");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        btnScrollUp = (ImageButton) view.findViewById(R.id.btnScrollUp);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        //mSwipeRefreshLayout.setEnabled(false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        // Optimize RecyclerView speed
        mRecyclerView.setHasFixedSize(true);

        btnScrollUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mLinearLayoutManager.scrollToPosition(0);
                mRecyclerView.smoothScrollToPosition(0);
                //Toast.makeText(getActivity(), "Clicked.", Toast.LENGTH_SHORT).show();
            }
        });

        mAdapter = new ParallaxArticleAdapter(mArticles);
        View myHeader = getLayoutInflater(savedInstanceState).inflate(R.layout.recycler_view_header, mRecyclerView, false);
        //mAdapter.setParallaxHeader(inflater.inflate( R.layout.recycler_view_header, mRecyclerView, false), mRecyclerView);
        mAdapter.setParallaxHeader(myHeader, mRecyclerView);
        scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
        //scaleInAnimationAdapter.setDuration(300);
        mRecyclerView.setAdapter(scaleInAnimationAdapter);
        //mRecyclerView.setAdapter(mAdapter);

        setScrollAnimation();

        mSliderLayout = (SliderLayout) myHeader.findViewById(R.id.slider_layout);

        initEvent();

        // First Load Popular Articles to ImageSlider
        loadPopularArticles();

        // Loading Articles into RecyclerView
        loadArticles();

        return view;
    }

    public void setScrollAnimation() {
        Setting.readSetting(getActivity());
        if (!Setting.IS_ANIMATED) {
            if (scaleInAnimationAdapter != null) {
                scaleInAnimationAdapter.setDuration(0);
            } else {
                scaleInAnimationAdapter.setDuration(300);
            }
        }
    }

    public void initEvent() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSliderLayout.removeAllSliders();
                loadPopularArticles();
                mArticles.clear();
                //mAdapter.notifyDataSetChanged();
                scaleInAnimationAdapter.notifyDataSetChanged();
                page = 1;
                loadArticles();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItem = mLinearLayoutManager.getItemCount();
                int lastVisibleItem = mLinearLayoutManager.findLastVisibleItemPosition();

                Log.d("scroll_test", "x: " + dx + ", y: " + dy);

                if ( dy > 0){
                    btnScrollUp.setVisibility(View.VISIBLE);
                } else {
                    btnScrollUp.setVisibility(View.GONE);
                }

                if (!isLoading && lastVisibleItem == totalItem - 1) {
                    //float_arrow.setVisibility(View.VISIBLE);
                    isLoading = true;
                    // Scrolled to bottom. Do something here.
                    //Toast.makeText(ListBySourceActivity.this, "olo", Toast.LENGTH_SHORT).show();
                    loadMoreArticles();
                    isLoading = false;
                }
            }
        });

    }

    public void loadMoreArticles() {
        if ( page < totalPages) {
            page++;
            loadArticles();
        }
    }

    // Load Popular Articles to ImageSlider
    public void loadPopularArticles() {
        Session.readUserSession(getActivity());

        /*String url = Util.BASE_URL + "/api/article/popular/0/1/5";*/
        String url = Util.BASE_URL + "/api/article/popular/" + Session.USER_ID + "/1/5";
        AKNStringRequest request = new AKNStringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("RESPONSE_DATA");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                final JSONObject data = jsonArray.getJSONObject(i);

                                // Generate ImageSlider Object
                                slide = new MySlider(getActivity());
                                slide.image(data.getString("image"));
                                slide.setScaleType(BaseSliderView.ScaleType.CenterCrop);
                                slide.description(data.getString("title"));

                                // Handle ImageSlider Click
                                slide.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                    @Override
                                    public void onSliderClick(BaseSliderView slider) {
                                        try {
                                            //Toast.makeText(getActivity(), "" + data.getString("date"), Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getActivity(), ArticleDetailActivity.class);
                                            intent.putExtra("ARTICLE_ID", data.getInt("id"));
                                            intent.putExtra("DATE", data.getString("date"));
                                            intent.putExtra("URL", data.getString("url"));
                                            intent.putExtra("IS_SAVED", data.getBoolean("saved"));

                                            intent.putExtra("TITLE", data.getString("title"));
                                            intent.putExtra("THUMNAIL_URL", data.getString("image"));
                                            intent.putExtra("SOURCE_LOGO",
                                                    Util.BASE_LOGO_URL + data.getJSONObject("site").getString("logo"));
                                            startActivity(intent);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                                mSliderLayout.addSlider(slide);
                            }

                            // Add Animation to ImageSlider
                            mSliderLayout.setPresetTransformer(SliderLayout.Transformer.Stack);
                            mSliderLayout.setDuration(8000);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Util.showAlertDialog(getActivity(), "Sorry, Server Error!", true, SweetAlertDialog.ERROR_TYPE);
                error.printStackTrace();
            }
        });
        // Add request to Request Queue
        AppController.getInstance().addToRequestQueue(request);
    }

    @Override
    public void selectTab(int tabNum) {

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("test_fragment_home", "onResume");
        //Util.setDefaultTracker(getActivity(), "AKN, Screen Name", "Tab Name: HOME");
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name Tab Name: HOME");
        mTracker.setScreenName("AKN, Screen Name Tab Name: HOME");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }


    // Load articles
    public void loadArticles() {
        proDialog.show();

        Session.readUserSession(getActivity());
        int userId = Session.USER_ID;

        String url = Util.BASE_URL + "/api/article/" + page + "/" + row + "/0/0/" + userId + "/";

        AKNStringRequest stringRequest = new AKNStringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);

                    // For Pagination
                    totalPages = jsonObject.getInt("TOTAL_PAGES");
                    totalRecords = jsonObject.getInt("TOTAL_RECORDS");

                    JSONArray jsonArray = jsonObject.getJSONArray("RESPONSE_DATA");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        mArticle = new Article();
                        mArticle.setId(jsonArray.getJSONObject(i).getInt("id"));
                        mArticle.setTitle(jsonArray.getJSONObject(i).getString("title"));
                        mArticle.setViewCount((jsonArray.getJSONObject(i).getInt("hit")));
                        mArticle.setDate(jsonArray.getJSONObject(i).getString("date"));
                        mArticle.setImageUrl(jsonArray.getJSONObject(i).getString("image"));
                        mArticle.setSiteLogo(jsonArray.getJSONObject(i).getJSONObject("site").getString("logo"));

                        Log.d("home_tab", jsonArray.getJSONObject(i).getJSONObject("site").getString("logo"));

                        //mArticle.setSiteId(jsonArray.getJSONObject(i).getJSONObject("site").getInt("id"));
                        mArticle.setUrl(jsonArray.getJSONObject(i).getString("url"));
                        mArticle.setSaved(jsonArray.getJSONObject(i).getBoolean("saved"));

                        mArticles.add(mArticle);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (mArticles.size() > 0) {
                        //mAdapter.notifyDataSetChanged();
                        //scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
                        //scaleInAnimationAdapter.setDuration(300);
                        scaleInAnimationAdapter.notifyDataSetChanged();
                        //mRecyclerView.setAdapter(scaleInAnimationAdapter);
                    }

                    if (proDialog != null) proDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (proDialog != null) proDialog.dismiss();
                //Util.showAlertDialog(getActivity(), "Sorry, Server Error!", true, SweetAlertDialog.ERROR_TYPE);
                Log.d("mathearo", "ERROR: " + error);
            }
        });

        // Add request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

}
