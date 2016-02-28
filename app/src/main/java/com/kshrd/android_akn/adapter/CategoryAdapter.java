package com.kshrd.android_akn.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kshrd.android_akn.R;
import com.kshrd.android_akn.app.ListByCategoryActivity;
import com.kshrd.android_akn.app.StatisticActivity;
import com.kshrd.android_akn.model.CategoryItem;

import java.util.List;

/**
 * Created by Lim Seudy on 1/5/2016.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<CategoryItem>listCategory;
    private SparseBooleanArray selectedItem;
    private Context context;

    public CategoryAdapter(List<CategoryItem> list, Context context){
        super();
        this.listCategory=list;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CategoryItem item = listCategory.get(position);
        holder.categoryName.setText(item.getCategoryName());
        //holder.categoryIcon.setImageResource(item.getCategoryIcon());
    }

    @Override
    public int getItemCount() {
        return listCategory.size();
    }
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView categoryName;
        public ImageView categoryIcon;
        //public TextView categoryId;
        public ViewHolder(View view){
            super(view);
            categoryName = (TextView)view.findViewById(R.id.category_name);
            //Typeface font = Typeface.createFromAsset(getBaseContext().getAssets(), "fonts/KhmerOS_muol.ttf");
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Nokora-Regular.ttf");
            categoryName.setTypeface(tf);

            categoryIcon = (ImageView)view.findViewById(R.id.category_icon);
            //categoryId = (TextView)view.findViewById(R.id.cateogry_id);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent;
            if (listCategory.get(getAdapterPosition()).getCategoryName().equalsIgnoreCase("popular news")
                    || listCategory.get(getAdapterPosition()).getCategoryName().equals("ពត៌មានពេញនិយមប្រចាំថ្ងៃ")) {
                intent = new Intent(v.getContext(), StatisticActivity.class);
            } else {
                intent = new Intent(v.getContext(), ListByCategoryActivity.class);
                intent.putExtra("CATEGORY_ID",listCategory.get(getAdapterPosition()).getCategoryID());
                intent.putExtra("CATEGORY_NAME",listCategory.get(getAdapterPosition()).getCategoryName());
            }
            v.getContext().startActivity(intent);
        }
    }

}
