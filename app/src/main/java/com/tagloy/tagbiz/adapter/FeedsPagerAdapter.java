package com.tagloy.tagbiz.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.tagloy.tagbiz.fragment.AddFeedFragment;
import com.tagloy.tagbiz.fragment.ApprovedFeedFragment;
import com.tagloy.tagbiz.fragment.FeedFragment;
import com.tagloy.tagbiz.fragment.RejectedFeedFragment;

public class FeedsPagerAdapter extends FragmentStatePagerAdapter {
    int numOfTabs;

    public FeedsPagerAdapter(FragmentManager fragmentManager, int numOfTabs){
        super(fragmentManager);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new AddFeedFragment();
            case 1:
                return new FeedFragment();
            case 2:
                return new ApprovedFeedFragment();
            case 3:
                return new RejectedFeedFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
