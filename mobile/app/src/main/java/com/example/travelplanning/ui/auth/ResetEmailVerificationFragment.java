package com.example.travelplanning.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.FragmentResetEmailVerificationBinding;
import com.example.travelplanning.viewmodel.auth.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

public class ResetEmailVerificationFragment extends Fragment {
    private FragmentResetEmailVerificationBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentResetEmailVerificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnEmailResetContinue.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
        });

        viewModel.getOtpSentSuccess().observe(getViewLifecycleOwner(), sent -> {
            if (sent != null && sent) {
                // switch to otp verification
                ((AuthActivity) requireActivity()).navigateTo(new OTPVerificationFragment(), true);
                viewModel.getOtpSentSuccess().setValue(false);
            }
        });
    }

    private void setupListeners() {
        binding.btnEmailResetContinue.setOnClickListener(v -> {
            String email = binding.edtResetEmail.getText().toString().trim();

            if (email.isEmpty()) {
                binding.edtResetEmail.setError("Email is required");
                return;
            }

            // set state
            viewModel.setCurrentEmail(email);
            viewModel.setCurrentOtpType("reset");

            viewModel.sendOTP(email, "reset");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
