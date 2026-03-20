package com.example.travelplanning.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.ActivityEmailOtpVerificationBinding;
import com.example.travelplanning.viewmodel.auth.AuthViewModel;

public class OTPVerificationActivity extends AppCompatActivity {
    private ActivityEmailOtpVerificationBinding binding;
    private AuthViewModel viewModel;
    private String email, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get email and type from intent
        email = getIntent().getStringExtra("email");
        type = getIntent().getStringExtra("type");

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnVerifyOtp.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getOtpVerifySuccess().observe(this, verified -> {
            Toast.makeText(this, "Type nhận được: " + verified, Toast.LENGTH_SHORT).show();
            if (verified != null && verified) {
                if ("register".equals(type)) {
                    // OTP for register
                    startActivity(new Intent(this, RegisterDetailActivity.class)
                            .putExtra("email", email));
                } else {
                    // OTP for reset password
                    startActivity(new Intent(this, ResetPasswordActivity.class)
                            .putExtra("email", email));
                }
                finish(); // close this activity
            }
        });
    }

    private void setupListeners() {
        binding.btnVerifyOtp.setOnClickListener(v -> {
            String otp = binding.edtOtpCode.getText().toString().trim();
            viewModel.verifyOTP(email, otp, type);
        });

        binding.tvResend.setOnClickListener(v -> {
            viewModel.sendOTP(email, type);
        });
    }
}