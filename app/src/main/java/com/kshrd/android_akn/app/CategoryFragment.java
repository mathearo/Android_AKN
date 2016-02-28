package com.kshrd.android_akn.app;

import android.app.ProgressDialog;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.adapter.CategoryAdapter;
import com.kshrd.android_akn.model.CategoryItem;
import com.kshrd.android_akn.util.AKNStringRequest;
import com.kshrd.android_akn.util.Setting;
import com.kshrd.android_akn.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

/**
 * Created by Buth Mathearo on 12/31/2015.
 */
public class CategoryFragment extends Fragment {
    private RecyclerView mRecyclerCategory;
    private RecyclerView.LayoutManager layoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<CategoryItem> mCategories = new ArrayList<>();
    private CategoryAdapter categoryAdapter;
    private ScaleInAnimationAdapter scaleInAnimationAdapter;
    private ProgressDialog proDialog;
    private Tracker mTracker;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Init Progress Dialog
        proDialog = new ProgressDialog(getActivity(), R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        //Util.setDefaultTracker(getActivity(), "AKN, Screen Name", "Tab Name: CATEGORY");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name Tab Name: CATEGORY");
        mTracker.setScreenName("AKN, Screen Name Tab Name: CATEGORY");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        View view = inflater.inflate(R.layout.fragment_category, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setEnabled(true);
        initEvent();
        mRecyclerCategory = (RecyclerView) view.findViewById(R.id.list_category);
        categoryAdapter = new CategoryAdapter(mCategories, getActivity());

        scaleInAnimationAdapter = new ScaleInAnimationAdapter(categoryAdapter);
        scaleInAnimationAdapter.setDuration(500);

        mRecyclerCategory.setAdapter(scaleInAnimationAdapter);
        // Check Enable or Disable Scroll Animation
        setScrollAnimation();

        layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerCategory.setLayoutManager(layoutManager);
        mRecyclerCategory.setHasFixedSize(true);

        requestResponse();

        return view;
    }

    public void initEvent() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Clear Data in RecyclerView
                if (mCategories != null) {
                    mCategories.clear();
                    categoryAdapter.notifyDataSetChanged();
                    scaleInAnimationAdapter.notifyDataSetChanged();
                }
                requestResponse();
                // Close Loading Process
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
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

    @Override
    public void onResume() {
        super.onResume();
        //Util.setDefaultTracker(getActivity(), "AKN, Screen Name", "Tab Name: CATEGORY");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name Tab Name: CATEGORY");
        mTracker.setScreenName("AKN, Screen Name Tab Name: CATEGORY");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    // List all categories
    public void requestResponse(){
        proDialog.show();

        String url = Util.BASE_URL + "/api/article/category/";
        AKNStringRequest request = new AKNStringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray cateList = new JSONObject(response).getJSONArray("DATA");

                    for(int i=0;i<cateList.length();i++){
                        CategoryItem cate = new CategoryItem();
                        cate.setCategoryID(cateList.getJSONObject(i).getInt("id"));
                        cate.setCategoryName(cateList.getJSONObject(i).getString("name"));
                        /*cate.setCategoryIcon(R.drawable.ic_folder_small);*/
                        mCategories.add(cate);
                    }

                    // Statistic Category
                    CategoryItem cate = new CategoryItem();
                    cate.setCategoryID(0);
                    cate.setCategoryName(getString(R.string.popular_news));

                    mCategories.add(cate);

                    //Log.d("testing", mCategories.size() > 0 ? "true": "false");

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (mCategories.size() > 0){
                        scaleInAnimationAdapter.notifyDataSetChanged();
                    }

                    if (proDialog != null) proDialog.dismiss();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Util.showAlertDialog(getActivity(), "Sorry, Server Error!", true, SweetAlertDialog.ERROR_TYPE);
                if (proDialog != null) proDialog.dismiss();
            }
        });

        // Add Request to Request Queue
        AppController.getInstance().addToRequestQueue(request);
    }

}




