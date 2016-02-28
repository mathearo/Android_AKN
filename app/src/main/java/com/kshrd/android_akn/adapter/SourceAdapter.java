package com.kshrd.android_akn.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.kshrd.android_akn.R;
import com.kshrd.android_akn.app.AppController;
import com.kshrd.android_akn.app.ListBySourceActivity;
import com.kshrd.android_akn.model.SourceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Buth Mathearo on 1/9/2016.
 */
public class SourceAdapter extends RecyclerView.Adapter<SourceAdapter.ViewHolder> {
    private View mView;
    private LayoutInflater mLayoutInflater;
    private ViewHolder mViewHolder;

    // Data Set
    private List<SourceItem> mSourceItemList = new ArrayList<>();

    public SourceAdapter(List<SourceItem> list) {
        super();
        mSourceItemList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //mView = mLayoutInflater.inflate(R.layout.source_item, parent, false);
        mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.source_item,parent,false);
        mViewHolder = new ViewHolder(mView);
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SourceItem sourceItem = mSourceItemList.get(position);
        //holder.mSourceName.setText(sourceItem.getSourceName());
        holder.mSourceName.setText(sourceItem.getSourceName());
        //holder.mSourceLogo.setImageResource(R.drawable.logo_sabay1);
        holder.mSourceLogo.setImageUrl(sourceItem.getLogoUrl(), AppController.getInstance().getImageLoader());
        //holder.mSourceLogo.setDefaultImageResId(R.drawable.ic_star_small_black);
        Log.d("mathearo_source", sourceItem.getLogoUrl());

    }

    @Override
    public int getItemCount() {
        return mSourceItemList.size();
    }


    // Create subclass of ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        /*public ImageView mSourceLogo;*/
        public NetworkImageView mSourceLogo;
        public TextView mSourceName;

        public ViewHolder(View itemView) {
            super(itemView);

            /*mSourceLogo = (ImageView) itemView.findViewById(R.id.logo_source);*/
            mSourceLogo = (NetworkImageView) itemView.findViewById(R.id.logo_source);
            mSourceLogo.setDrawingCacheEnabled(true);
            mSourceLogo.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
            mSourceLogo.setDefaultImageResId(R.drawable.ic_action_globe);

            mSourceName = (TextView) itemView.findViewById(R.id.tv_source);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), ListBySourceActivity.class);
            intent.putExtra("SOURCE_ID", mSourceItemList.get(getAdapterPosition()).getId());
            intent.putExtra("SOURCE_NAME", mSourceItemList.get(getAdapterPosition()).getSourceName());
            v.getContext().startActivity(intent);
        }
    }

}
