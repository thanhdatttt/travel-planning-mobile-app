package com.example.travelplanning.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.ActivityEmailOtpVerificationBinding;
import com.example.travelplanning.viewmodel.auth.RegisterViewModel;

public class OTPVerificationActivity extends AppCompatActivity {
    private ActivityEmailOtpVerificationBinding binding;
    private RegisterViewModel viewModel;
    private String email, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmailOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // get email and type from intent
        email = getIntent().getStringExtra("email");
        type = getIntent().getStringExtra("type");

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.btnVerifyOtp.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getOtpVerified().observe(this, verified -> {
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
            viewModel.senOTP(email);
        });
    }
}