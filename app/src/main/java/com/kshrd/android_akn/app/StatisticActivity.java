
package com.kshrd.android_akn.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.adapter.ViewPagerAdapter;
import com.kshrd.android_akn.model.SpinnerItem;
import com.kshrd.android_akn.util.AKNNavigationView;
import com.kshrd.android_akn.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, Communication{
    private static final String urlCategory = Util.BASE_URL + "/api/article/category/";
    private static final String urlSource = Util.BASE_URL + "/api/article/site/";
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private Spinner cateSpin, sourceSpin;
    private List<SpinnerItem>categoryList;
    private List<SpinnerItem>sourceList;
    private TodayFragment todayFragment;
    private WeekFragment weekFragment;
    private MonthFragment monthFragment;
    private ProgressDialog proDialog;

    public int cateId = 0;
    public int sourceId = 0;
    public int tempCateId = 0;
    public int tempSourceId = 0;
    public int userId = 0;
    public static final int ROW = 10;
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
        getLayoutInflater().inflate(R.layout.activity_statistic, frameLayout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.statistic);
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_left);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Init Progress Dialog
        proDialog = new ProgressDialog(this, R.style.MyProgressDialogTheme);
        proDialog.setCancelable(true);
        proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
        proDialog.show();

        AKNNavigationView aknNavigationView = new AKNNavigationView(this);

        mViewPager = (ViewPager) findViewById(R.id.statistic_viewpager);
        setupViewPager(mViewPager);

        mTabLayout = (TabLayout) findViewById(R.id.statistic_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        cateSpin = (Spinner)findViewById(R.id.spinner_cateogry);
        sourceSpin = (Spinner)findViewById(R.id.spinner_source);

        loadingCategoryItem(urlCategory);
        loadingSourceItem(urlSource);
        //if (proDialog != null) proDialog.dismiss();
    }


    // load category spinner items
    public void loadingCategoryItem(String url){
        categoryList = new ArrayList<SpinnerItem>();
        categoryList.add(new SpinnerItem(0,"All Categories"));
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Result ", response.toString());
                try {
                    JSONArray cateList = new JSONObject(response).getJSONArray("DATA");
                    for(int i=0;i<cateList.length();i++){
                        SpinnerItem cate = new SpinnerItem();
                        cate.setId(cateList.getJSONObject(i).getInt("id"));
                        cate.setName(cateList.getJSONObject(i).getString("name"));
                        categoryList.add(cate);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally{
                    ArrayAdapter<SpinnerItem> cateAdapter = new ArrayAdapter<SpinnerItem>(StatisticActivity.this,R.layout.spinner_item,categoryList);
                    cateAdapter.setDropDownViewResource(R.layout.spinner_item);
                    cateSpin.setAdapter(cateAdapter);
                    cateSpin.setOnItemSelectedListener(StatisticActivity.this);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return Util.getHeaders();
            }
        };
        AppController.getInstance().getRequestQueue().add(request);
    }

    // load source spinner items
    public void loadingSourceItem(String url){
        sourceList = new ArrayList<SpinnerItem>();
        sourceList.add(new SpinnerItem(0, "All Sources"));
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Result ", response.toString());
                try {
                    JSONArray cateList = new JSONObject(response).getJSONArray("DATA");
                    for(int i=0;i<cateList.length();i++){
                        SpinnerItem cate = new SpinnerItem();
                        cate.setId(cateList.getJSONObject(i).getInt("id"));
                        cate.setName(cateList.getJSONObject(i).getString("name"));
                        sourceList.add(cate);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }finally{
                    ArrayAdapter<SpinnerItem> sourceAdapter = new ArrayAdapter<SpinnerItem>(StatisticActivity.this,R.layout.spinner_item,sourceList);
                    sourceAdapter.setDropDownViewResource(R.layout.spinner_item);
                    sourceSpin.setAdapter(sourceAdapter);
                    sourceSpin.setOnItemSelectedListener(StatisticActivity.this);
                    if (proDialog != null) proDialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
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
        AppController.getInstance().getRequestQueue().add(request);
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        todayFragment = new TodayFragment();
        weekFragment = new WeekFragment();
        monthFragment = new MonthFragment();
        adapter.addFragment(todayFragment,getResources().getString(R.string.today));
        adapter.addFragment(weekFragment, getResources().getString(R.string.week));
        adapter.addFragment(monthFragment, getResources().getString(R.string.month));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
    }

    /**
     * Change Current Tab base on tabNum
     */
    @Override
    public void selectTab(int tabNum) {
        mTabLayout.getTabAt(tabNum).select();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
        if(cateSpin.getCount()!= 0 && sourceSpin.getCount() != 0 ) {
            if (parent.getId() == R.id.spinner_cateogry) {
                cateId = ((SpinnerItem) parent.getItemAtPosition(position)).getId();
                sourceId = ((SpinnerItem) sourceSpin.getSelectedItem()).getId();

            } else if (parent.getId() == R.id.spinner_source) {
                cateId = ((SpinnerItem) cateSpin.getSelectedItem()).getId();
                sourceId = ((SpinnerItem) parent.getItemAtPosition(position)).getId();
            }

            //
            // check select tab
            /*int tabpostion = mTabLayout.getSelectedTabPosition();
            if (tabpostion == 0) {
                todayFragment.requestContent(cateId, sourceId, ROW);
            } else if (tabpostion == 1) {
                weekFragment.requestContent(cateId, sourceId, ROW);
            } else if (tabpostion == 2) {
                monthFragment.requestContent(cateId, sourceId, ROW);
            }*/
            todayFragment.requestContent(cateId, sourceId, ROW);
            weekFragment.requestContent(cateId, sourceId, ROW);
            monthFragment.requestContent(cateId, sourceId, ROW);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}