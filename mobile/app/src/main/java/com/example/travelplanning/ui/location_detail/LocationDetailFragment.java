package com.example.travelplanning.ui.location_detail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.preference.PreferenceManager;

import com.example.travelplanning.R;
import com.example.travelplanning.core.util.AndroidStringProvider;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.model.location.LocationHour;
import com.example.travelplanning.data.model.location.Photo;
import com.example.travelplanning.data.model.review.RatingStat;
import com.example.travelplanning.databinding.FragmentLocationDetailBinding;
import com.example.travelplanning.databinding.LayoutReviewListBinding;
import com.example.travelplanning.ui.map.LocationAdapter;
import com.example.travelplanning.ui.review.ReviewAdapter;
import com.example.travelplanning.viewmodel.location_detail.LocationDetailViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LocationDetailFragment extends Fragment {
    private FragmentLocationDetailBinding binding;
    private LayoutReviewListBinding reviewListBinding;
    private LocationDetailViewModel viewModel;
    private PhotoAdapter photoAdapter;
    private ReviewAdapter reviewAdapter;
    private LocationAdapter nearbyAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLocationDetailBinding.inflate(inflater, container, false);
        reviewListBinding = binding.layoutReviewList;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LocationDetailViewModel.class);

        setupRecyclerViews();
        setupPhotoAreaLogic();
        setupObservers();
        setupBookmarkActions();

        if (getArguments() != null) {
            String locationId = getArguments().getString("location_id");
            if (locationId != null) {
                viewModel.fetchDetail(locationId);
            }
        }
    }

    private void setupRecyclerViews() {
        binding.rvLocationInfo.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLocationInfo.setNestedScrollingEnabled(false);

        photoAdapter = new PhotoAdapter();
        binding.layoutPhotos.rvPhotos.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.layoutPhotos.rvPhotos.setHasFixedSize(true);
        binding.layoutPhotos.rvPhotos.setAdapter(photoAdapter);

        //Review

        reviewAdapter = new ReviewAdapter();
        reviewListBinding.rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        reviewListBinding.rvReviews.setNestedScrollingEnabled(false); //để cuộn mượt trong ScrollView
        reviewListBinding.rvReviews.setAdapter(reviewAdapter);

        //Nearby
        nearbyAdapter = new LocationAdapter(location -> {
            // Chuyển sang Detail của địa điểm lân cận
            Bundle bundle = new Bundle();
            bundle.putString("location_id", location.getId());
            androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.nav_location_detail, bundle);
        });

        binding.rvNearbyPlaces.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvNearbyPlaces.setAdapter(nearbyAdapter);
    }

    private void setupPhotoAreaLogic() {
        var photoBinding = binding.layoutPhotos;

        photoBinding.btnAddPhotos.setOnClickListener(v -> {
            pickLocationPhoto.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        photoBinding.btnNext.setOnClickListener(v -> viewModel.nextPage());
        photoBinding.btnPrev.setOnClickListener(v -> viewModel.prevPage());
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

            setupMiniMap(location);

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

            setupActionButtons(location);

            binding.tvSeeMore.setOnClickListener(v -> {
                if (binding.tvDescription.getMaxLines() == 4) {
                    binding.tvDescription.setMaxLines(Integer.MAX_VALUE);
                    binding.tvSeeMore.setText(getString(R.string.show_less));
                } else {
                    binding.tvDescription.setMaxLines(4);
                    binding.tvSeeMore.setText(getString(R.string.see_more));
                }
            });
            updatePhotoList(location.getPhotos(), viewModel.getCurrentPage().getValue());
        });

        // Quan sát riêng trang hiện tại để chuyển ảnh
        viewModel.getCurrentPage().observe(getViewLifecycleOwner(), page -> {
            Location loc = viewModel.getLocationDetail().getValue();
            if (loc != null) updatePhotoList(loc.getPhotos(), page);
        });

        // Quan sát danh sách Review chi tiết
        viewModel.getReviews().observe(getViewLifecycleOwner(), reviews -> {
            if (reviews != null) {
                Log.d("DEBUG_REVIEW", "Reviews received: " + reviews.size());
                reviewAdapter.setReviews(reviews);
            }

            if (viewModel.isLastPage()) {
                reviewListBinding.btnLoadMoreReviews.setVisibility(View.GONE);
            } else {
                reviewListBinding.btnLoadMoreReviews.setVisibility(View.VISIBLE);
            }
            reviewListBinding.pbLoadMore.setVisibility(View.GONE);
        });

        viewModel.getReviewStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                updateReviewStatsUI(stats);
            }
        });

        viewModel.getNearbyLocations().observe(getViewLifecycleOwner(), locations -> {
            if (locations != null) {
                Location currentDetail = viewModel.getLocationDetail().getValue();
                GeoPoint referencePoint = null;

                if (currentDetail != null && currentDetail.getLatitude() != null) {
                    referencePoint = new GeoPoint(currentDetail.getLatitude(), currentDetail.getLongitude());
                }

                nearbyAdapter.updateData(locations, referencePoint);
            }
        });
    }

    private void setupInfoRecyclerView(Location location) {
        List<LocationInfoItem> infoList = new ArrayList<>();
        int textColor = 0;
        int greenColor = Color.parseColor("#00B14F");
        int redColor = Color.RED;

        // Giờ mở cửa: [Open/Closed] + Time
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

        // Địa chỉ
        infoList.add(new LocationInfoItem(R.drawable.ic_location, "Address", location.getAddress(), textColor));

        // Điện thoại
        if (location.getPhone() != null) {
            infoList.add(new LocationInfoItem(R.drawable.ic_phone, "Phone number", location.getPhone(), textColor));
        }

        // Mức giá
        if (location.getPriceLevel() != null){
            infoList.add(new LocationInfoItem(R.drawable.ic_money, "Price range", location.getPriceLevel().toString(), textColor));
        }

        // Loại hình (Category)
        AndroidStringProvider stringProvider = new AndroidStringProvider(requireContext());
        infoList.add(new LocationInfoItem(R.drawable.ic_category, "Loại hình", stringProvider.getString(location.getCategorySlug()), textColor));

        // Website
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

        //
        reviewListBinding.btnLoadMoreReviews.setOnClickListener(v -> {
            String locationId = getArguments().getString("location_id");
            if (locationId != null) {
                viewModel.fetchReviews(locationId, true);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupMiniMap(Location location) {
        if (location.getLatitude() == null || location.getLongitude() == null) return;

        var mapBinding = binding.layoutMiniMap;

        mapBinding.mapDetail.setTileSource(TileSourceFactory.MAPNIK);
        mapBinding.mapDetail.setMultiTouchControls(false); // Vô hiệu hóa để ưu tiên cuộn trang Detail

        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapBinding.mapDetail.getController().setZoom(17.5);
        mapBinding.mapDetail.getController().setCenter(startPoint);

        // Marker
        mapBinding.mapDetail.getOverlays().clear();
        Marker marker = new Marker(mapBinding.mapDetail);
        marker.setPosition(startPoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        // Bạn có thể set icon tùy chỉnh theo category tại đây
        mapBinding.mapDetail.getOverlays().add(marker);

        mapBinding.touchOverlay.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Ngăn ScrollView cha chặn sự kiện
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            // Để MapView bên dưới vẫn nhận được sự kiện chạm
            mapBinding.mapDetail.dispatchTouchEvent(event);
            return true;
        });

        // Sự kiện khi bấm vào mũi tên để qua bản đồ lớn
        binding.layoutMiniMap.layoutHeaderMap.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            // Truyền ID để NearbyFragment xử lý
            bundle.putString("location_id", location.getId());

            // Điều hướng sang màn hình Nearby
            androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.nav_nearby, bundle);
        });

        mapBinding.mapDetail.invalidate(); // Vẽ lại bản đồ
    }


    // LOCATION PHOTOS------------------
    private final ActivityResultLauncher<PickVisualMediaRequest> pickLocationPhoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null && viewModel.getLocationDetail().getValue() != null) {
                    viewModel.uploadPhoto(viewModel.getLocationDetail().getValue().getId(), uri);
                }
            });

    private void updatePhotoList(List<Photo> allPhotos, int page) {
        if (allPhotos == null || allPhotos.isEmpty()) {
            binding.layoutPhotos.getRoot().setVisibility(View.GONE);
            return;
        }
        binding.layoutPhotos.getRoot().setVisibility(View.VISIBLE);

        // Logic phân trang: mỗi lần hiện 2 ảnh (vì ta muốn 2 ảnh full màn hình)
        int ITEMS_PER_PAGE = 2;
        int fromIndex = page * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allPhotos.size());

        if (fromIndex < allPhotos.size()) {
            List<Photo> pagedList = new ArrayList<>(allPhotos.subList(fromIndex, toIndex));
            photoAdapter.setList(pagedList);
        }

        int totalPages = (int) Math.ceil((double) allPhotos.size() / ITEMS_PER_PAGE);
        binding.layoutPhotos.tvPageIndicator.setText((page + 1) + "/" + totalPages);

        // Cập nhật trạng thái nút
        binding.layoutPhotos.btnPrev.setAlpha(page > 0 ? 1.0f : 0.3f);
        binding.layoutPhotos.btnNext.setAlpha(page < totalPages - 1 ? 1.0f : 0.3f);
    }

    // REVIEW
    private void updateReviewStatsUI(List<RatingStat> stats) {
        var summaryBinding = binding.layoutReviewSummary;
        int totalReviews = 0;
        double totalPoints = 0;

        // Tính toán con số thực tế từ kết quả API Stats trả về
        for (RatingStat stat : stats) {
            totalReviews += stat.getCount();
            totalPoints += (stat.getRating() * stat.getCount());
        }

        if (totalReviews > 0) {
            double average = totalPoints / totalReviews;

            // Update điểm số (ví dụ: 4.7)
            binding.tvDetailRatingScore.setText(String.format(Locale.US, "%.1f", average));
            // Update sao vàng
            binding.ratingBar.setRating((float) average);
            // Update số lượng (ví dụ: (7 reviews))
            String countStr = "(" + totalReviews + " " + getString(R.string.reviews) + ")";
            binding.tvDetailRatingCount.setText(countStr);

            // Cập nhật phần Summary phía dưới
            summaryBinding.tvTotalReviews.setText(countStr);
            summaryBinding.tvAverageRating.setText(String.format(Locale.US, "%.1f", average));
            summaryBinding.miniRatingBar.setRating((float) average);
        } else {
            binding.tvDetailRatingScore.setText("0.0");
            binding.ratingBar.setRating(0f);
            binding.tvDetailRatingCount.setText("(0 reviews)");
        }


        // Gán tổng số lượng vào TextView trong summary
        summaryBinding.tvTotalReviews.setText("(" + totalReviews + ")");

        // Duyệt qua danh sách stats (Backend trả về 1-5 sao)
        for (RatingStat stat : stats) {
            int progress = (stat.getCount() * 100) / totalReviews;
            switch (stat.getRating()) {
                case 5: summaryBinding.pbStar5.setProgress(progress); break;
                case 4: summaryBinding.pbStar4.setProgress(progress); break;
                case 3: summaryBinding.pbStar3.setProgress(progress); break;
                case 2: summaryBinding.pbStar2.setProgress(progress); break;
                case 1: summaryBinding.pbStar1.setProgress(progress); break;
            }
        }
    }

    private void setupBookmarkActions() {
        String locationId = getArguments() != null ? getArguments().getString("location_id") : null;

        if (locationId == null) return;

        binding.btnBookmark.setOnClickListener(v -> {
            viewModel.toggleBookmark(locationId);
        });

        viewModel.getIsBookmarked().observe(getViewLifecycleOwner(), bookmarked -> {
            if (bookmarked) {
                binding.btnBookmark.setImageResource(R.drawable.ic_heart);
                binding.btnBookmark.setColorFilter(Color.GRAY);
            } else {
                binding.btnBookmark.setImageResource(R.drawable.ic_heart_outline);
                binding.btnBookmark.setColorFilter(Color.GRAY);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null) binding.layoutMiniMap.mapDetail.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null) binding.layoutMiniMap.mapDetail.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}