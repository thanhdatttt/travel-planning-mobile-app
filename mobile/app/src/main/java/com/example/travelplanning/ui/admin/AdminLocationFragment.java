package com.example.travelplanning.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.location.Location;
import com.example.travelplanning.databinding.FragmentAdminLocationBinding;
import com.example.travelplanning.databinding.SearchAndFilterBinding;
import com.example.travelplanning.databinding.AdminHeaderBinding;
import com.example.travelplanning.viewmodel.admin.AdminLocationViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

public class AdminLocationFragment extends Fragment {
    private FragmentAdminLocationBinding binding;
    private SearchAndFilterBinding searchAndFilterBinding;
    private AdminHeaderBinding adminHeaderBinding;
    private AdminLocationViewModel viewModel;
    private AdminLocationAdapter adapter;
    private final List<Location> locationList = new ArrayList<>();
    private final List<Uri> selectedPhotoUris = new ArrayList<>();
    private PhotoUriAdapter createPhotoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminLocationBinding.inflate(inflater, container, false);
        searchAndFilterBinding = SearchAndFilterBinding.bind(binding.searchAndFilter.getRoot());
        adminHeaderBinding = AdminHeaderBinding.bind(binding.adminHeader.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AdminLocationViewModel.class);

        adminHeaderBinding.btnLocation.setSelected(true);
        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.fetchLocations(false);
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> pickIncrementalPhoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedPhotoUris.add(uri); // Add to the end of the list
                    if (createPhotoAdapter != null) {
                        // Tell the adapter to draw the newly added photo
                        createPhotoAdapter.notifyItemInserted(selectedPhotoUris.size() - 1);
                    }
                }
            });

    private void setupRecyclerView() {
        adapter = new AdminLocationAdapter(locationList, this::showPopupMenu, (view, location) -> {
            Bundle bundle = new Bundle();
            bundle.putString("location_id", location.getId());
            Navigation.findNavController(view).navigate(R.id.nav_location_detail, bundle);
        });
        binding.rvLocations.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLocations.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getLocations().observe(getViewLifecycleOwner(), locations -> {
            if (locations != null) {
                locationList.clear();
                locationList.addAll(locations);
                adapter.notifyDataSetChanged();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        searchAndFilterBinding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onSearchQueryChanged(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.fabAddLocation.setOnClickListener(v -> showCreateLocationDialog());

        searchAndFilterBinding.btnFilter.setOnClickListener(v ->
                new AdminLocationFilterDialog().show(getChildFragmentManager(), "AdminLocationFilter"));

        adminHeaderBinding.btnChart.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_admin_stat);
        });

        adminHeaderBinding.btnUser.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_admin);
        });

        // SCROLLING LISTENER
        binding.rvLocations.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // ONLY FETCH WHEN NEAR BOTTOM OF RC
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 13
                            && firstVisibleItemPosition >= 0) {
                        viewModel.fetchLocations(true);
                    }
                }
            }
        });
    }

    private void showPopupMenu(View anchor, Location location) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenu().add(0, 1, 0, R.string.edit_location);
        popup.getMenu().add(0, 2, 1, R.string.delete_location);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                showEditLocationDialog(location);
                return true;
            } else if (item.getItemId() == 2) {
                AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.delete_location))
                        .setMessage(getString(R.string.are_you_sure_you_want_to_delete) + location.getName() + "?")
                        .setPositiveButton(getString(R.string.delete), (d, w) -> viewModel.deleteLocation(location))
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showEditLocationDialog(Location location) {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_location, null);

        TextView tvId = view.findViewById(R.id.tvEditId);
        EditText etName = view.findViewById(R.id.etEditName);
        EditText etAddress = view.findViewById(R.id.etEditAddress);
        EditText etPhone = view.findViewById(R.id.etEditPhone);
        EditText etRating = view.findViewById(R.id.etEditRating);

        AutoCompleteTextView autoPrice = view.findViewById(R.id.autoCompletePriceLevel);
        AutoCompleteTextView autoCategory = view.findViewById(R.id.autoCompleteCategory);

        ImageView ivPreview = view.findViewById(R.id.ivCurrentThumbnail);
        RecyclerView rvPhotos = view.findViewById(R.id.rvPhotoSelector);

        tvId.setText(location.getId() != null ? location.getId() : "N/A");
        etName.setText(location.getName());
        etAddress.setText(location.getAddress());
        etPhone.setText(location.getPhone());
        etRating.setText(String.valueOf(location.getAvgRating()));

        String[] priceLabels = {"$", "$$", "$$$", "$$$$"};
        ArrayAdapter<String> priceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, priceLabels);
        autoPrice.setAdapter(priceAdapter);

        // Map Integer (1-4) back to Label
        if (location.getPriceLevel() != null && location.getPriceLevel() >= 1 && location.getPriceLevel() <= 4) {
            autoPrice.setText(priceLabels[location.getPriceLevel() - 1], false);
        }

        String[] categories = {getString(R.string.restaurant), getString(R.string.hotel), getString(R.string.attraction), getString(R.string.shopping), getString(R.string.service)};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        autoCategory.setAdapter(catAdapter);
        autoCategory.setText(categories[location.getCategoryId()], false);

        final String[] selectedUrl = {location.getImageUrl()};
        Glide.with(this).load(selectedUrl[0]).into(ivPreview);

        PhotoSelectAdapter photoAdapter = new PhotoSelectAdapter(location.getPhotos(), url -> {
            selectedUrl[0] = url;
            Glide.with(ivPreview).load(url).into(ivPreview);
        });
        rvPhotos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPhotos.setAdapter(photoAdapter);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.edit_location) + location.getName())
                .setView(view)
                .setPositiveButton(getString(R.string.edit), (d, which) -> {
                    try {
                        location.setName(etName.getText().toString());
                        location.setAddress(etAddress.getText().toString());
                        location.setPhone(etPhone.getText().toString());
                        location.setAvgRating(Double.parseDouble(etRating.getText().toString()));
                        location.setImageUrl(selectedUrl[0]);
                        location.setCategoryName(autoCategory.getText().toString());

                        String priceText = autoPrice.getText().toString();
                        for (int i = 0; i < priceLabels.length; i++) {
                            if (priceLabels[i].equals(priceText)) {
                                location.setPriceLevel(i + 1);
                                break;
                            }
                        }

                        viewModel.updateLocation(location);
                        Toast.makeText(getContext(), "Updating " + location.getName() + "...", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Invalid input format!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (d, which) -> d.dismiss())
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green));

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
    }

    private void showCreateLocationDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_create_location, null);

        EditText etName = view.findViewById(R.id.etCreateName);
        EditText etAddress = view.findViewById(R.id.etCreateAddress);
        EditText etDesc = view.findViewById(R.id.etCreateDesc);

        AutoCompleteTextView autoPrice = view.findViewById(R.id.autoCompleteCreatePriceLevel);
        AutoCompleteTextView autoCategory = view.findViewById(R.id.autoCompleteCreateCategory);
        RecyclerView rvPhotos = view.findViewById(R.id.rvCreatePhotoSelector);
        ImageButton btnAddPhotos = view.findViewById(R.id.btnIncrementalAddPhoto);

        selectedPhotoUris.clear();
        createPhotoAdapter = new PhotoUriAdapter(selectedPhotoUris);
        rvPhotos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPhotos.setAdapter(createPhotoAdapter);

        if (btnAddPhotos != null) {
            btnAddPhotos.setOnClickListener(v -> {
                pickIncrementalPhoto.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            });
        }

        String[] priceLabels = {"$", "$$", "$$$", "$$$$"};
        ArrayAdapter<String> priceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, priceLabels);
        autoPrice.setAdapter(priceAdapter);
        autoPrice.setText(priceLabels[0], false);

        String[] categories = {getString(R.string.restaurant), getString(R.string.hotel), getString(R.string.attraction), getString(R.string.shopping), getString(R.string.service)};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        autoCategory.setAdapter(catAdapter);
        autoCategory.setText(categories[0], false);

        // get index of selected category and price
        final int[] selectedPriceLevel = {1};
        final int[] selectedCategoryId = {1};

        autoPrice.setOnItemClickListener((parent, view1, position, id) -> {
            selectedPriceLevel[0] = position + 1;
        });

        autoCategory.setOnItemClickListener((parent, view1, position, id) -> {
            selectedCategoryId[0] = position + 1;
        });


        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.add_location))
                .setView(view)
                .setPositiveButton(getString(R.string.create), (d, which) -> {
                    String name = etName.getText().toString();
                    String address = etAddress.getText().toString();

                    if (name.isEmpty() || address.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        Location newLocation = new Location();
                        newLocation.setName(name);
                        newLocation.setAddress(address);
                        newLocation.setDescription(etDesc.getText().toString());

                        newLocation.setPriceLevel(selectedPriceLevel[0]);
                        newLocation.setCategoryId(selectedCategoryId[0]);

                        viewModel.createLocationWithPhotos(newLocation, new ArrayList<>(selectedPhotoUris));
                        Toast.makeText(getContext(), "Creating " + name + "...", Toast.LENGTH_SHORT).show();

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid coordinate formats!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (d, which) -> d.dismiss())
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
    }
}