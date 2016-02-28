package com.kshrd.android_akn.util;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

/**
 * Created by Buth Mathearo on 1/19/2016.
 */
public class AKNStringRequest extends StringRequest {

    public AKNStringRequest(int method, String url, Response.Listener listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return Util.getHeaders();
    }

    @Override
    public RetryPolicy getRetryPolicy() {
        return new DefaultRetryPolicy(Util.MAX_TIME_OUT,
                Util.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

}
