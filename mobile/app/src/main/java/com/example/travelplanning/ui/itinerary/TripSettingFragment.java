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
import com.example.travelplanning.ui.util.SnackBarHelper;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class TripSettingFragment extends Fragment {
    private FragmentSettingTripBinding binding;
    private ItineraryViewModel viewModel;
    private String tripId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static TripSettingFragment newInstance(String id) {
        TripSettingFragment fragment = new TripSettingFragment();
        Bundle args = new Bundle();
        args.putString("arg_trip_id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tripId = getArguments().getString("arg_trip_id");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingTripBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ItineraryViewModel.class);

        setupObservers();
        setupListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupObservers() {
        viewModel.getSelectedItinerary().observe(getViewLifecycleOwner(), itinerary -> {
            if (itinerary != null) {
                prefillFields(itinerary);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (binding == null) return;
            binding.btnSaveChange.setEnabled(!loading);
            binding.btnDeleteTrip.setEnabled(!loading);
            binding.btnCopyTrip.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                SnackBarHelper.showTopSnackBar(binding.getRoot(), msg, SnackBarHelper.SnackBarType.ERROR);
        });

        viewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                SnackBarHelper.showTopSnackBar(binding.getRoot(), "Trip deleted", SnackBarHelper.SnackBarType.SUCCESS);
                viewModel.getDeleteSuccess().setValue(false);
                assert getParentFragment() != null;
                ((TripContainerFragment) getParentFragment()).navigateTo(new TripFragment(), false);
            }
        });

        viewModel.getCloneSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                SnackBarHelper.showTopSnackBar(binding.getRoot(), "Trip cloned", SnackBarHelper.SnackBarType.SUCCESS);
                viewModel.getCloneSuccess().setValue(false);
            }
        });

        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                SnackBarHelper.showTopSnackBar(binding.getRoot(), "Changes saved", SnackBarHelper.SnackBarType.SUCCESS);
                viewModel.getUpdateSuccess().setValue(false);
            }
        });
    }

    private void prefillFields(Itinerary itinerary) {
        if (binding == null) return;

        binding.edtTripName.setText(itinerary.getTitle());
        if (itinerary.getStartDate() != null)
            binding.edtStartDate.setText(dateFormat.format(itinerary.getStartDate()));
        if (itinerary.getEndDate() != null)
            binding.edtEndDate.setText(dateFormat.format(itinerary.getEndDate()));
        binding.edtTripDescription.setText(itinerary.getDescription() != null ? itinerary.getDescription() : "");
        binding.swPrivacy.setChecked(!Objects.equals(itinerary.getPrivacy(), "private"));
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        binding.edtStartDate.setOnClickListener(v -> showDatePicker("Choose start date", true));
        binding.edtEndDate.setOnClickListener(v -> showDatePicker("Choose end date", false));
        binding.btnCopyTrip.setOnClickListener(v -> {
            if (tripId != null) viewModel.cloneItinerary(tripId);
        });
        binding.btnDeleteTrip.setOnClickListener(v -> confirmAndDelete());
        binding.btnSaveChange.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        if (tripId == null) return;

        String title = binding.edtTripName.getText().toString().trim();
        String description = binding.edtTripDescription.getText().toString().trim();
        String privacy = binding.swPrivacy.isChecked() ? "public" : "private";

        if (title.isEmpty()) {
            binding.edtTripName.setError("Trip name is required");
            return;
        }

        Date startDate = parseDate(binding.edtStartDate.getText().toString());
        Date endDate = parseDate(binding.edtEndDate.getText().toString());
        if (startDate == null) {
            binding.edtStartDate.setError("Start date is required");
            return;
        }
        if (endDate == null) {
            binding.edtEndDate.setError("End date is required");
            return;
        }
        if (endDate.before(startDate)) {
            binding.edtEndDate.setError("End date must be after start date");
            return;
        }

        viewModel.updateItinerary(tripId, title, description, privacy, startDate, endDate);
    }

    private void showDatePicker(String title, boolean isStartDate) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        datePicker.show(getParentFragmentManager(), "MATERIAL_DATE_PICKER");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);

            // format date
            String formattedDate = dateFormat.format(calendar.getTime());

            // display
            if (isStartDate)
                binding.edtStartDate.setText(formattedDate);
            else
                binding.edtEndDate.setText(formattedDate);
        });
    }

    private void confirmAndDelete() {
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_delete)
                .setTitle("Delete this trip?")
                .setMessage("Are you sure you want to delete this trip? This cannot be undone.")
                .setPositiveButton("Remove", (d, which) -> {
                    if (tripId != null)
                        viewModel.deleteItinerary(tripId);
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(getResources().getColor(R.color.error, requireContext().getTheme()));
    }

    @Nullable
    private Date parseDate(String raw) {
        try { return dateFormat.parse(raw); }
        catch (ParseException e) { return null; }
    }
}