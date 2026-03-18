package com.example.travelplanning.ui.map;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelplanning.databinding.FragmentMapBinding;
import com.example.travelplanning.data.remote.map.dto.response.PhotonResponse;
import com.example.travelplanning.viewmodel.map.MapViewModel;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    private MapViewModel viewModel;
    private MapView mapView;
    private IMapController mapController;

    private ArrayAdapter<String> autocompleteAdapter;
    private List<PhotonResponse.Feature> currentSuggestions = new ArrayList<>();

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private List<String> displayList = new ArrayList<>();
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
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        mapView = binding.mapView;

        setupMap();
        setupAutocomplete();


        binding.editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                executeSearch();
                return true;
            }
            return false;
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
        });

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.features != null && !response.features.isEmpty()) {
                pinLocationOnMap(response.features.get(0)); // Lấy top 1 cắm ghim
            } else {
                Toast.makeText(requireContext(), "Không tìm thấy địa điểm!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        mapView.setBuiltInZoomControls(true);

        mapController = mapView.getController();
        mapController.setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(10.7769, 106.7009);
        mapController.setCenter(startPoint);
    }

    private void setupAutocomplete() {
        SuggestAdapter customAdapter = new SuggestAdapter(requireContext(), displayList);
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

                    org.osmdroid.util.BoundingBox box = mapView.getBoundingBox();
                    String bboxString = box.getLonWest() + "," + box.getLatSouth() + "," + box.getLonEast() + "," + box.getLatNorth();
                    double centerLat = mapView.getMapCenter().getLatitude();
                    double centerLon = mapView.getMapCenter().getLongitude();

                    searchRunnable = () -> viewModel.fetchAutocomplete(keyword, centerLat, centerLon, bboxString);
                    searchHandler.postDelayed(searchRunnable, 500);
                } else {
                    displayList.clear();
                    currentSuggestions.clear();
                    if (binding.editTextSearch.getAdapter() != null) {
                        ((SuggestAdapter) binding.editTextSearch.getAdapter()).notifyDataSetChanged();
                    }
                    binding.editTextSearch.dismissDropDown();
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

        viewModel.getAutocompleteResults().observe(getViewLifecycleOwner(), features -> {
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
            viewModel.performSearch(keyword, centerLat, centerLon, bboxString);
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

}