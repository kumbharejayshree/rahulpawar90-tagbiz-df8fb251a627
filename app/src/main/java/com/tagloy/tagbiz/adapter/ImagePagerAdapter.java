package com.tagloy.tagbiz.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.models.Captures;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ImagePagerAdapter extends PagerAdapter {

    Context context;
    List<Captures> capturesList;

    public ImagePagerAdapter(Context context, List<Captures> capturesList){
        this.context = context;
        this.capturesList = capturesList;
    }

    @Override
    public int getCount() {
        return capturesList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.pager_item,null);
        ImageView backgroundImage = view.findViewById(R.id.pictureViewBackground);
        ImageView pictureView = view.findViewById(R.id.pictureView);
        TextView pagination = view.findViewById(R.id.pagination);
        Glide.with(context)
                .load(capturesList.get(position).getImagePath())
                .into(pictureView);
        Glide.with(context).load(capturesList.get(position).getImagePath())
                .apply(bitmapTransform(new BlurTransformation(25)))
                .into(backgroundImage);
        pagination.setText(String.valueOf(position+1) + " of " + capturesList.size());
        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        object = null;
    }
}
