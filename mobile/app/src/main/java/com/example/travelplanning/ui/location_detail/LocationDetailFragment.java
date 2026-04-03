package com.example.travelplanning.ui.location_detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.travelplanning.R;
import com.example.travelplanning.viewmodel.location_detail.LocationDetailViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class LocationDetailFragment extends Fragment {
    private LocationDetailViewModel viewModel;
    private ViewPager2 vpImageSlider;
    private TextView tvName, tvDescription, tvRatingScore, tvRatingCount;
    private MaterialButton btnCall, btnWebsite, btnEmail;
    private RatingBar ratingBar;
    private String locationId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the Navigation Controller passed any arguments
        if (getArguments() != null) {
            // Retrieve the string using the same key "location_id"
            this.locationId = getArguments().getString("location_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LocationDetailViewModel.class);
        initViews(view);
        setupObservers();

        if (locationId != null) {
            viewModel.fetchDetail(locationId);
        }
    }

    private void setupObservers() {
        viewModel.getLocationDetail().observe(getViewLifecycleOwner(), location -> {
            if (location == null) {
                Log.d("DEBUG_DETAIL", "Location data is null");
                return;
            }
            Log.d("DEBUG_DETAIL", "Data received: " + location.getName());
            tvName.setText(location.getName());
            tvDescription.setText(location.getDescription());

            double avgRating = location.getAvgRating() != null ? location.getAvgRating() : 0.0;
            tvRatingScore.setText(String.format(java.util.Locale.US, "%.1f", avgRating));

            ratingBar.setRating((float) avgRating);

            int count = location.getRatingCount() != null ? location.getRatingCount() : 0;
            tvRatingCount.setText(String.format(java.util.Locale.US, "(%d reviews)", count));

            // Setup Slider
            List<String> urls = new ArrayList<>();
            if (location.getPhotoUrls() != null) {
                for (String photo : location.getPhotoUrls()) {
                    urls.add(photo.getUrl());
                }
            }
            ImageSliderAdapter adapter = new ImageSliderAdapter(urls);
            vpImageSlider.setAdapter(adapter);

            // Action Buttons logic
            btnCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + location.getPhone()));
                startActivity(intent);
            });

            btnWebsite.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(location.getWebsite()));
                startActivity(intent);
            });

            btnEmail.setOnClickListener(v -> {
                // Since 'email' isn't in your schema, we use a placeholder or check metadata
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@travel.com"));
                startActivity(intent);
            });
        });
    }

    private void initViews(View v) {
        vpImageSlider = v.findViewById(R.id.vpImageSlider);
        tvName = v.findViewById(R.id.tvDetailName);
        tvDescription = v.findViewById(R.id.tvDescription);

        tvRatingScore = v.findViewById(R.id.tvDetailRatingScore);
        ratingBar = v.findViewById(R.id.ratingBar);
        tvRatingCount = v.findViewById(R.id.tvDetailRatingCount);

        btnCall = v.findViewById(R.id.btnCall);
        btnWebsite = v.findViewById(R.id.btnWebsite);
        btnEmail = v.findViewById(R.id.btnEmail);
    }
}