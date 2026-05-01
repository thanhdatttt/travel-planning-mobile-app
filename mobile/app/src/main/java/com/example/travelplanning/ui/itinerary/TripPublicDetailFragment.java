package com.example.travelplanning.ui.itinerary;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.itinerary.Itinerary;
import com.example.travelplanning.databinding.FragmentTripPublicDetailBinding;
import com.example.travelplanning.ui.util.SnackBarHelper;
import com.example.travelplanning.viewmodel.itinerary.ItineraryViewModel;
import com.example.travelplanning.databinding.DialogReviewReportBinding;
import com.example.travelplanning.viewmodel.report.ReportViewmodel;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TripPublicDetailFragment extends Fragment {
    private FragmentTripPublicDetailBinding binding;
    private ItineraryViewModel viewModel;
    private ReportViewmodel reportViewModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // newInstance to pass data to fragment
    private String tripId;

    public static TripPublicDetailFragment newInstance(String id) {
        TripPublicDetailFragment fragment = new TripPublicDetailFragment();
        Bundle args = new Bundle();
        args.putString("arg_public_trip_id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tripId = getArguments().getString("arg_public_trip_id");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTripPublicDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ItineraryViewModel.class);
        reportViewModel = new ViewModelProvider(this).get(ReportViewmodel.class);

        if (tripId != null) {
            viewModel.getSelectedItinerary().setValue(null);
            viewModel.fetchItineraryById(tripId);
        }

        setupTabUI();
        setupObservers();
        setupListeners();
        setupFavoriteActions();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // prevent memory leaks
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnCloneTrip.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                SnackBarHelper.showTopSnackBar(binding.getRoot(), msg, SnackBarHelper.SnackBarType.ERROR);
        });

        reportViewModel.getReportSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), getString(R.string.submit_report) + " thành công!", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSelectedItinerary().observe(getViewLifecycleOwner(), itinerary -> {
            if (itinerary != null) {
                displayTripDetail(itinerary);
            }
        });

        viewModel.getCloneSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Trip clone successfully", Toast.LENGTH_SHORT).show();
                viewModel.getCloneSuccess().setValue(false);
            }
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            viewModel.getSelectedItinerary().setValue(null);
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.btnReport.setOnClickListener(v -> {
            if (tripId != null) {
                showReportDialog(tripId);
            }
        });


        binding.btnCloneTrip.setOnClickListener(v -> {
            viewModel.cloneItinerary(tripId);
        });
    }

    private void showReportDialog(String targetId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        DialogReviewReportBinding dialogBinding = DialogReviewReportBinding.inflate(getLayoutInflater());

        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.rgReportReasons.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOther) {
                dialogBinding.tilOtherReason.setVisibility(View.VISIBLE);
            } else {
                dialogBinding.tilOtherReason.setVisibility(View.GONE);
            }
        });

        dialogBinding.btnSubmitReport.setOnClickListener(v -> {
            int selectedId = dialogBinding.rgReportReasons.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(requireContext(), getString(R.string.please_specify), Toast.LENGTH_SHORT).show();
                return;
            }

            String reason = "";
            if (selectedId == R.id.rbSpam) reason = "Spam";
            else if (selectedId == R.id.rbInappropriate) reason = "Inappropriate";
            else if (selectedId == R.id.rbIrrelevant) reason = "Irrelevant";
            else if (selectedId == R.id.rbOther) {
                reason = dialogBinding.etOtherReason.getText().toString().trim();
                if (reason.isEmpty()) {
                    dialogBinding.etOtherReason.setError(getString(R.string.please_specify));
                    return;
                }
            }

            reportViewModel.reportItinerary(targetId, reason);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupTabUI() {
        TripPublicPagerAdapter adapter = new TripPublicPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        // Link TabLayout and ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(R.string.saved_locations);
                    } else {
                        tab.setText(R.string.itinerary);
                    }
                }).attach();
    }

    private void displayTripDetail(Itinerary itinerary) {
        if (itinerary.getItineraryItems() != null && !itinerary.getItineraryItems().isEmpty()) {
            binding.tvTripLocation.setText(itinerary.getItineraryItems().get(0).getLocation().getName());
            Glide.with(this).load(itinerary.getItineraryItems().get(0).getLocation().getImageUrl()).into(binding.ivTripCover);
        } else {
            binding.ivTripCover.setImageResource(R.drawable.ic_placeholder);
        }

        // creator info
        if (itinerary.getUser() != null) {
            binding.tvCreatorName.setText(itinerary.getUser().getUsername());
            if (itinerary.getUser().getAvatarUrl() != null) {
                Glide.with(this).load(itinerary.getUser().getAvatarUrl()).into(binding.ivCreatorAvatar);
            } else {
                binding.ivCreatorAvatar.setImageResource(R.drawable.ic_user);
            }
        } else {
            // Hiển thị tạm thời trong khi chờ API trả về bản full
            binding.tvCreatorName.setText("Loading...");
            binding.ivCreatorAvatar.setImageResource(R.drawable.ic_user);
        }

        binding.tvTripTitle.setText(itinerary.getTitle());
        if (itinerary.getStartDate() != null && itinerary.getEndDate() != null) {
            String dates = dateFormat.format(itinerary.getStartDate()) + " – " + dateFormat.format(itinerary.getEndDate());
            binding.tvTripDates.setText(dates);
        }
        if (itinerary.getItineraryItems() != null && !itinerary.getItineraryItems().isEmpty()) {
            binding.tvTripLocation.setText(itinerary.getItineraryItems().get(0).getLocation().getName());
        }
        if (itinerary.getDescription() != null) binding.tvTripDescription.setText(itinerary.getDescription());
    }

    private void setupFavoriteActions() {
        binding.btnFavorite.setOnClickListener(v -> {
            if (tripId != null) {
                viewModel.toggleFavorite(tripId);
            }
        });

        viewModel.getIsFavorited().observe(getViewLifecycleOwner(), favorited -> {
            if (favorited) {
                binding.btnFavorite.setImageResource(R.drawable.ic_heart);
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_heart_outline);
            }
        });
    }
}
