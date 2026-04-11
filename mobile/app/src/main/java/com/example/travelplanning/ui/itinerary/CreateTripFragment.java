package com.example.travelplanning.ui.itinerary;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.FragmentCreateTripBinding;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CreateTripFragment extends Fragment {
    private FragmentCreateTripBinding binding;
    private ItineraryViewModel viewModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateTripBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.btnCreateTrip.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show();
        });

        viewModel.getCreateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Snackbar.make(requireView(), "Trip created", Snackbar.LENGTH_LONG).show();
                viewModel.getCreateSuccess().setValue(false);
                ((TripActivity) requireActivity()).navigateTo(new TripFragment(), true);
            }
        });
    }

    private void setupListeners() {
        binding.edtStartDate.setOnClickListener(v -> showDatePicker("Choose start date", true));
        binding.edtEndDate.setOnClickListener(v -> showDatePicker("Choose end date", false));

        binding.btnBack.setOnClickListener(v -> {
            ((TripActivity) requireActivity()).navigateTo(new TripFragment(), true);
        });
        binding.btnCancel.setOnClickListener(v -> {
            ((TripActivity) requireActivity()).navigateTo(new TripFragment(), true);
        });

        binding.btnCreateTrip.setOnClickListener(view -> createTrip());
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

    private void createTrip() {
        String title = binding.edtTripName.getText().toString().trim();
        String startStr = binding.edtStartDate.getText().toString().trim();
        String endStr = binding.edtEndDate.getText().toString().trim();

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

        viewModel.createItinerary(title, startDate, endDate);
    }

    private Date parseDate(String raw) {
        try {
            return dateFormat.parse(raw);
        } catch (ParseException e) {
            return null;
        }
    }
}
