package com.example.travelplanning.ui.map;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelplanning.databinding.FragmentMapBinding;
import com.example.travelplanning.viewmodel.map.MapViewModel;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    private MapViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cấu hình osmdroid BẮT BUỘC phải đặt ở đây, trước khi tạo View
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo bản đồ
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK);
        binding.mapView.setMultiTouchControls(true);
        binding.mapView.getController().setZoom(15.0);

        // Lắng nghe ViewModel
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        viewModel.getMapData().observe(getViewLifecycleOwner(), points -> {
            if (points != null && !points.isEmpty()) {
                binding.mapView.getOverlays().clear();

                for (Pair<String, GeoPoint> point : points) {
                    Marker marker = new Marker(binding.mapView);
                    marker.setPosition(point.second);
                    marker.setTitle(point.first);
                    binding.mapView.getOverlays().add(marker);
                }

                binding.mapView.getController().setCenter(points.get(0).second);
                binding.mapView.invalidate();
            }
        });

        viewModel.loadSampleData();
    }

    // --- Xử lý vòng đời ---
    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh rò rỉ bộ nhớ
    }
}