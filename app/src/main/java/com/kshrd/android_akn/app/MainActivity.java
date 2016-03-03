package com.kshrd.android_akn.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.kshrd.android_akn.R;
import com.kshrd.android_akn.adapter.ViewPagerAdapter;
import com.kshrd.android_akn.util.AKNNavigationView;
import com.kshrd.android_akn.util.Session;
import com.kshrd.android_akn.util.Setting;
import com.kshrd.android_akn.util.Util;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Communication {
    private SearchView mSearchView;
    private SearchView.OnQueryTextListener mSearchViewListener;
    private Menu mMenu;
    private MenuItem mMenuItem;
    private TabLayout tabLayout;
    private AKNNavigationView navigationView;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.ic_home,
            R.drawable.ic_assignment,
            R.drawable.ic_language
    };

    private Typeface font;

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

        // Check Internet Connection
        if (!Util.isConnectingToInternet(getApplicationContext())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.msg_title_no_internet_connection));
            builder.setMessage(getString(R.string.msg_no_internet_connection));
            builder.setCancelable(false);
            builder.setPositiveButton(getString(R.string.try_again), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    recreate();
                }
            });
            builder.setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return;
        }

        // Wrap app_bar_main.xml into drawer_layout.xml
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.app_bar_main, contentFrame);

        // Set up Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(getResources().getString(R.string.app_full_name));
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Use Custom Navigation Drawer
        navigationView = new AKNNavigationView(this);

        viewPager = (ViewPager) findViewById(R.id.viewpager);

        setupViewPage(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        // Handle SearchView
        mSearchViewListener = new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {

                Intent intent  = new Intent(getBaseContext(), SearchActivity.class);
                intent.putExtra("KEY_WORD", query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        };

    }

    /**
     * Add icons to TabLayout
     */
    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    /**
     * Add Fragments to ViewPager
     * @param viewPager
     */
    private void setupViewPage(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment(), getString(R.string.tab_home));
        adapter.addFragment(new CategoryFragment(), getString(R.string.tab_category));
        adapter.addFragment(new SourceFragment(), getString(R.string.tab_source));
        viewPager.setAdapter(adapter);
        // Stop ViewPager from refreshing Fragment.
        viewPager.setOffscreenPageLimit(3);
    }

    /**
     * Please use onPostResume() instead of onResume() or App will be crashed.
      */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        Setting.loadLanguage(getBaseContext());

        Session.readUserSession(this);
        if (Session.IS_LOGIN) {
            if (navigationView != null) {
                navigationView.updateProfileOnNavHeader();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (drawer != null) drawer.closeDrawers();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    /**
     * Change Current Tab base on tabNum
      */
    @Override
    public void selectTab(int tabNum) {
        tabLayout.getTabAt(tabNum).select();
    }

}

