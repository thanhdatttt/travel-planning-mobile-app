package com.example.travelplanning.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.FragmentRegisterEmailBinding;
import com.example.travelplanning.ui.mainscreen.MainScreenActivity;
import com.example.travelplanning.viewmodel.auth.AuthViewModel;

public class RegisterEmailFragment extends Fragment {
    private FragmentRegisterEmailBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterEmailBinding.inflate(inflater, container, false);
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
            binding.btnEmailContinue.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getOtpSentSuccess().observe(getViewLifecycleOwner(), sent -> {
            if (sent != null && sent) {
                ((AuthActivity) requireActivity()).navigateTo(new OTPVerificationFragment(), true);
            }
        });

        viewModel.getSocialLoginSuccess().observe(getViewLifecycleOwner(), res -> {
            if (res != null) {
                startActivity(new Intent(requireActivity(), MainScreenActivity.class));
                requireActivity().finish();
            }
        });
    }

    private void setupListeners() {
        binding.btnEmailContinue.setOnClickListener(v -> {
            String email = binding.edtRegisterEmail.getText().toString().trim();

            // set state
            viewModel.setCurrentEmail(email);
            viewModel.setCurrentOtpType("register");

            viewModel.sendOTP(email, "register");
        });

        binding.tvLogin.setOnClickListener(v -> {
            ((AuthActivity) requireActivity()).navigateTo(new LoginFragment(), true);
        });

        binding.btnGoogle.setOnClickListener(v -> ((AuthActivity) requireActivity()).triggerGoogleLogin());
        binding.btnFacebook.setOnClickListener(v -> ((AuthActivity) requireActivity()).triggerFacebookLogin());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
