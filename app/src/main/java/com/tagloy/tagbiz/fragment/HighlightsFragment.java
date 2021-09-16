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
import com.tagloy.tagbiz.adapter.HighlightPagerAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class HighlightsFragment extends Fragment {

    TabLayout highlightTabLayout;
    ViewPager highlightViewPager;
    HighlightPagerAdapter highlightPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_highlights, container, false);
        highlightTabLayout = view.findViewById(R.id.highlightTabLayout);
        highlightViewPager = view.findViewById(R.id.highlightViewPager);
        highlightTabLayout.addTab(highlightTabLayout.newTab().setText("New Highlight"));
        highlightTabLayout.addTab(highlightTabLayout.newTab().setText("Live"));
        highlightTabLayout.addTab(highlightTabLayout.newTab().setText("Deleted"));
        highlightTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        highlightTabLayout.setSelectedTabIndicatorColor(Color.parseColor("#000000"));
        highlightTabLayout.setTabTextColors(Color.parseColor("#ffffff"),Color.parseColor("#000000"));
        highlightPagerAdapter = new HighlightPagerAdapter(getFragmentManager(),highlightTabLayout.getTabCount());
        highlightViewPager.setAdapter(highlightPagerAdapter);
        highlightViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(highlightTabLayout));
        highlightTabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                highlightViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                highlightPagerAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Highlight");
    }

}
