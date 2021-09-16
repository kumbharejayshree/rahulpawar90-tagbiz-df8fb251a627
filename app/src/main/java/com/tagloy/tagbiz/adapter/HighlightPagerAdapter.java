package com.tagloy.tagbiz.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.tagloy.tagbiz.fragment.CompletedHighlightFragment;
import com.tagloy.tagbiz.fragment.HighlightFragment;
import com.tagloy.tagbiz.fragment.LiveHighlightFragment;

public class HighlightPagerAdapter extends FragmentStatePagerAdapter {
    int numOfTabs;

    public HighlightPagerAdapter(FragmentManager fragmentManager, int numOfTabs){
        super(fragmentManager);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new HighlightFragment();
            case 1:
                return new LiveHighlightFragment();
            case 2:
                return new CompletedHighlightFragment();
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
        if (object instanceof HighlightFragment)
            return POSITION_UNCHANGED;
        else
            return POSITION_NONE;
    }
}
