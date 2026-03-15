package com.example.travelplanning.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.databinding.FragmentProfileBinding;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private ProfileAdapter adapter;
    private List<ProfileItem> profileItems;
    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //giả lập dữ liệu từ UserProfile object
        UserProfile userProfile = UserProfile.builder()
                .email("a@gmail.com")
                .address("atlantis")
                .fullName("Fish")
                .dob(LocalDate.of(2000, Month.MARCH,6))
                .phone("9993636718")
                .build();
        profileItems = new ArrayList<>();
        profileItems.add(new ProfileItem("Email", userProfile.getEmail(), "email", false));
        profileItems.add(new ProfileItem("Address", userProfile.getAddress(), "address", false));
        profileItems.add(new ProfileItem("Full Name", userProfile.getFullName(), "name", false));
        profileItems.add(new ProfileItem("Birthdate", userProfile.getDob().toString(), "birthdate", false));
        profileItems.add(new ProfileItem("Phone", userProfile.getPhone(), "phone", false));

        binding.rvProfileInfo.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProfileAdapter(profileItems);
        binding.rvProfileInfo.setAdapter(adapter);

        binding.btnEdit.setOnClickListener(v -> {
            adapter.setEditMode(true);
            binding.btnSave.setVisibility(View.VISIBLE);
            binding.btnEdit.setVisibility(View.GONE);
        });

        binding.btnSave.setOnClickListener(v -> {
            // TODO: Gọi API update profile tại đây
            // updateProfileApi(profileItems);

            adapter.setEditMode(false);
            binding.btnSave.setVisibility(View.GONE);
            binding.btnEdit.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
        });
    }
}