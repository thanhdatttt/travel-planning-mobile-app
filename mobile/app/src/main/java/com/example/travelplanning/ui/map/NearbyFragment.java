package com.example.travelplanning.ui.map;
import android.Manifest; 
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat; 
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelplanning.R; 
import com.example.travelplanning.databinding.FragmentMapBinding;
import com.example.travelplanning.data.remote.map.dto.response.PhotonResponse;
import com.example.travelplanning.viewmodel.location.LocationViewModel;
import com.example.travelplanning.viewmodel.map.MapViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import com.example.travelplanning.data.model.location.Location;
import java.util.ArrayList;
import java.util.List;

public class NearbyFragment extends Fragment {
    private List<Location> currentDbSuggestions = new ArrayList<>();
    private final Handler mapHandler = new Handler(Looper.getMainLooper());
    private Runnable mapRunnable;
    private double lastLat = 0, lastLng = 0; // Để check xem map có dịch chuyển đáng kể không

    private FragmentMapBinding binding;
    private MapViewModel mapViewModel;            // Phục vụ Search Photon cũ
    private LocationViewModel locationViewModel;  // Phục vụ lấy Data Nearby từ Server của bạn
    
    private MapView mapView;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay; // Dùng để hiển thị chấm xanh GPS
    private FusedLocationProviderClient fusedLocationClient; // Lấy tọa độ GPS
    private SuggestAdapter customAdapter;
    // Biến cho Autocomplete cũ
    private ArrayAdapter<String> autocompleteAdapter;
    private List<PhotonResponse.Feature> currentSuggestions = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private List<String> displayList = new ArrayList<>();

    // 1. KHỞI TẠO LAUNCHER XIN QUYỀN GPS TRƯỚC
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    getUserLocationAndFetchNearby(); // Có quyền thì lấy GPS
                } else {
                    Toast.makeText(requireContext(), "Cần quyền vị trí để hiển thị quanh đây!", Toast.LENGTH_LONG).show();
                    // Nếu không có quyền, load map mặc định ở trung tâm thành phố
                    GeoPoint defaultPoint = new GeoPoint(10.7769, 106.7009); // TP.HCM
                    mapController.setCenter(defaultPoint);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Khởi tạo cả 2 ViewModel
        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        
        mapView = binding.mapView;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupMap();
        setupAutocomplete(); // Giữ nguyên chức năng search cũ
        observeData();       // Lắng nghe dữ liệu Nearby

        // 2. YÊU CẦU QUYỀN VỊ TRÍ KHI MỞ MÀN HÌNH
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        // Xử lý sự kiện bàn phím cho thanh Search cũ
        binding.editTextSearch.setOnItemClickListener((parent, v, position, id) -> {
            if (position < currentDbSuggestions.size()) {
                // 1. Lấy đúng object Location người dùng vừa chọn
                Location selectedLoc = currentDbSuggestions.get(position);

                // 2. Điền tên vào ô nhập liệu
                binding.editTextSearch.setText(selectedLoc.getName());
                binding.editTextSearch.setSelection(selectedLoc.getName().length());
                binding.editTextSearch.dismissDropDown();
                hideKeyboard();

                // 3. Gọi hàm bay đến vị trí và hiện BottomSheet
                onLocationSelected(selectedLoc);
            }
        });
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        mapController = mapView.getController();
        mapView.setFlingEnabled(true);
        mapController.setZoom(16.0); // Zoom gần hơn một chút để xem Nearby

        // Lớp hiển thị vị trí người dùng (Chấm xanh)
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        mapView.addMapListener(new org.osmdroid.events.MapListener() {
            @Override
            public boolean onScroll(org.osmdroid.events.ScrollEvent event) {
                scheduleFetch(); // Khi lướt map cũng tìm địa điểm mới
                return true;
            }

            @Override
            public boolean onZoom(org.osmdroid.events.ZoomEvent event) {
                scheduleFetch(); // Khi zoom cũng tính lại radius
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
                // Không cần gọi locationViewModel trực tiếp ở đây nữa 
                // vì animateTo sẽ kích hoạt onScroll/onZoom -> tự gọi fetch.
            }
        });
    }

    private void observeData() {
        // 1. Lắng nghe dữ liệu Nearby để VẼ MARKER (Đoạn này lúc nãy bị mất)
        locationViewModel.getNearbyLocations().observe(getViewLifecycleOwner(), locations -> {
            if (locations != null && !locations.isEmpty()) {
                drawCustomMarkers(locations);
            }
        });

        // 2. Lắng nghe dữ liệu Search để ĐỔ VÀO DROPDOWN
        locationViewModel.getSearchResults().observe(getViewLifecycleOwner(), locations -> {
            if (locations != null) {
                currentDbSuggestions.clear();
                currentDbSuggestions.addAll(locations); // Lưu lại object để dùng khi click

                displayList.clear();
                for (Location loc : locations) {
                    // Hiện tên và địa chỉ cho dễ nhìn
                    String address = loc.getAddress() != null ? " - " + loc.getAddress() : "";
                    displayList.add(loc.getName() + address); 
                }
                customAdapter.notifyDataSetChanged();
                
                if (!displayList.isEmpty()) {
                    binding.editTextSearch.showDropDown();
                }
            }
        });
    }

    // 4. VẼ ICON TÙY BIẾN THEO CATEGORY LÊN BẢN ĐỒ
    private void drawCustomMarkers(List<com.example.travelplanning.data.model.location.Location> locations) {
        // Xóa các marker cũ (nhưng phải giữ lại MyLocationOverlay chấm xanh)
        mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker);

        for (Location loc : locations) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
            marker.setTitle(loc.getName());
            
            // Gán Icon dựa theo ID
            marker.setIcon(getIconForCategory(loc.getCategoryIcon()));
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Sự kiện click vào Icon
            marker.setOnMarkerClickListener((m, map) -> {
                // 1. Zoom nhẹ vào địa điểm để tạo cảm giác tập trung
                mapController.animateTo(m.getPosition());
                
                // 2. Hiện BottomSheet thay vì InfoWindow
                showLocationPreview(loc); 
                
                return true; // Trả về true để không hiện cái bảng trắng InfoWindow mặc định nữa
            });

            mapView.getOverlays().add(marker);
        }
        mapView.invalidate(); // Vẽ lại bản đồ
    }

    // HÀM HELPER: Chọn XML Drawable dựa trên Category ID
    private Drawable getIconForCategory(String iconName) {
        int resId = R.drawable.ic_map_default; // Bố nhớ đảm bảo có file ic_map_default.xml nhé
        
        if (iconName != null) {
            switch (iconName) {
                case "ic_category_food": 
                    resId = R.drawable.ic_category_food; 
                    break;
                case "ic_category_hotel": 
                    resId = R.drawable.ic_category_hotel; 
                    break;
                case "ic_category_attraction": 
                    resId = R.drawable.ic_category_attraction; 
                    break;
                case "ic_category_shop": 
                    resId = R.drawable.ic_category_shop; 
                    break;
                case "ic_category_service": 
                    resId = R.drawable.ic_category_service; 
                    break;
            }
        }
        return ContextCompat.getDrawable(requireContext(), resId);
    }

    private void setupAutocomplete() {
        customAdapter = new SuggestAdapter(requireContext(), displayList);
        binding.editTextSearch.setAdapter(customAdapter);

        binding.editTextSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String keyword = s.toString().trim();
                if (keyword.length() >= 2) {
                    if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                    
                    searchRunnable = () -> {
                        // Thay vì gọi Photon, ta gọi API search của chính mình
                        // Page = 1, Limit = 5 (chỉ lấy vài gợi ý hiện lên dropdown)
                        locationViewModel.searchLocations(keyword, null, null, 1, 5);
                    };
                    searchHandler.postDelayed(searchRunnable, 500);
                } else {
                    displayList.clear();
                    // Clear danh sách cũ
                }
            }
        });

        binding.editTextSearch.setOnItemClickListener((parent, view, position, id) -> {
            if (position < currentSuggestions.size()) {
                // 1. Lấy tên địa điểm được chọn
                String selectedName = displayList.get(position);

                // 2. Điền tên đó vào ô nhập liệu
                binding.editTextSearch.setText(selectedName);

                // 3. Di chuyển con trỏ chuột xuống cuối dòng chữ
                binding.editTextSearch.setSelection(selectedName.length());

                // 4. Đóng danh sách gợi ý
                binding.editTextSearch.dismissDropDown();

                // ĐẶC BIỆT: Không gọi pinLocationOnMap ở đây nữa.
                // Người dùng phải bấm nút SEARCH trên bàn phím mới bắt đầu tìm.
            }
        });

        mapViewModel.getAutocompleteResults().observe(getViewLifecycleOwner(), features -> {
            currentSuggestions.clear();
            currentSuggestions.addAll(features);

            displayList.clear();
            for (PhotonResponse.Feature feature : features) {
                String name = feature.properties.name != null ? feature.properties.name : feature.properties.street;
                String city = feature.properties.city != null ? " (" + feature.properties.city + ")" : "";

                if (name != null) displayList.add(name + city);
            }

            customAdapter.notifyDataSetChanged();

            if (!displayList.isEmpty()) {
                binding.editTextSearch.showDropDown();
            }
        });
    }

    private void pinLocationOnMap(PhotonResponse.Feature feature) {
        if (feature.geometry != null && feature.geometry.coordinates != null && feature.geometry.coordinates.size() >= 2) {
            mapView.getOverlays().clear();

            double lng = feature.geometry.coordinates.get(0);
            double lat = feature.geometry.coordinates.get(1);
            GeoPoint placeLocation = new GeoPoint(lat, lng);

            Marker marker = new Marker(mapView);
            marker.setPosition(placeLocation);

            String placeName = feature.properties.name != null ? feature.properties.name : feature.properties.street;
            marker.setTitle(placeName != null ? placeName : "Địa điểm đã chọn");
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            mapView.getOverlays().add(marker);
            mapView.invalidate();

            mapController.animateTo(placeLocation);
            mapController.setZoom(17.5);
            marker.showInfoWindow();
        } else {
            Toast.makeText(requireContext(), "Địa điểm này không có tọa độ cụ thể!", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeSearch() {
        String keyword = binding.editTextSearch.getText().toString().trim();
        if (!keyword.isEmpty()) {
            hideKeyboard();
            binding.editTextSearch.dismissDropDown();
            binding.editTextSearch.clearFocus();

            // LẤY TỌA ĐỘ MÀN HÌNH HIỆN TẠI LÀM RANH GIỚI
            org.osmdroid.util.BoundingBox box = mapView.getBoundingBox();
            String bboxString = box.getLonWest() + "," + box.getLatSouth() + "," + box.getLonEast() + "," + box.getLatNorth();
            double centerLat = mapView.getMapCenter().getLatitude();
            double centerLon = mapView.getMapCenter().getLongitude();

            // Truyền vào ViewModel
            mapViewModel.performSearch(keyword, centerLat, centerLon, bboxString);
            Toast.makeText(requireContext(), "Đang tìm...", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = items;
                    filterResults.count = items.size();
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    notifyDataSetChanged();
                }
            };
        }
    }

    // 1. Hàm quy đổi Zoom Level -> Radius (mét)
    private int calculateRadiusFromZoom(double zoomLevel) {
        if (zoomLevel >= 17) return 500;      // Zoom rất cận: 500m
        if (zoomLevel >= 15) return 2000;     // Zoom gần: 2km
        if (zoomLevel >= 13) return 5000;     // Zoom trung bình: 5km
        if (zoomLevel >= 11) return 15000;    // Zoom xa: 15km
        return 50000;                         // Zoom rất xa: 50km
    }

    // 2. Hàm fetch data dựa trên trạng thái hiện tại của Map
    private void fetchNearbyFromMap() {
        double currentLat = mapView.getMapCenter().getLatitude();
        double currentLng = mapView.getMapCenter().getLongitude();
        double currentZoom = mapView.getZoomLevelDouble();
        
        int radius = calculateRadiusFromZoom(currentZoom);

        // Chỉ gọi API nếu tọa độ thay đổi đáng kể hoặc zoom thay đổi
        // (Ở đây con cứ cho gọi mỗi khi dừng thao tác để bố thấy kết quả ngay)
        locationViewModel.fetchNearbyLocations(currentLat, currentLng, radius, null);
        
        lastLat = currentLat;
        lastLng = currentLng;
    }

    // Hàm hỗ trợ delay 800ms sau khi người dùng ngừng thao tác
    private void scheduleFetch() {
        if (mapRunnable != null) mapHandler.removeCallbacks(mapRunnable);
        mapRunnable = this::fetchNearbyFromMap;
        mapHandler.postDelayed(mapRunnable, 800); 
    }

    private void showLocationPreview(Location loc) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_location_preview, null);
        
        // Ánh xạ View
        android.widget.TextView tvName = view.findViewById(R.id.tvName);
        android.widget.TextView tvRating = view.findViewById(R.id.tvRating);
        android.widget.TextView tvAddress = view.findViewById(R.id.tvAddress);
        android.widget.TextView tvDistance = view.findViewById(R.id.tvDistance);
        android.widget.ImageView imgLocation = view.findViewById(R.id.imgLocation);

        // 1. Set dữ liệu cơ bản (Check null cho an toàn)
        tvName.setText(loc.getName() != null ? loc.getName() : "Chưa có tên");
        tvAddress.setText(loc.getAddress() != null ? loc.getAddress() : "Chưa có địa chỉ");
        
        // 2. An toàn với Rating
        double rating = loc.getAvgRating() != null ? loc.getAvgRating() : 0.0;
        int count = loc.getRatingCount() != null ? loc.getRatingCount() : 0;
        tvRating.setText("⭐ " + rating + " (" + count + ")");
        
        // 3. FIX LỖI VĂNG APP CHỖ NÀY: Check null cho Distance
        if (loc.getDistance() != null) {
            String distanceStr = loc.getDistance() > 1000 
                ? String.format("%.1f km", loc.getDistance() / 1000) 
                : Math.round(loc.getDistance()) + " m";
            tvDistance.setText("Cách đây " + distanceStr);
            tvDistance.setVisibility(View.VISIBLE);
        } else {
            tvDistance.setVisibility(View.GONE); // Nếu search không có khoảng cách thì ẩn dòng này đi
        }

        // 4. Dùng Glide để load ảnh 
        if (loc.getImageUrl() != null && !loc.getImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this).load(loc.getImageUrl()).into(imgLocation);
        } else {
            imgLocation.setImageResource(R.drawable.ic_placeholder); // Ảnh mặc định nếu không có url
        }

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void onLocationSelected(Location selectedLoc) {
        GeoPoint targetPoint = new GeoPoint(selectedLoc.getLatitude(), selectedLoc.getLongitude());
        mapController.setZoom(18.0);
        mapController.animateTo(targetPoint);
        
        // Hiện ngay BottomSheet thông tin địa điểm đó luôn cho xịn
        showLocationPreview(selectedLoc);
    }

}