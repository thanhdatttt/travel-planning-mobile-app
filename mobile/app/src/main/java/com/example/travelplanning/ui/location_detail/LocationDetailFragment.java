package com.example.travelplanning.ui.location_detail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.travelplanning.R;
import com.example.travelplanning.core.util.AndroidStringProvider;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.location.LocationHour;
import com.example.travelplanning.data.model.location.Photo;
import com.example.travelplanning.databinding.FragmentLocationDetailBinding;
import com.example.travelplanning.viewmodel.location_detail.LocationDetailViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LocationDetailFragment extends Fragment {
    private FragmentLocationDetailBinding binding;
    private LocationDetailViewModel viewModel;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLocationDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LocationDetailViewModel.class);
        setupObservers();

        if (getArguments() != null) {
            String locationId = getArguments().getString("location_id");
            if (locationId != null) {
                viewModel.fetchDetail(locationId);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setupObservers() {
        viewModel.getLocationDetail().observe(getViewLifecycleOwner(), location -> {
            if (location == null) {
                Log.d("DEBUG_DETAIL", "Location data is null");
                return;
            }
            Log.d("DEBUG_DETAIL", "Data received: " + location.getOpeningHours());
//
//            if (location.getPhotos() != null) {
//                Log.d("DEBUG_DETAIL", "Photo count: " + location.getPhotos().size());
//                for (Photo p : location.getPhotos()) {
//                    Log.d("DEBUG_DETAIL", "Photo URL: " + p.getUrl());
//                }
//            } else {
//                Log.e("DEBUG_DETAIL", "Photos list is NULL");
//            }

            // Mapping dữ liệu cơ bản qua binding
            binding.tvDetailName.setText(location.getName());
            binding.tvDescription.setText(location.getDescription());

            // Xử lý nút See More
            binding.tvDescription.post(() -> {
                if (binding.tvDescription.getLineCount() > 4) {
                    binding.tvSeeMore.setVisibility(View.VISIBLE);
                } else {
                    binding.tvSeeMore.setVisibility(View.GONE);
                }
            });

            // Rating
            double avgRating = location.getAvgRating() != null ? location.getAvgRating() : 0.0;
            binding.tvDetailRatingScore.setText(String.format(Locale.US, "%.1f", avgRating));
            binding.ratingBar.setRating((float) avgRating);
            binding.tvDetailRatingCount.setText(getString(R.string.reviews_count,
                    location.getRatingCount() != null ? location.getRatingCount() : 0));

            // Image Slider
            if (location.getPhotos() != null && !location.getPhotos().isEmpty()) {
                List<String> imageUrls = new ArrayList<>();
                for (Photo p : location.getPhotos()) imageUrls.add(p.getUrl());
                binding.vpImageSlider.setAdapter(new ImageSliderAdapter(imageUrls));
            }

            // RecyclerView Thông tin chi tiết
            setupInfoRecyclerView(location);

            // Action Buttons
            setupActionButtons(location);

            // See More toggle
            binding.tvSeeMore.setOnClickListener(v -> {
                if (binding.tvDescription.getMaxLines() == 4) {
                    binding.tvDescription.setMaxLines(Integer.MAX_VALUE);
                    binding.tvSeeMore.setText(getString(R.string.show_less));
                } else {
                    binding.tvDescription.setMaxLines(4);
                    binding.tvSeeMore.setText(getString(R.string.see_more));
                }
            });
        });
    }

    private void setupInfoRecyclerView(Location location) {
        List<LocationInfoItem> infoList = new ArrayList<>();
        int textColor = 0;
        int greenColor = Color.parseColor("#00B14F");
        int redColor = Color.RED;

        // 1. Giờ mở cửa: [Open/Closed] + Time
        LocationHour today = location.getTodayHours();
        if (today != null) {
            Calendar now = Calendar.getInstance();
            int currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

            boolean isOpen = currentMinutes >= today.getOpenTime() && currentMinutes <= today.getCloseTime();
            String statusPrefix = isOpen ? "Open · " : "Closed · ";
            int statusColor = isOpen ? greenColor : redColor;

            String timeText = String.format(Locale.getDefault(), "%s%s - %s",
                    statusPrefix, today.getFormattedOpen(), today.getFormattedClose());

            infoList.add(new LocationInfoItem(R.drawable.ic_clock, "Open hours", timeText, statusColor));
        }

        // 2. Địa chỉ
        infoList.add(new LocationInfoItem(R.drawable.ic_location, "Address", location.getAddress(), textColor));

        // 3. Điện thoại
        if (location.getPhone() != null) {
            infoList.add(new LocationInfoItem(R.drawable.ic_phone, "Phone number", location.getPhone(), textColor));
        }

        // 4. Mức giá
        if (location.getPriceLevel() != null){
            infoList.add(new LocationInfoItem(R.drawable.ic_money, "Price range", location.getPriceLevel().toString(), textColor));
        }

        // 5. Loại hình (Category)
        AndroidStringProvider stringProvider = new AndroidStringProvider(requireContext());
        infoList.add(new LocationInfoItem(R.drawable.ic_category, "Loại hình", stringProvider.getString(location.getCategorySlug()), textColor));

        // 6. Website
        if (location.getWebsite() != null) {
            infoList.add(new LocationInfoItem(R.drawable.ic_link, "Website", location.getWebsite(), greenColor));
        }

        binding.rvLocationInfo.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLocationInfo.setAdapter(new LocationInfoAdapter(infoList));
        binding.rvLocationInfo.setNestedScrollingEnabled(false);
    }

    private void setupActionButtons(Location location) {
        binding.btnCall.setOnClickListener(v -> {
            if (location.getPhone() != null) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + location.getPhone())));
            }
        });

        binding.btnWebsite.setOnClickListener(v -> {
            String url = location.getWebsite();
            if (url != null && !url.isEmpty()) {
                if (!url.startsWith("http")) url = "https://" + url;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.error_cannot_open_link), Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.btnEmail.setOnClickListener(v -> {
            String email = (location.getEmail() != null) ? location.getEmail() : "support@travel.com";
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}