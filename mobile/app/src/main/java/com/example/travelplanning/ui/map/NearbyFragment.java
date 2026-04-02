package com.example.travelplanning.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.remote.map.dto.response.PhotonResponse;
import com.example.travelplanning.databinding.FragmentNearbyBinding;
import com.example.travelplanning.viewmodel.location.LocationViewModel;
import com.example.travelplanning.viewmodel.map.MapViewModel;
import com.example.travelplanning.viewmodel.map.NearbyViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class NearbyFragment extends Fragment {

    private FragmentNearbyBinding binding;
    private MapViewModel mapViewModel;
    private LocationViewModel locationViewModel;
    private NearbyViewModel nearbyViewModel; // View Model Mới

    private MapView mapView;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private FusedLocationProviderClient fusedLocationClient;
    
    private SuggestAdapter customAdapter;
    private final List<String> currentDisplayNames = new ArrayList<>(); // Danh sách local cho Adapter
    private List<PhotonResponse.Feature> currentSuggestions = new ArrayList<>();

    private final Handler mapHandler = new Handler(Looper.getMainLooper());
    private Runnable mapRunnable;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    getUserLocationAndFetchNearby();
                } else {
                    Toast.makeText(requireContext(), "Cần quyền vị trí để hiển thị quanh đây!", Toast.LENGTH_LONG).show();
                    mapController.setCenter(new GeoPoint(10.7769, 106.7009));
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        binding = FragmentNearbyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo cả 3 ViewModel
        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        nearbyViewModel = new ViewModelProvider(this).get(NearbyViewModel.class);

        mapView = binding.mapView;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupMap();
        setupAutocomplete();
        setupUIFeatures();
        observeData();

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        mapController = mapView.getController();
        mapView.setFlingEnabled(true);
        mapController.setZoom(16.0);

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        mapView.addMapListener(new org.osmdroid.events.MapListener() {
            @Override
            public boolean onScroll(org.osmdroid.events.ScrollEvent event) {
                scheduleFetch();
                return true;
            }
            @Override
            public boolean onZoom(org.osmdroid.events.ZoomEvent event) {
                scheduleFetch();
                return true;
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getUserLocationAndFetchNearby() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                GeoPoint userPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setZoom(16.0);
                mapController.animateTo(userPoint);
            }
        });
    }

    private void setupUIFeatures() {
        // 1. TẮT BÀN PHÍM VÀ FOCUS KHI BẤM NÚT BACK (Sửa lại thứ tự)
        binding.btnBack.setOnClickListener(v -> {
            hideKeyboard(); // Đóng bàn phím trước
            binding.editTextSearch.clearFocus(); // Gỡ focus sau
            
            // Nút back này nếu bố muốn nó quay về màn hình trước (Home) thì thêm dòng này:
            // requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        // Delegate sự kiện Click sang ViewModel
        View.OnClickListener chipClickListener = v -> {
            String targetIcon = "";
            if (v.getId() == R.id.chipFood) targetIcon = "ic_category_food";
            else if (v.getId() == R.id.chipHotel) targetIcon = "ic_category_hotel";
            else if (v.getId() == R.id.chipAttraction) targetIcon = "ic_category_attraction";
            else if (v.getId() == R.id.chipShop) targetIcon = "ic_category_shop";
            else if (v.getId() == R.id.chipService) targetIcon = "ic_category_service";

            nearbyViewModel.toggleCategory(targetIcon);
        };

        binding.chipFood.setOnClickListener(chipClickListener);
        binding.chipHotel.setOnClickListener(chipClickListener);
        binding.chipAttraction.setOnClickListener(chipClickListener);
        binding.chipShop.setOnClickListener(chipClickListener);
        binding.chipService.setOnClickListener(chipClickListener);
    }

    private void observeData() {
        // --- 1. LUỒNG DỮ LIỆU TỪ SERVER CHẢY VÀO NEARBY_VIEWMODEL ---
        locationViewModel.getNearbyLocations().observe(getViewLifecycleOwner(), locations -> {
            nearbyViewModel.setLocations(locations);
        });

        locationViewModel.getSearchResults().observe(getViewLifecycleOwner(), locations -> {
            nearbyViewModel.setSearchSuggestions(locations);
        });

        // --- 2. LUỒNG DỮ LIỆU TỪ NEARBY_VIEWMODEL CHẢY RA UI ---
        
        // Cập nhật lại bản đồ mỗi khi danh sách lọc thay đổi
        nearbyViewModel.getFilteredLocations().observe(getViewLifecycleOwner(), this::drawCustomMarkers);

        // Cập nhật lại UI các Nút Chip (Độ sáng mờ)
        nearbyViewModel.getSelectedCategoryIcon().observe(getViewLifecycleOwner(), selectedIcon -> {
            binding.chipFood.setAlpha(selectedIcon == null || selectedIcon.equals("ic_category_food") ? 1.0f : 0.5f);
            binding.chipHotel.setAlpha(selectedIcon == null || selectedIcon.equals("ic_category_hotel") ? 1.0f : 0.5f);
            binding.chipAttraction.setAlpha(selectedIcon == null || selectedIcon.equals("ic_category_attraction") ? 1.0f : 0.5f);
            binding.chipShop.setAlpha(selectedIcon == null || selectedIcon.equals("ic_category_shop") ? 1.0f : 0.5f);
            binding.chipService.setAlpha(selectedIcon == null || selectedIcon.equals("ic_category_service") ? 1.0f : 0.5f);
        });

        // Đổ dữ liệu vào Dropdown tìm kiếm
        nearbyViewModel.getDisplaySearchNames().observe(getViewLifecycleOwner(), names -> {
            currentDisplayNames.clear();
            if (names != null) currentDisplayNames.addAll(names);
            customAdapter.notifyDataSetChanged();
            if (!currentDisplayNames.isEmpty() && binding.editTextSearch.hasFocus()) {
                binding.editTextSearch.showDropDown();
            }
        });

        // Giữ lại Observer của Photon
        mapViewModel.getAutocompleteResults().observe(getViewLifecycleOwner(), features -> {
            currentSuggestions.clear();
            currentSuggestions.addAll(features);
            currentDisplayNames.clear();
            for (PhotonResponse.Feature feature : features) {
                String name = feature.properties.name != null ? feature.properties.name : feature.properties.street;
                String city = feature.properties.city != null ? " (" + feature.properties.city + ")" : "";
                if (name != null) currentDisplayNames.add(name + city);
            }
            customAdapter.notifyDataSetChanged();
            if (!currentDisplayNames.isEmpty()) binding.editTextSearch.showDropDown();
        });
    }

    private void drawCustomMarkers(List<Location> locations) {
        mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker);

        if (locations != null) {
            for (Location loc : locations) {
                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
                marker.setTitle(loc.getName());
                marker.setIcon(getIconForCategory(loc.getCategoryIcon()));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                marker.setOnMarkerClickListener((m, map) -> {
                    mapController.animateTo(m.getPosition());
                    showLocationPreview(loc);
                    return true;
                });
                mapView.getOverlays().add(marker);
            }
        }
        mapView.invalidate();
    }

    private void fetchNearbyFromMap() {
        double currentLat = mapView.getMapCenter().getLatitude();
        double currentLng = mapView.getMapCenter().getLongitude();
        double currentZoom = mapView.getZoomLevelDouble();
        
        // Delegate logic tính toán bán kính sang ViewModel
        int radius = nearbyViewModel.calculateRadiusFromZoom(currentZoom);

        locationViewModel.fetchNearbyLocations(currentLat, currentLng, radius, null);
    }

    private void scheduleFetch() {
        if (mapRunnable != null) mapHandler.removeCallbacks(mapRunnable);
        mapRunnable = this::fetchNearbyFromMap;
        mapHandler.postDelayed(mapRunnable, 800);
    }

    private void setupAutocomplete() {
        customAdapter = new SuggestAdapter(requireContext(), currentDisplayNames);
        binding.editTextSearch.setAdapter(customAdapter);

        binding.editTextSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
            }
            @Override public void afterTextChanged(android.text.Editable s) {
                String keyword = s.toString().trim();
                if (keyword.length() >= 2) {
                    if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                    searchRunnable = () -> locationViewModel.searchLocations(keyword, null, null, 1, 5);
                    searchHandler.postDelayed(searchRunnable, 500);
                } else {
                    currentDisplayNames.clear();
                    customAdapter.notifyDataSetChanged();
                }
            }
        });

        binding.editTextSearch.setOnItemClickListener((parent, v, position, id) -> {
            List<Location> dbSuggestions = nearbyViewModel.getCurrentDbSuggestions().getValue();
            if (dbSuggestions != null && position < dbSuggestions.size()) {
                Location selectedLoc = dbSuggestions.get(position);
                
                binding.editTextSearch.setText(selectedLoc.getName());
                binding.editTextSearch.setSelection(selectedLoc.getName().length());
                binding.editTextSearch.dismissDropDown();
                hideKeyboard();
                
                onLocationSelected(selectedLoc);
            }
        });

        binding.editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                executeSearch();
                return true;
            }
            return false;
        });
    }

    private void showLocationPreview(Location loc) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_location_preview, null);

        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvRating = view.findViewById(R.id.tvRating);
        TextView tvAddress = view.findViewById(R.id.tvAddress);
        TextView tvDistance = view.findViewById(R.id.tvDistance);
        ImageView imgLocation = view.findViewById(R.id.imgLocation);

        tvName.setText(loc.getName() != null ? loc.getName() : "Chưa có tên");
        tvAddress.setText(loc.getAddress() != null ? loc.getAddress() : "Chưa có địa chỉ");

        double rating = loc.getAvgRating() != null ? loc.getAvgRating() : 0.0;
        int count = loc.getRatingCount() != null ? loc.getRatingCount() : 0;
        tvRating.setText("⭐ " + rating + " (" + count + ")");

        Double displayDistance = null;
        GeoPoint myActualLocation = myLocationOverlay != null ? myLocationOverlay.getMyLocation() : null;

        if (myActualLocation != null && loc.getLatitude() != null && loc.getLongitude() != null) {
            GeoPoint placeLocation = new GeoPoint(loc.getLatitude(), loc.getLongitude());
            displayDistance = myActualLocation.distanceToAsDouble(placeLocation);
        } else if (loc.getDistance() != null) {
            displayDistance = loc.getDistance();
        }

        if (displayDistance != null) {
            String distanceStr = displayDistance > 1000
                    ? String.format("%.1f km", displayDistance / 1000)
                    : Math.round(displayDistance) + " m";
            tvDistance.setText("Cách đây " + distanceStr);
            tvDistance.setVisibility(View.VISIBLE);
        } else {
            tvDistance.setVisibility(View.GONE);
        }

        if (loc.getImageUrl() != null && !loc.getImageUrl().isEmpty()) {
            Glide.with(this).load(loc.getImageUrl()).into(imgLocation);
        } else {
            imgLocation.setImageResource(R.drawable.ic_placeholder);
        }

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void onLocationSelected(Location selectedLoc) {
        if (selectedLoc.getLatitude() == null || selectedLoc.getLongitude() == null) {
            Toast.makeText(requireContext(), "Địa điểm này chưa có tọa độ cụ thể!", Toast.LENGTH_SHORT).show();
            showLocationPreview(selectedLoc);
            return;
        }
        GeoPoint targetPoint = new GeoPoint(selectedLoc.getLatitude(), selectedLoc.getLongitude());
        mapController.setZoom(18.0);
        mapController.animateTo(targetPoint);
        showLocationPreview(selectedLoc);
    }

    private void executeSearch() {
        String keyword = binding.editTextSearch.getText().toString().trim();
        if (!keyword.isEmpty()) {
            hideKeyboard();
            binding.editTextSearch.dismissDropDown();
            binding.editTextSearch.clearFocus();

            org.osmdroid.util.BoundingBox box = mapView.getBoundingBox();
            String bboxString = box.getLonWest() + "," + box.getLatSouth() + "," + box.getLonEast() + "," + box.getLatNorth();
            double centerLat = mapView.getMapCenter().getLatitude();
            double centerLon = mapView.getMapCenter().getLongitude();

            mapViewModel.performSearch(keyword, centerLat, centerLon, bboxString);
            Toast.makeText(requireContext(), "Đang tìm...", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        // Ép nó đóng bàn phím của đúng cái ô nhập liệu này
        imm.hideSoftInputFromWindow(binding.editTextSearch.getWindowToken(), 0);
    }

    private Drawable getIconForCategory(String iconName) {
        int resId = R.drawable.ic_map_default;
        if (iconName != null) {
            switch (iconName) {
                case "ic_category_food": resId = R.drawable.ic_category_food; break;
                case "ic_category_hotel": resId = R.drawable.ic_category_hotel; break;
                case "ic_category_attraction": resId = R.drawable.ic_category_attraction; break;
                case "ic_category_shop": resId = R.drawable.ic_category_shop; break;
                case "ic_category_service": resId = R.drawable.ic_category_service; break;
            }
        }
        return ContextCompat.getDrawable(requireContext(), resId);
    }

    @Override
    public void onResume() { super.onResume(); if (mapView != null) mapView.onResume(); }
    @Override
    public void onPause() { super.onPause(); if (mapView != null) mapView.onPause(); }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) mapView.onDetach();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        binding = null;
    }

    private class SuggestAdapter extends ArrayAdapter<String> {
        private final List<String> items;
        public SuggestAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, android.R.layout.simple_dropdown_item_1line, objects);
            this.items = objects;
        }

        @NonNull
        @Override
        public android.widget.Filter getFilter() {
            return new android.widget.Filter() {
                @Override protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = items;
                    filterResults.count = items.size();
                    return filterResults;
                }
                @Override protected void publishResults(CharSequence constraint, FilterResults results) {
                    notifyDataSetChanged();
                }
            };
        }
    }
}