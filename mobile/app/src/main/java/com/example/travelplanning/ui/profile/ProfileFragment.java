package com.example.travelplanning.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.databinding.FragmentProfileBinding;
import com.example.travelplanning.viewmodel.profile.ProfileViewModel;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

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
        adapter = new ProfileAdapter(profileItems);
        binding.rvProfileInfo.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProfileInfo.setAdapter(adapter);
    }

    private void setupObservers(){
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            Log.d("DEBUG_UI", "Dữ liệu User nhận được: " + (user != null ? user.getFullName() : "null"));
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
        profileItems.add(new ProfileItem(R.string.label_birthdate, dobString, "birthdate", false));

        adapter.notifyDataSetChanged();
    }

    private void setupListeners() {
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


    }

    private UserProfile convertItemsToUserProfile() {
        UserProfile user = new UserProfile();
        for (ProfileItem item : profileItems) {
            String value = item.getValue();
            if (value == null) value = "";

            switch (item.getFieldKey()) {
                case "name": user.setFullName(item.getValue()); break;
                case "email": user.setEmail(item.getValue()); break;
                case "address": user.setAddress(item.getValue()); break;
                case "phone": user.setPhone(item.getValue()); break;
                case "birthdate":
                    if (!value.isEmpty()) {
                        try {
                            user.setDob(LocalDate.parse(value));
                        } catch (DateTimeParseException e) {
                            Log.e("PROFILE", "Sai định dạng ngày: " + value);
                        }
                    }
                    break;
            }
        }
        return user;
    }
}