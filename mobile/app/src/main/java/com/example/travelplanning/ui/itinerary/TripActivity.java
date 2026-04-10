package com.example.travelplanning.ui.itinerary;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.travelplanning.R;

public class TripActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        if (savedInstanceState == null) {
            navigateTo(new TripFragment(), false);
        }
    }

    // navigate to fragments
    public void navigateTo(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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
