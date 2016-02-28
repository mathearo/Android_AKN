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
import com.kshrd.android_akn.adapter.SourceAdapter;
import com.kshrd.android_akn.model.SourceItem;
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
public class SourceFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private List<SourceItem> mSourceItems = new ArrayList<>();
    private SourceItem mSourceItem;
    private List<SourceItem> mSourceList = new ArrayList<>();;
    private SourceItem mSource;
    private SourceAdapter mAdapter;
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

        //Util.setDefaultTracker(getActivity(), "AKN, Screen Name", "Tab Name: SOURCE");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name Tab Name: SOURCE");
        mTracker.setScreenName("AKN, Screen Name Tab Name: SOURCE");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        View view = inflater.inflate(R.layout.fragment_source, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setEnabled(true);
        initEvent();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_source_fragment);
        mAdapter = new SourceAdapter(mSourceList);

        scaleInAnimationAdapter = new ScaleInAnimationAdapter(mAdapter);
        scaleInAnimationAdapter.setDuration(500);
        mRecyclerView.setAdapter(scaleInAnimationAdapter);

        // Check Enable or Disable Scroll Animation
        setScrollAnimation();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);

        loadSource();

        return view;
    }

    public void initEvent() {
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Clear Data in RecyclerView
                if (mSourceList != null) {
                    mSourceList.clear();
                    mAdapter.notifyDataSetChanged();
                    scaleInAnimationAdapter.notifyDataSetChanged();

                }
                loadSource();
                // Stop Loading Progress
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    // Check Enable or Disable Scroll Animation
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
        //Util.setDefaultTracker(getActivity(), "AKN, Screen Name", "Tab Name: SOURCE");
        // Google Analytics
        mTracker = AppController.getInstance().getDefaultTracker();
        Log.i("test_home_fragment", "AKN, Screen Name Tab Name: SOURCE");
        mTracker.setScreenName("AKN, Screen Name Tab Name: SOURCE");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void loadSource() {
        proDialog.show();

        String url = Util.BASE_URL + "/api/article/site/";

        AKNStringRequest stringRequest = new AKNStringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Toast.makeText(getActivity(), "" + response, Toast.LENGTH_LONG).show();
                Log.d("mathearo", response);
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("DATA");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        mSource = new SourceItem();
                        mSource.setId(jsonArray.getJSONObject(i).getInt("id"));
                        mSource.setSourceName(jsonArray.getJSONObject(i).getString("name"));
                        mSource.setUrl(jsonArray.getJSONObject(i).getString("url"));
                        mSource.setLogoUrl(jsonArray.getJSONObject(i).getString("logo"));

                        mSourceList.add(mSource);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    //mAdapter.notifyDataSetChanged();
                    if (mSourceList.size() > 0) {
                        scaleInAnimationAdapter.notifyDataSetChanged();
                    }
                    if (proDialog != null) proDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (proDialog != null) proDialog.dismiss();
                Util.showAlertDialog(getActivity(), "Sorry, Server Error!", true, SweetAlertDialog.ERROR_TYPE);
                Log.d("mathearo", "ERROR: " + error);
            }
        });

        AppController.getInstance().addToRequestQueue(stringRequest);
    }
}




