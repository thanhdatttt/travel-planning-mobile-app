package com.example.travelplanning.ui.itinerary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.databinding.FragmentSettingTripBinding;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class TripSettingFragment extends Fragment {
    private FragmentSettingTripBinding binding;
    private ItineraryViewModel viewModel;
    private Itinerary mItinerary;
    private static final String ARG_ITINERARY = "arg_itinerary";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static TripSettingFragment newInstance(Itinerary itinerary) {
        TripSettingFragment fragment = new TripSettingFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITINERARY, (Serializable) itinerary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mItinerary = (Itinerary) getArguments().getSerializable(ARG_ITINERARY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingTripBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ItineraryViewModel.class);

        prefillFields();
        setupObservers();
        setupListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // fill existed data into form
    private void prefillFields() {
        if (mItinerary == null) return;

        binding.edtTripName.setText(mItinerary.getTitle());
        binding.edtStartDate.setText(dateFormat.format(mItinerary.getStartDate()));
        binding.edtEndDate.setText(dateFormat.format(mItinerary.getEndDate()));
        if (mItinerary.getDescription() != null) binding.edtTripDescription.setText(mItinerary.getDescription());
        binding.swPrivacy.setChecked(!Objects.equals(mItinerary.getPrivacy(), "private"));
    }

    private void setupListeners() {
        // back button
        binding.btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // privacy switch
        binding.swPrivacy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updatePrivacy(isChecked);
        });

        // delete and clone
        binding.btnCopyTrip.setOnClickListener(v -> {
            viewModel.cloneItinerary(mItinerary.getId());
        });
        binding.btnDeleteTrip.setOnClickListener(v -> confirmAndDelete());

        // save changes
        binding.btnSaveChange.setOnClickListener(v -> saveChanges());
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.btnSaveChange.setEnabled(!loading);
            binding.btnDeleteTrip.setEnabled(!loading);
            binding.btnCopyTrip.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                Snackbar.make(requireView(), msg, Snackbar.LENGTH_SHORT).show();
        });

        viewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Snackbar.make(requireView(), "Trip deleted", Snackbar.LENGTH_LONG).show();
                viewModel.getDeleteSuccess().setValue(false);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        viewModel.getCloneSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Snackbar.make(requireView(), "Trip cloned", Snackbar.LENGTH_LONG).show();
                viewModel.getCloneSuccess().setValue(false);
            }
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Snackbar.make(requireView(), "Changes saved", Snackbar.LENGTH_SHORT).show();
                viewModel.getUpdateSuccess().setValue(false);
            }
        });
    }

    // update privacy
    private void updatePrivacy(boolean isChecked) {
        binding.tvPrivacyStatus.setText(isChecked ? "Public — anyone can see this trip"
                : "Private — only you can see this trip");
    }

    // confirm delete trip
    private void confirmAndDelete() {
        new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_delete)
                .setTitle("Delete this trip?")
                .setMessage("Are you sure you want to delete this trip? This cannot be undone.")
                .setCancelable(false) // Ensure user makes a choice, can't dismiss by tapping outside
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteItinerary(mItinerary.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // save setting changes
    private void saveChanges() {
        String title = binding.edtTripName.getText().toString().trim();
        String description = binding.edtTripDescription.getText().toString().trim();
        String startStr = binding.edtStartDate.getText().toString().trim();
        String endStr = binding.edtEndDate.getText().toString().trim();
        String privacy = binding.swPrivacy.isChecked() ? "private" : "public";

        // check valid data
        if (title.isEmpty()) {
            binding.edtTripName.setError("Trip name is required");
            return;
        }
        Date startDate = parseDate(startStr);
        Date endDate = parseDate(endStr);
        if (endDate == null || startDate == null) {
            binding.edtStartDate.setError("Start date is required");
            binding.edtEndDate.setError("End date is required");
            return;
        }
        if (endDate.before(startDate)) {
            binding.edtEndDate.setError("End date must be after start date");
            return;
        }

        viewModel.updateItinerary(mItinerary.getId(), title, description, privacy, startDate, endDate);
    }

    @Nullable
    private Date parseDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return dateFormat.parse(raw);
        } catch (ParseException e) {
            return null;
        }
    }
}
