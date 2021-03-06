package com.kshrd.android_akn.app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.adapter.ArticleAdapter;
import com.kshrd.android_akn.model.Article;
import com.kshrd.android_akn.util.AKNStringRequest;
import com.kshrd.android_akn.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TodayFragment extends Fragment{
    private TextView mTextView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLinearLayoutManager;
    private ArticleAdapter mAdapter;
    private List<Article>mArticles = new ArrayList<>();
    private View view;
    private ProgressDialog proDialog;

    private boolean isLoading = false;
    private int catId, sourceId;
    // Pagination
    int row = 10;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =inflater.inflate(R.layout.fragment_today, container, false);

        // Init Progress Dialog
        proDialog = new ProgressDialog(getActivity(), R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        mTextView = (TextView)view.findViewById(R.id.no_article);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.today_recycler);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        catId = ((StatisticActivity) getActivity()).cateId;
        sourceId = ((StatisticActivity) getActivity()).sourceId;
        mAdapter = new ArticleAdapter(mArticles);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mArticles != null) {
                    mArticles.clear();
                    mAdapter.notifyDataSetChanged();
                    requestContent(((StatisticActivity) getActivity()).cateId,
                            ((StatisticActivity) getActivity()).sourceId, row);
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        //requestContent(0,0,5);
        return view;
    }

    public void requestContent(int cateId,int sourceId, int row){
        proDialog.show();
        String url = Util.BASE_URL + "/api/article/statistic/" + cateId + "/"+ sourceId +"/1/" + row;
        mTextView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        AKNStringRequest request = new AKNStringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Response",response.toString());

                // Clear Redundant
                if (mArticles != null) {
                    mArticles.clear();
                    mAdapter.notifyDataSetChanged();
                }

                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(response);

                    JSONArray jsonArray = jsonObject.getJSONArray("RESPONSE_DATA");

                    for (int i = 0; i < jsonArray.length(); i++) {
                        Article mArticle = new Article();
                        mArticle.setId(jsonArray.getJSONObject(i).getInt("id"));
                        mArticle.setTitle(jsonArray.getJSONObject(i).getString("title"));
                        mArticle.setViewCount((jsonArray.getJSONObject(i).getInt("hit")));
                        mArticle.setDate(jsonArray.getJSONObject(i).getString("date"));
                        mArticle.setImageUrl(jsonArray.getJSONObject(i).getString("image"));
                        mArticle.setSiteLogo(jsonArray.getJSONObject(i).getJSONObject("site").getString("logo"));
                        mArticle.setUrl(jsonArray.getJSONObject(i).getString("url"));
                        mArticle.setSaved(jsonArray.getJSONObject(i).getBoolean("saved"));
                        mArticles.add(mArticle);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if(mArticles.size()>0) {
                        mAdapter.notifyDataSetChanged();
                    }else{
                        mTextView.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    }

                    if (proDialog != null) proDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (proDialog != null) proDialog.dismiss();
            }
        });
        AppController.getInstance().getRequestQueue().add(request);
    }


}