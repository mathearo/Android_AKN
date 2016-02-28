package com.kshrd.android_akn.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.slider.library.SliderLayout;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.app.AppController;
import com.kshrd.android_akn.app.ArticleDetailActivity;
import com.kshrd.android_akn.model.Article;
import com.kshrd.android_akn.util.AKNStringRequest;
import com.kshrd.android_akn.util.Session;
import com.kshrd.android_akn.util.Util;
import com.poliveira.parallaxrecyclerview.ParallaxRecyclerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Buth Mathearo on 1/5/2016.
 */
/*public class ParallaxArticleAdapter extends RecyclerView.Adapter <ParallaxArticleAdapter.ViewHolder>  {*/
public class ParallaxArticleAdapter extends ParallaxRecyclerAdapter<Article> implements ParallaxRecyclerAdapter.OnClickEvent {
    private List<Article> mArticles;
    private int curItemPosition;
    private Activity activity;
    private String strViewCount;
    private ProgressDialog proDialog;
    private Typeface tf;

    public ParallaxArticleAdapter(List<Article> mArticles) {
        super(mArticles);
        this.mArticles = mArticles;
        setOnClickEvent(this);
    }



    @Override
    public void onBindViewHolderImpl(RecyclerView.ViewHolder viewHolder, ParallaxRecyclerAdapter<Article> parallaxRecyclerAdapter, int position) {
        final Article article = mArticles.get(position);
        //curItemPosition = position;

        final ParallaxArticleAdapter.ViewHolder holder = (ParallaxArticleAdapter.ViewHolder) viewHolder;

        holder.tvTitle.setText(article.getShortTitle());
        if (article.getViewCount() <= 0) {
            strViewCount = "0 View";
        } else strViewCount = article.getViewCount() + " Views";
        holder.tvViewCount.setText(strViewCount);
        //holder.tvViewCount.setText("" + article.getViewCount() + " Views");
        holder.tvDate.setText(Util.convertToDate(article.getDate()));
        holder.imageViewThumnail.setImageUrl(article.getImageUrl(), AppController.getInstance().getImageLoader());
        //holder.imageViewLogo.setImageResource(Util.getSourceIcon(article.getSiteId()));
        holder.imageViewLogo.setImageUrl(article.getSiteLogo(), AppController.getInstance().getImageLoader());

        // Read User Data from SharedPreferences
        Session.readUserSession(holder.imageViewLogo.getContext());

        activity = (Activity) holder.tvTitle.getContext();

        // Manage Save Icon and Delete Icon
        if (activity.getClass().getCanonicalName().equals(Util.SAVE_LIST_ACTIVITY)){
            holder.btnDeleteSave.setVisibility(View.VISIBLE);
        } else if (article.isSaved() == false) {
            holder.btnSave.setVisibility(View.VISIBLE);
        }

        holder.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Session.readUserSession(v.getContext());
                if (Session.IS_LOGIN) {
                    // Init Progress Dialog
                    proDialog = new ProgressDialog(v.getContext(), R.style.MyProgressDialogTheme);
                    proDialog.setCancelable(true);
                    proDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);
                    proDialog.show();
                    //int userId = 18;
                    int userId = Session.USER_ID;
                    if(userId != 0){
                        int newsId = article.getId();
                        String url = Util.BASE_URL +"/api/article/savelist";
                        JSONObject object = new JSONObject();

                        try {
                            object.put("newsid", newsId);
                            object.put("userid", userId);
                            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,
                                    object, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    if (proDialog != null) proDialog.dismiss();
                                /*holder.btnSave.setVisibility(View.GONE);
                                holder.btnSaved.setVisibility(View.VISIBLE);*/
                                    holder.btnSave.setVisibility(View.GONE);
                                    Util.showAlertDialog((Activity) v.getContext(),
                                            v.getContext().getString(R.string.added_to_saved_lists), true, SweetAlertDialog.SUCCESS_TYPE);
                                }
                            } ,new Response.ErrorListener() {
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
                            // Add to Request to Request Queue
                            AppController.getInstance().addToRequestQueue(request);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            if (proDialog != null) proDialog.dismiss();
                        }
                    }
                } else {
                    Util.showAlertDialog((Activity) v.getContext(), v.getContext().getString(R.string.please_login_to_save), true,
                            SweetAlertDialog.WARNING_TYPE);
                }

            }
        });

        holder.btnDeleteSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v) {
                Session.readUserSession(activity);
                String url = Util.BASE_URL + "/api/article/savelist/" + article.getId()
                        + "/" + Session.USER_ID + "/";
                /*DELETE /api/article/savelist/{newsid}/{userid*/
                AKNStringRequest request = new AKNStringRequest(Request.Method.DELETE, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    if (jsonObject.getInt("STATUS") == 200) {
                                        mArticles.remove(article);
                                        notifyDataSetChanged();
                                        Toast.makeText(activity, "Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

                // Add Request to Request Queue
                AppController.getInstance().addToRequestQueue(request);

            }
        });

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup parent, ParallaxRecyclerAdapter<Article> parallaxRecyclerAdapter, int i) {
        Context context = parent.getContext();
        // Check if user changed language
        SharedPreferences sharedPref = context.getSharedPreferences("setting",
                Context.MODE_PRIVATE);
        Locale locale = null;
        Configuration config = null;

        if (locale == null) locale = new Locale(sharedPref.getString("LANGUAGE", "en"));
        Locale.setDefault(locale);
        if (config == null) config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());

        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.article, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public int getItemCountImpl(ParallaxRecyclerAdapter<Article> parallaxRecyclerAdapter) {
        return mArticles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public NetworkImageView imageViewThumnail;
        public NetworkImageView  imageViewLogo;
        public TextView tvTitle, tvViewCount, tvDate;
        public Button btnSave;
        public ImageButton btnDeleteSave;


        public SliderLayout mSliderLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewThumnail = (NetworkImageView) itemView.findViewById(R.id.image_view_thumnail);
            imageViewThumnail.setDrawingCacheEnabled(true);
            imageViewThumnail.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
            //imageViewThumnail.setDefaultImageResId(R.drawable.ic_no_image);
            imageViewThumnail.setDefaultImageResId(R.drawable.ic_no_image_available);

            imageViewLogo = (NetworkImageView) itemView.findViewById(R.id.image_view_logo);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            // Get Font from assets
            tf = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/Nokora-Bold.ttf");
            tvTitle.setTypeface(tf);

            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvViewCount = (TextView) itemView.findViewById(R.id.tv_view);
            btnSave = (Button) itemView.findViewById(R.id.btnSave);
            btnSave.setTypeface(tf);
            btnDeleteSave = (ImageButton) itemView.findViewById(R.id.btn_delete_save);
        }

    }

    @Override
    public long getItemId(int position) {
        return mArticles.get(position).hashCode();
    }

    @Override
    public void onClick(View view, int i) {
        Intent intent = new Intent(view.getContext(), ArticleDetailActivity.class);
        intent.putExtra("ARTICLE_ID",mArticles.get(i).getId());
        intent.putExtra("TITLE", mArticles.get(i).getTitle());
        intent.putExtra("THUMNAIL_URL", mArticles.get(i).getImageUrl());
        intent.putExtra("SOURCE_LOGO", mArticles.get(i).getSiteLogo());
        intent.putExtra("DATE", mArticles.get(i).getDate());
        intent.putExtra("URL", mArticles.get(i).getUrl());
        Log.d("home_tab", "id: " + mArticles.get(i).getId());
        view.getContext().startActivity(intent);
    }

}
