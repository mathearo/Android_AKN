package com.kshrd.android_akn.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.app.AppController;
import com.kshrd.android_akn.app.ArticleDetailActivity;
import com.kshrd.android_akn.model.Article;
import com.kshrd.android_akn.util.AKNStringRequest;
import com.kshrd.android_akn.util.Session;
import com.kshrd.android_akn.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Buth Mathearo on 1/5/2016.
 */
public class ArticleAdapter extends RecyclerView.Adapter <ArticleAdapter.ViewHolder>  {
    private List<Article> mArticles;
    private int curItemPosition;
    private Activity activity;
    private String strViewCount;
    private ProgressDialog proDialog;
    private Typeface tf;


    // Constructor
    public ArticleAdapter(List<Article> articles) {
        mArticles = articles;
    }

    @Override
    public ArticleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

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

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.article, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public long getItemId(int position) {
        return mArticles.get(position).hashCode();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Article article = mArticles.get(position);
        curItemPosition = position;

        holder.tvTitle.setText(article.getShortTitle());
        if (article.getViewCount() <= 0) {
            strViewCount = "0 View";
        } else strViewCount = article.getViewCount() + " Views";
        holder.tvViewCount.setText(strViewCount);
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
                                    holder.btnSave.setVisibility(View.GONE);
                                    Util.showAlertDialog((Activity) v.getContext(),
                                            v.getContext().getString(R.string.added_to_saved_lists),
                                            true, SweetAlertDialog.SUCCESS_TYPE);
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
            public void onClick(final View v) {

                // Show Alert Dialog when Delete Saved List Item

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                builder.setTitle(v.getContext().getString(R.string.confirm));
                builder.setMessage(v.getContext().getString(R.string.delete_confirm));

                builder.setNegativeButton(v.getContext().getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.setPositiveButton(v.getContext().getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                                                if (mArticles.size() == 0) {
                                                    Activity act = (Activity) v.getContext();
                                                    RecyclerView recyclerView = (RecyclerView)
                                                            act.findViewById(R.id.list_save_list);
                                                    LinearLayout layoutSavedListEmpty = (LinearLayout)
                                                            act.findViewById(R.id.layout_saved_lists_empty);

                                                    recyclerView.setVisibility(View.GONE);
                                                    layoutSavedListEmpty.setVisibility(View.VISIBLE);
                                                }
                                                Toast.makeText(activity,
                                                           v.getContext().getString(R.string.deleted),
                                                        Toast.LENGTH_SHORT).show();
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

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

    }
    @Override
    public int getItemCount() {
        return mArticles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public NetworkImageView imageViewThumnail;
        /*public CircularImageView imageViewLogo;*/
        public NetworkImageView  imageViewLogo;
        public TextView tvTitle, tvViewCount, tvDate;
        public Button btnSave;
        /*public ImageButton btnSave, btnSaved, btnDeleteSave;*/
        public ImageButton btnDeleteSave;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewThumnail = (NetworkImageView) itemView.findViewById(R.id.image_view_thumnail);
            imageViewThumnail.setDrawingCacheEnabled(true);
            imageViewThumnail.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
            imageViewThumnail.setDefaultImageResId(R.drawable.ic_no_image_available);

            /*imageViewLogo = (CircularImageView) itemView.findViewById(R.id.image_view_logo);*/
            imageViewLogo = (NetworkImageView) itemView.findViewById(R.id.image_view_logo);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tf = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/Nokora-Bold.ttf");
            tvTitle.setTypeface(tf);

            tvDate = (TextView) itemView.findViewById(R.id.tv_date);

            tvViewCount = (TextView) itemView.findViewById(R.id.tv_view);

            /*btnSave = (ImageButton) itemView.findViewById(R.id.btn_save);
            btnSaved = (ImageButton) itemView.findViewById(R.id.btn_saved);*/
            btnSave = (Button) itemView.findViewById(R.id.btnSave);
            btnSave.setTypeface(tf);
            btnDeleteSave = (ImageButton) itemView.findViewById(R.id.btn_delete_save);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //Log.d("Article ID",mArticles.get(getAdapterPosition()).getId()+"");
            Intent intent = new Intent(view.getContext(), ArticleDetailActivity.class);
            intent.putExtra("ARTICLE_ID",mArticles.get(getAdapterPosition()).getId());
            intent.putExtra("TITLE", mArticles.get(getAdapterPosition()).getTitle());
            intent.putExtra("THUMNAIL_URL", mArticles.get(getAdapterPosition()).getImageUrl());
            intent.putExtra("SOURCE_LOGO",mArticles.get(getAdapterPosition()).getSiteLogo());
            intent.putExtra("DATE", mArticles.get(getAdapterPosition()).getDate());
            intent.putExtra("URL",mArticles.get(getAdapterPosition()).getUrl());

            view.getContext().startActivity(intent);
        }
    }

}
