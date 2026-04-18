package com.example.travelplanning.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.databinding.FragmentProfileBinding;
import com.example.travelplanning.viewmodel.profile.ProfileViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {
    private ProfileAdapter adapter;
    private List<ProfileItem> profileItems = new ArrayList<>();;
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupRecyclerView();
        setupObservers();
        setupListeners();

        viewModel.fetchUserProfile();
    }

    private void setupRecyclerView(){
        adapter = new ProfileAdapter(profileItems, item -> {
            if ("dob".equals(item.getFieldKey())) {
                int position = profileItems.indexOf(item);
                showDatePicker(item, position);
            }
        });
        binding.rvProfileInfo.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(binding.rvProfileInfo.getContext(), LinearLayoutManager.VERTICAL);
        binding.rvProfileInfo.addItemDecoration(dividerItemDecoration);
        binding.rvProfileInfo.setAdapter(adapter);
    }

    private void setupObservers(){
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
//            Log.d("DEBUG_UI", "Dữ liệu User nhận được: " + (user != null ? user.getFullName() : "null"));
            if (user != null)
                updateUI(user);
        });
    }

    private void updateUI(UserProfile user){
        profileItems.clear();
        profileItems.add(new ProfileItem(R.string.label_fullname, user.getFullName(), "name", false));
        profileItems.add(new ProfileItem(R.string.label_email, user.getEmail(), "email", false));
        profileItems.add(new ProfileItem(R.string.label_address, user.getAddress(), "address", false));
        profileItems.add(new ProfileItem(R.string.label_phone, user.getPhone(), "phone", false));
        String dobString = (user.getDob() != null) ? user.getDob().toString() : "";
        profileItems.add(new ProfileItem(R.string.label_birthdate, dobString, "dob", false));

        adapter.notifyDataSetChanged();

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.suprised_car)
                    .circleCrop()
                    .into(binding.ivAvatar);
        }
    }

    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                Navigation.findNavController(v).popBackStack();
            }
        });

        binding.btnEdit.setOnClickListener(v -> {
            adapter.setEditMode(true);
            binding.btnSave.setVisibility(View.VISIBLE);
            binding.btnEdit.setVisibility(View.GONE);
        });

        binding.btnSave.setOnClickListener(v -> {
            UserProfile updatedUserProfile = convertItemsToUserProfile();
            viewModel.updateUserProfile(updatedUserProfile);

            adapter.setEditMode(false);
            binding.btnSave.setVisibility(View.GONE);
            binding.btnEdit.setVisibility(View.VISIBLE);
        });

        binding.ivAvatar.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
    }

    private UserProfile convertItemsToUserProfile() {
        UserProfile user = new UserProfile();
        for (ProfileItem item : profileItems) {
            String value = item.getValue();
            String finalValue = (value == null || value.trim().isEmpty()) ? null : value;

            switch (item.getFieldKey()) {
                case "name": user.setFullName(finalValue); break;
                case "email": user.setEmail(finalValue); break;
                case "address": user.setAddress(finalValue); break;
                case "phone": user.setPhone(finalValue); break;
                case "dob":
                    if (finalValue != null) {
                        user.setDob(LocalDate.parse(finalValue));
                    } else {
                        user.setDob(null);
                    }
                    break;
            }
        }
        return user;
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    Glide.with(this).load(uri).circleCrop().into(binding.ivAvatar);
                    viewModel.uploadAvatar(uri);
                }
            });

    private void showDatePicker(ProfileItem item, int position) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.select_date)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateString = sdf.format(new Date(selection));

            item.setValue(dateString);
            adapter.notifyItemChanged(position);
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }
}