package com.example.travelplanning.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.ActivityRegisterEmailBinding;
import com.example.travelplanning.viewmodel.auth.RegisterViewModel;

public class RegisterEmailActivity extends AppCompatActivity {
    private ActivityRegisterEmailBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnEmailContinue.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getOtpSent().observe(this, sent -> {
            if (sent != null && sent) {
                // OTP sent successfully, navigate to OTP verification screen
                startActivity(new Intent(this, RegisterOTPActivity.class)
                        .putExtra("email", binding.edtEmail.getText().toString().trim()));
            }
        });
    }
}