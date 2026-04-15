package com.example.travelplanning.ui.itinerary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.travelplanning.R;
import com.example.travelplanning.ui.mainscreen.MainScreenActivity;

public class TripContainerFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_trip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull  View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // handle visibility of nav bar when back
        getChildFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getChildFragmentManager().findFragmentById(R.id.trip_fragment_container);
            if (getActivity() instanceof MainScreenActivity && currentFragment != null) {
                if (currentFragment instanceof CreateTripFragment || currentFragment instanceof TripSettingFragment) {
                    ((MainScreenActivity) getActivity()).setBottomNavVisibility(View.GONE);
                } else {
                    ((MainScreenActivity) getActivity()).setBottomNavVisibility(View.VISIBLE);
                }
            }
        });

        if (savedInstanceState == null) {
            navigateTo(new TripFragment(), false);
        }
    }

    // navigate to fragments
    public void navigateTo(Fragment fragment, boolean addToBackStack) {
        // check what fragment will hide / open the nav bar
        if (getActivity() instanceof MainScreenActivity) {
            if (fragment instanceof CreateTripFragment || fragment instanceof TripSettingFragment) {
                ((MainScreenActivity) getActivity()).setBottomNavVisibility(View.GONE);
            } else {
                ((MainScreenActivity) getActivity()).setBottomNavVisibility(View.VISIBLE);
            }
        }

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        // set animation when change fragment
        transaction.setCustomAnimations(
                android.R.anim.slide_in_left,  // enter
                android.R.anim.slide_out_right, // exit
                android.R.anim.slide_in_left,   // popEnter
                android.R.anim.slide_out_right  // popExit
        );
        transaction.replace(R.id.trip_fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}
