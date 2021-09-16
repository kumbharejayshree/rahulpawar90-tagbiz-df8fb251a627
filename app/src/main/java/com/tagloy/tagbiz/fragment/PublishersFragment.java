package com.tagloy.tagbiz.fragment;


import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.adapter.PublisherPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class PublishersFragment extends Fragment {

    TabLayout publisherTabLayout;
    ViewPager publisherViewPager;
    PublisherPagerAdapter publisherPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_publishers, container, false);
        publisherTabLayout = view.findViewById(R.id.publisherTabLayout);
        publisherViewPager = view.findViewById(R.id.publisherViewPager);
        publisherTabLayout.addTab(publisherTabLayout.newTab().setText("New Publisher"));
        publisherTabLayout.addTab(publisherTabLayout.newTab().setText("Live"));
        publisherTabLayout.addTab(publisherTabLayout.newTab().setText("Completed"));
        publisherTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        publisherTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#000000"));
        publisherTabLayout.setTabTextColors(Color.parseColor("#ffffff"),Color.parseColor("#000000"));
        publisherPagerAdapter = new PublisherPagerAdapter(getFragmentManager(),publisherTabLayout.getTabCount());
        publisherViewPager.setAdapter(publisherPagerAdapter);
        publisherViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(publisherTabLayout));
        publisherTabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                publisherViewPager.setCurrentItem(tab.getPosition());
                publisherPagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                publisherPagerAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Publisher");
    }
}
