package com.example.travelplanning.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.FragmentEmailOtpVerificationBinding;
import com.example.travelplanning.viewmodel.auth.AuthViewModel;

public class OTPVerificationFragment extends Fragment {
    private FragmentEmailOtpVerificationBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEmailOtpVerificationBinding.inflate(inflater, container, false);
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
            binding.btnVerifyOtp.setEnabled(!loading);
        });


        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getOtpVerifySuccess().observe(getViewLifecycleOwner(), verified -> {
            if (verified != null && verified) {
                if ("register".equals(viewModel.getCurrentOtpType())) {
                    ((AuthActivity) requireActivity()).navigateTo(new RegisterDetailFragment(), true);
                } else {
                    ((AuthActivity) requireActivity()).navigateTo(new ResetPasswordFragment(), true);
                }
            }
        });
    }

    private void setupListeners() {
        binding.btnVerifyOtp.setOnClickListener(v -> {
            String otp = binding.edtOtpCode.getText().toString().trim();

            // get state
            String email = viewModel.getCurrentEmail();
            String type = viewModel.getCurrentOtpType();

            viewModel.verifyOTP(email, otp, type);
        });

        binding.tvResend.setOnClickListener(v -> {
            viewModel.sendOTP(viewModel.getCurrentEmail(), viewModel.getCurrentOtpType());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
