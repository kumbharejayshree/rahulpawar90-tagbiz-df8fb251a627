package com.tagloy.tagbiz.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.tagloy.tagbiz.fragment.CompletedPublisherFragment;
import com.tagloy.tagbiz.fragment.LivePublisherFragment;
import com.tagloy.tagbiz.fragment.PublisherFragment;

public class PublisherPagerAdapter extends FragmentStatePagerAdapter {
    int numOfTabs;

    public PublisherPagerAdapter(FragmentManager fragmentManager, int numOfTabs){
        super(fragmentManager);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new PublisherFragment();
            case 1:
                return new LivePublisherFragment();
            case 2:
                return new CompletedPublisherFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (object instanceof CompletedPublisherFragment)
            return POSITION_UNCHANGED;
        else
            return POSITION_NONE;
    }
}
