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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.location.Photo;
import com.example.travelplanning.viewmodel.location_detail.LocationDetailViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class LocationDetailFragment extends Fragment {
    private LocationDetailViewModel viewModel;
    private ViewPager2 vpImageSlider;
    private TextView tvName, tvDescription, tvRatingScore, tvRatingCount, tvSeeMore;
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

        if (getArguments() != null) {
            String locationId = getArguments().getString("location_id");
            if (locationId != null) {
                viewModel.fetchDetail(locationId);
            }
        }
    }

    private void setupObservers() {
        viewModel.getLocationDetail().observe(getViewLifecycleOwner(), location -> {
//            if (location == null) {
//                Log.d("DEBUG_DETAIL", "Location data is null");
//                return;
//            }
//            Log.d("DEBUG_DETAIL", "Data received: " + location.getName());
//
//            if (location.getPhotos() != null) {
//                Log.d("DEBUG_DETAIL", "Photo count: " + location.getPhotos().size());
//                for (Photo p : location.getPhotos()) {
//                    Log.d("DEBUG_DETAIL", "Photo URL: " + p.getUrl());
//                }
//            } else {
//                Log.e("DEBUG_DETAIL", "Photos list is NULL");
//            }

            tvName.setText(location.getName());
            tvDescription.setText(location.getDescription());
            tvDescription.post(() -> {
                // Chỉ hiện "See more" nếu text thực sự dài hơn 4 dòng
                if (tvDescription.getLineCount() > 4) {
                    tvSeeMore.setVisibility(View.VISIBLE);
                } else {
                    tvSeeMore.setVisibility(View.GONE);
                }
            });

            double avgRating = location.getAvgRating() != null ? location.getAvgRating() : 0.0;
            tvRatingScore.setText(String.format(java.util.Locale.US, "%.1f", avgRating));

            ratingBar.setRating((float) avgRating);

            int count = location.getRatingCount() != null ? location.getRatingCount() : 0;
            tvRatingCount.setText(String.format(java.util.Locale.US, "(%d reviews)", count));

            // Setup Slider
            if (location.getPhotos() != null && !location.getPhotos().isEmpty()) {
                List<String> imageUrls = new ArrayList<>();
                for (Photo p : location.getPhotos()) {
                    imageUrls.add(p.getUrl());
                }
                vpImageSlider.setAdapter(new ImageSliderAdapter(imageUrls));
            }

            // Action Buttons logic
            btnCall.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + location.getPhone()));
                startActivity(intent);
            });

            btnWebsite.setOnClickListener(v -> {
                String url = location.getWebsite();
                if (url != null && !url.isEmpty()) {
                    // phải có http:// hoặc https://
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Không thể mở liên kết này", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            btnEmail.setOnClickListener(v -> {
                // Since 'email' isn't in your schema, we use a placeholder or check metadata
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@travel.com"));
                startActivity(intent);
            });

            tvSeeMore.setOnClickListener(v -> {
                if (tvDescription.getMaxLines() == 4) {
                    tvDescription.setMaxLines(Integer.MAX_VALUE);
                    tvSeeMore.setText("Show less");
                } else {
                    tvDescription.setMaxLines(4);
                    tvSeeMore.setText("See more");
                }
            });
        });
    }

    private void initViews(View v) {
        vpImageSlider = v.findViewById(R.id.vpImageSlider);
        vpImageSlider.setOffscreenPageLimit(1);
        tvName = v.findViewById(R.id.tvDetailName);
        tvDescription = v.findViewById(R.id.tvDescription);

        tvRatingScore = v.findViewById(R.id.tvDetailRatingScore);
        ratingBar = v.findViewById(R.id.ratingBar);
        tvRatingCount = v.findViewById(R.id.tvDetailRatingCount);

        btnCall = v.findViewById(R.id.btnCall);
        btnWebsite = v.findViewById(R.id.btnWebsite);
        btnEmail = v.findViewById(R.id.btnEmail);

        tvSeeMore = v.findViewById(R.id.tvSeeMore);
    }
}