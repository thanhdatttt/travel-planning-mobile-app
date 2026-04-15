package com.example.travelplanning.ui.itinerary;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TripPublicPagerAdapter extends FragmentStateAdapter {
    public TripPublicPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new TripPublicLocationListFragment(); // Tab 1 saved locations list
        } else {
            return new PublicItineraryFragment();     // Tab 2 itinerary
        }
    }

    @Override
    public int getItemCount() {
        return 2; // number of tabs
    }
}
