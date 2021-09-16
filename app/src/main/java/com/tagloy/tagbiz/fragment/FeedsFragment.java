package com.tagloy.tagbiz.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.adapter.FeedsPagerAdapter;


public class FeedsFragment extends Fragment {

    TabLayout feedsTabLayout;
    ViewPager feedsViewPager;
    FeedsPagerAdapter feedsPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feeds, container, false);
        feedsTabLayout = view.findViewById(R.id.feedsTabLayout);
        feedsViewPager = view.findViewById(R.id.feedViewPager);
        feedsTabLayout.addTab(feedsTabLayout.newTab().setText("Upload Feed"));
        feedsTabLayout.addTab(feedsTabLayout.newTab().setText("New Feeds"));
        feedsTabLayout.addTab(feedsTabLayout.newTab().setText("Approved"));
        feedsTabLayout.addTab(feedsTabLayout.newTab().setText("Rejected"));
        feedsTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        feedsTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#000000"));
        feedsTabLayout.setTabTextColors(Color.parseColor("#ffffff"),Color.parseColor("#000000"));
        feedsPagerAdapter = new FeedsPagerAdapter(getFragmentManager(),feedsTabLayout.getTabCount());
        feedsViewPager.setAdapter(feedsPagerAdapter);
        feedsViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(feedsTabLayout));
        feedsTabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                feedsViewPager.setCurrentItem(tab.getPosition());
                feedsPagerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return view;
    }

}
