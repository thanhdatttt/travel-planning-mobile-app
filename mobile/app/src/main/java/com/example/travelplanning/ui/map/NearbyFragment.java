package com.example.travelplanning.ui.map;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.views.overlay.MapEventsOverlay;
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
import org.osmdroid.util.GeoPoint;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.RatingBar;
import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.data.remote.map.dto.response.PhotonResponse;
import com.example.travelplanning.databinding.FragmentNearbyBinding;
import com.example.travelplanning.ui.map.LocationAdapter;
import com.example.travelplanning.viewmodel.location.LocationViewModel;
import com.example.travelplanning.viewmodel.map.MapViewModel;
import com.example.travelplanning.viewmodel.map.NearbyViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class NearbyFragment extends Fragment {

    private GeoPoint lastFetchPoint = null;
    private long lastFetchTime = 0;

    private FragmentNearbyBinding binding;
    private MapViewModel mapViewModel;
    private LocationViewModel locationViewModel;
    private NearbyViewModel nearbyViewModel;

    private MapView mapView;
    private IMapController mapController;
    private MyLocationNewOverlay myLocationOverlay;
    private FusedLocationProviderClient fusedLocationClient;

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private LocationAdapter locationAdapter;

    private SuggestAdapter customAdapter;
    private final List<String> currentDisplayNames = new ArrayList<>();
    private List<PhotonResponse.Feature> currentSuggestions = new ArrayList<>();
    private NoFilterSuggestAdapter noFilterSuggestAdapter; 

    private List<Location> currentSearchLocations = new ArrayList<>();
    private List<String> currentSearchNames = new ArrayList<>();
    private ArrayAdapter<String> searchAdapter;

    private final Handler mapHandler = new Handler(Looper.getMainLooper());
    private Runnable mapRunnable;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private int lastBottomSheetState = BottomSheetBehavior.STATE_COLLAPSED;
    private boolean isViewingPreview = false;
    private boolean isSilentFetch = false;

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    getUserLocationAndFetchNearby();
                } else {
                    mapController.setCenter(new GeoPoint(10.7769, 106.7009));
                    fetchNearbyFromMap();
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

        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        nearbyViewModel = new ViewModelProvider(this).get(NearbyViewModel.class);

        mapView = binding.mapView;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupMap();
        setupUIFeatures();
        setupAutocomplete();
        observeData();

        locationViewModel.getLocationDetail().observe(getViewLifecycleOwner(), location -> {
            if (location != null && getArguments() != null) {
                String targetId = getArguments().getString("location_id");

                if (location.getId().equals(targetId)) {
                    if (location.getLatitude() != null && location.getLongitude() != null) {
                        GeoPoint targetPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        mapController.setZoom(18.0);
                        mapController.animateTo(targetPoint);

                        showLocationPreview(location);
                    }
                }
            }
        });

        if (getArguments() != null && getArguments().containsKey("location_id")) {
            String id = getArguments().getString("location_id");
            if (id != null) {
                locationViewModel.fetchDetail(id);
            }
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING || 
                    newState == BottomSheetBehavior.STATE_HALF_EXPANDED || 
                    newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    
                    if (binding != null && binding.editTextSearch.hasFocus()) {
                        binding.editTextSearch.clearFocus();
                        hideKeyboard();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        locationViewModel.getSearchResults().observe(getViewLifecycleOwner(), locations -> {
            if (locations != null && locationAdapter != null) {
                GeoPoint myLoc = myLocationOverlay != null ? myLocationOverlay.getMyLocation() : null;
                locationAdapter.updateData(locations, myLoc);
            }
        });
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        mapController = mapView.getController();
        mapView.setFlingEnabled(true);
        mapController.setZoom(16.0);
        mapView.setBuiltInZoomControls(false);

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), mapView);
        myLocationOverlay.enableMyLocation();

        myLocationOverlay.setDrawAccuracyEnabled(false);

        mapView.getOverlays().add(myLocationOverlay);

        MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (binding != null && binding.editTextSearch.hasFocus()) {
                    binding.editTextSearch.clearFocus();
                    hideKeyboard();
                }
                return false; 
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        mapView.getOverlays().add(new MapEventsOverlay(mapEventsReceiver));

        mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                if (binding != null && binding.editTextSearch.hasFocus()) {
                    binding.editTextSearch.clearFocus();
                    hideKeyboard();
                }
                
                checkAndFetchWhileMoving();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
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
                fetchNearbyFromMap();
            } else {
                fetchNearbyFromMap();
            }
        });
    }

    private void setupUIFeatures() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetPanel);
        bottomSheetBehavior.setHideable(false);

        locationAdapter = new LocationAdapter(this::onLocationSelected);
        binding.recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewPlaces.setAdapter(locationAdapter);

        binding.btnBack.setOnClickListener(v -> {
            hideKeyboard();
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        binding.fabMyLocation.setOnClickListener(v -> {
            if (myLocationOverlay.getMyLocation() != null) {
                mapController.animateTo(myLocationOverlay.getMyLocation());
                mapController.setZoom(17.0);
                binding.containerPreviewCard.setVisibility(View.GONE);
                bottomSheetBehavior.setHideable(false);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        View.OnClickListener chipClickListener = v -> {
            String targetIcon = "";
            if (v.getId() == R.id.chipFood) targetIcon = "ic_category_food";
            else if (v.getId() == R.id.chipHotel) targetIcon = "ic_category_hotel";
            else if (v.getId() == R.id.chipAttraction) targetIcon = "ic_category_attraction";
            else if (v.getId() == R.id.chipShop) targetIcon = "ic_category_shop";
            else if (v.getId() == R.id.chipService) targetIcon = "ic_category_service";

            nearbyViewModel.toggleCategory(targetIcon);
            
            GeoPoint myLoc = myLocationOverlay.getMyLocation();
            if (myLoc != null && nearbyViewModel.getSelectedCategoryIcon().getValue() != null) {
                locationViewModel.fetchPanelLocationsByCategory(myLoc.getLatitude(), myLoc.getLongitude(), targetIcon);
            }

            bottomSheetBehavior.setHideable(false);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        };
        
        binding.chipFood.setOnClickListener(chipClickListener);
        binding.chipHotel.setOnClickListener(chipClickListener);
        binding.chipAttraction.setOnClickListener(chipClickListener);
        if (binding.chipShop != null) binding.chipShop.setOnClickListener(chipClickListener);
        if (binding.chipService != null) binding.chipService.setOnClickListener(chipClickListener);

        binding.btnZoomIn.setOnClickListener(v -> {
            if (mapController != null) mapController.zoomIn();
        });

        binding.btnZoomOut.setOnClickListener(v -> {
            if (mapController != null) mapController.zoomOut();
        });
    }

    private void observeData() {
        locationViewModel.getNearbyLocations().observe(getViewLifecycleOwner(), locations -> {
            GeoPoint myLoc = myLocationOverlay != null ? myLocationOverlay.getMyLocation() : null;
            nearbyViewModel.setMapData(locations, myLoc);
        });

        locationViewModel.getCategoryPanelLocations().observe(getViewLifecycleOwner(), locations -> {
            GeoPoint myLoc = myLocationOverlay != null ? myLocationOverlay.getMyLocation() : null;
            nearbyViewModel.setCategoryPanelData(locations, myLoc);
        });

        nearbyViewModel.getMapMarkers().observe(getViewLifecycleOwner(), this::drawCustomMarkers);

        nearbyViewModel.getPanelList().observe(getViewLifecycleOwner(), locations -> {
            GeoPoint myLoc = myLocationOverlay != null ? myLocationOverlay.getMyLocation() : null;
            locationAdapter.updateData(locations, myLoc);
        });

        nearbyViewModel.getSelectedCategoryIcon().observe(getViewLifecycleOwner(), selectedIcon -> {
            binding.chipFood.setAlpha(selectedIcon == null || selectedIcon.equals("ic_category_food") ? 1.0f : 0.5f);
            binding.chipHotel.setAlpha(selectedIcon == null || selectedIcon.equals("ic_category_hotel") ? 1.0f : 0.5f);
            binding.chipAttraction.setAlpha(selectedIcon == null || selectedIcon.equals("ic_category_attraction") ? 1.0f : 0.5f);
            if (binding.chipShop != null) binding.chipShop.setAlpha(selectedIcon == null || selectedIcon.equals("ic_category_shop") ? 1.0f : 0.5f);
            if (binding.chipService != null) binding.chipService.setAlpha(selectedIcon == null || selectedIcon.equals("ic_category_service") ? 1.0f : 0.5f);
        });

        mapViewModel.getAutocompleteResults().observe(getViewLifecycleOwner(), features -> {
            currentSuggestions.clear();
            currentSuggestions.addAll(features);
            currentDisplayNames.clear();
            for (PhotonResponse.Feature feature : features) {
                String name = feature.properties.name != null ? feature.properties.name : feature.properties.street;
                if (name != null) currentDisplayNames.add(name);
            }
            customAdapter.notifyDataSetChanged();
            if (!currentDisplayNames.isEmpty()) binding.editTextSearch.showDropDown();
        });

        locationViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
        if (binding.layoutLoadingOverlay != null) {
            
            if (isLoading && isSilentFetch) {
                return; 
            }
            
            if (!isLoading) {
                isSilentFetch = false;
            }
        }
    });

    }

    private void showLocationPreview(Location loc) {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            lastBottomSheetState = bottomSheetBehavior.getState();
        }

        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        
        binding.containerPreviewCard.setVisibility(View.VISIBLE);
        
        binding.containerPreviewCard.setAlpha(0f);
        binding.containerPreviewCard.setVisibility(View.VISIBLE);
        binding.containerPreviewCard.animate()
                .alpha(1f) 
                .setDuration(250) 
                .start();

        View card = binding.containerPreviewCard;
        ((TextView) card.findViewById(R.id.tvPlaceName)).setText(loc.getName());
        
        double rating = loc.getAvgRating() != null ? loc.getAvgRating() : 0.0;
        int count = loc.getRatingCount() != null ? loc.getRatingCount() : 0;
            
        TextView tvRatingScore = card.findViewById(R.id.tvRatingScore);
        if (tvRatingScore != null) {
            tvRatingScore.setText(String.format(java.util.Locale.US, "%.1f (%d)", rating, count));
        }

        RatingBar rbAverageRating = card.findViewById(R.id.rbAverageRating);
        if (rbAverageRating != null) {
            rbAverageRating.setRating((float) rating);
        }

        GeoPoint myLoc = myLocationOverlay != null ? myLocationOverlay.getMyLocation() : null;
        if (myLoc != null && loc.getLatitude() != null) {
            double dist = myLoc.distanceToAsDouble(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
            String distStr = dist > 1000 ? String.format("%.1f km", dist / 1000) : Math.round(dist) + " m";
            ((TextView) card.findViewById(R.id.tvDistanceAddress)).setText(distStr + " • " + (loc.getAddress() != null ? loc.getAddress() : ""));
        } else {
            ((TextView) card.findViewById(R.id.tvDistanceAddress)).setText(loc.getAddress() != null ? loc.getAddress() : "");
        }

        Glide.with(this).load(loc.getImageUrl())
            .centerCrop().placeholder(R.drawable.ic_placeholder)
            .into((ImageView) card.findViewById(R.id.imgPlace));

        isViewingPreview = true; 

        ImageView btnClosePreview = card.findViewById(R.id.btnClosePreview);
        if (btnClosePreview != null) {
            btnClosePreview.setOnClickListener(v -> {
                
                binding.containerPreviewCard.animate()
                        .alpha(0f) 
                        .setDuration(200) 
                        .withEndAction(() -> { 
                            binding.containerPreviewCard.setVisibility(View.GONE);
                            
                            bottomSheetBehavior.setState(lastBottomSheetState);
                            
                            binding.getRoot().postDelayed(() -> {
                                bottomSheetBehavior.setHideable(false);
                                isViewingPreview = false; 
                            }, 300);
                        }).start();
            });
        }

        card.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("location_id", loc.getId());
            androidx.navigation.Navigation.findNavController(requireView())
                    .navigate(R.id.nav_location_detail, bundle);
        });
    }

    private void drawCustomMarkers(List<Location> locations) {
        mapView.getOverlays().removeIf(overlay -> overlay instanceof Marker);

        if (locations != null) {
            for (Location loc : locations) {
                if (loc.getLatitude() == null || loc.getLongitude() == null) continue;
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
        if (mapView == null) return;
        double currentLat = mapView.getMapCenter().getLatitude();
        double currentLng = mapView.getMapCenter().getLongitude();
        double currentZoom = mapView.getZoomLevelDouble();

        lastFetchPoint = new GeoPoint(currentLat, currentLng);
        lastFetchTime = System.currentTimeMillis();

        int radius = nearbyViewModel.calculateRadiusFromZoom(currentZoom);
        isSilentFetch = true;
        locationViewModel.fetchNearbyLocations(currentLat, currentLng, radius, null);
    }

    private void scheduleFetch() {
        if (mapRunnable != null) mapHandler.removeCallbacks(mapRunnable);
        mapRunnable = this::fetchNearbyFromMap;
        mapHandler.postDelayed(mapRunnable, 800);
    }

    private void setupAutocomplete() {
        searchAdapter = new NoFilterSuggestAdapter(requireContext(), currentSearchNames);
        binding.editTextSearch.setAdapter(searchAdapter);

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.editTextSearch.setText(""); 
        });

        if (binding.btnSearchBack != null) {
            binding.btnSearchBack.setOnClickListener(v -> {
                binding.editTextSearch.clearFocus();
                hideKeyboard();
            });
        }

        binding.editTextSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (binding.imgSearchIcon != null && binding.btnSearchBack != null) {
                if (hasFocus) {
                    binding.imgSearchIcon.setVisibility(View.GONE);
                    binding.btnSearchBack.setVisibility(View.VISIBLE);
                    
                    if (bottomSheetBehavior != null) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                } else {
                    binding.btnSearchBack.setVisibility(View.GONE);
                    binding.imgSearchIcon.setVisibility(View.VISIBLE);  
                }
            }
        });

        binding.editTextSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override 
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override 
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.toString().trim().length() > 0) {
                    binding.btnClearSearch.setVisibility(View.VISIBLE);
                } else {
                    binding.btnClearSearch.setVisibility(View.GONE);
                }
                
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
            }

            @Override 
            public void afterTextChanged(android.text.Editable s) {
                String keyword = s.toString().trim();
                if (keyword.length() >= 2) {
                    searchRunnable = () -> {
                        locationViewModel.searchLocations(keyword, null, null, 1, 10);
                    };
                    searchHandler.postDelayed(searchRunnable, 500);
                } else {
                    currentSearchNames.clear();
                    currentSearchLocations.clear();
                    searchAdapter.notifyDataSetChanged();
                }
            }
        });

        binding.editTextSearch.setOnItemClickListener((parent, v, position, id) -> {
            if (position < currentSearchLocations.size()) {
                Location selectedLocation = currentSearchLocations.get(position);
                String name = selectedLocation.getName();
                
                binding.editTextSearch.setText(name);
                binding.editTextSearch.setSelection(name != null ? name.length() : 0);
                binding.editTextSearch.dismissDropDown();
                binding.editTextSearch.clearFocus();
                hideKeyboard();
                
                if (selectedLocation.getLatitude() != null && selectedLocation.getLongitude() != null) {
                    GeoPoint point = new GeoPoint(
                            selectedLocation.getLatitude(), 
                            selectedLocation.getLongitude()
                    );
                    mapController.animateTo(point, 17.0, 1500L);
                }
            }
        });

        binding.editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                
                String currentKeyword = binding.editTextSearch.getText().toString().trim();
                if (!currentKeyword.isEmpty()) {
                    locationViewModel.searchLocations(currentKeyword, null, null, 1, 10);
                    
                    binding.editTextSearch.dismissDropDown();
                    binding.editTextSearch.clearFocus();
                    hideKeyboard();
                    if (bottomSheetBehavior != null) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }
                return true;
            }
            return false;
        });
    }

    private void executeSearch() {
        String keyword = binding.editTextSearch.getText().toString().trim();
        if (!keyword.isEmpty()) {
            hideKeyboard();
            binding.editTextSearch.dismissDropDown();
            binding.editTextSearch.clearFocus();
            nearbyViewModel.toggleCategory("CLEAR_ALL");
            locationViewModel.searchLocations(keyword, null, null, 1, 20);
        }
    }

    private void onLocationSelected(Location selectedLoc) {
        if (selectedLoc.getLatitude() == null || selectedLoc.getLongitude() == null) {
            Toast.makeText(requireContext(), "Địa điểm này chưa có tọa độ cụ thể!", Toast.LENGTH_SHORT).show();
            showLocationPreview(selectedLoc);
            return;
        }
        isViewingPreview = true;
        GeoPoint targetPoint = new GeoPoint(selectedLoc.getLatitude(), selectedLoc.getLongitude());
        mapController.setZoom(18.0);
        mapController.animateTo(targetPoint);
        showLocationPreview(selectedLoc);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.editTextSearch.getWindowToken(), 0);
        }
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
        if (mapRunnable != null) mapHandler.removeCallbacks(mapRunnable);
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

    private void checkAndFetchWhileMoving() {
        if (mapView == null || isViewingPreview) return;
        GeoPoint currentCenter = new GeoPoint(mapView.getMapCenter().getLatitude(), mapView.getMapCenter().getLongitude());
        long currentTime = System.currentTimeMillis();

        if (lastFetchPoint == null) {
            fetchNearbyFromMap();
            return;
        }

        double distanceMoved = lastFetchPoint.distanceToAsDouble(currentCenter);
        if (distanceMoved > 300 && (currentTime - lastFetchTime > 1000)) {
            if (mapRunnable != null) mapHandler.removeCallbacks(mapRunnable);
            fetchNearbyFromMap();
        } else {
            scheduleFetch();
        }
    }

    private class NoFilterSuggestAdapter extends ArrayAdapter<String> {
        private final List<String> items;

        public NoFilterSuggestAdapter(Context context, List<String> items) {
            super(context, android.R.layout.simple_dropdown_item_1line, items);
            this.items = items;
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
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }
    }

}