package com.kshrd.android_akn.app;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.util.BitmapCache;

public class AppController extends Application {
	// For Google Analytics
	private Tracker mTracker;

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private static AppController mInstance;
	private static final String TAG = AppController.class.getSimpleName();
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}

	public static synchronized AppController getInstance() {
		return mInstance;
	}
	
	public RequestQueue getRequestQueue() {
		if (mRequestQueue == null) {
			mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		}
		return mRequestQueue;
	}
	
	public ImageLoader getImageLoader() {
		getRequestQueue();
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(mRequestQueue, new BitmapCache());
		}
		return mImageLoader;
	}
	
	public <T> void addToRequestQueue(Request<T> request, String tag) {
		request.setTag(TextUtils.isEmpty(tag)? TAG : tag);
		getRequestQueue().add(request);
	}
	
	public <T> void addToRequestQueue(Request<T> request) {
		getRequestQueue().add(request);
	}
	
	public void cancelPendingRequest(Object tag) {
		if (mRequestQueue != null) {
			mRequestQueue.cancelAll(tag);
		}
	}


	// For Google Analytics

	/**
	 * Gets the default {@link Tracker} for this {@link Application}.
	 * @return tracker
	 */
	synchronized public Tracker getDefaultTracker() {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.global_tracker);
		}
		return mTracker;
	}

	// My own method
	synchronized public void setDefaultTracker(String strDesc, String screenName) {
		if (mTracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
			mTracker = analytics.newTracker(R.xml.global_tracker);
		}

		// For Google Analytics
		Log.i("google_analytics", "Setting screen name: " + screenName);
		mTracker.setScreenName(strDesc + ": " + screenName);
		mTracker.send(new HitBuilders.ScreenViewBuilder().build());
	}
	
}
