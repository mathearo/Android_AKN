package com.kshrd.android_akn.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.kshrd.android_akn.R;

/**
 * Created by Buth Mathearo on 1/4/2016.
 */

// ImageSlider
public class MySlider extends BaseSliderView {
    private ImageView imageView;
    private TextView tvTitle;
    private TextView tvDescription;

    public MySlider(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.slide_show_content,null);
        imageView = (ImageView) v.findViewById(R.id.slider_image);
        tvDescription = (TextView) v.findViewById(R.id.description);
        tvDescription.setText(getDescription());
        // Prevent 'Path must not be empty.'
        try {
            bindEventAndShow(v, imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return v;
    }
}
