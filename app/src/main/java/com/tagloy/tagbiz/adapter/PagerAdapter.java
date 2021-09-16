package com.tagloy.tagbiz.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.tagloy.tagbiz.fragment.FeedFragment;
import com.tagloy.tagbiz.fragment.HighlightFragment;
import com.tagloy.tagbiz.fragment.PublisherFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int numOfTabs;

    public PagerAdapter(FragmentManager fragmentManager, int numOfTabs){
        super(fragmentManager);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new FeedFragment();
            case 1:
                return new PublisherFragment();
            case 2:
                return new HighlightFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
