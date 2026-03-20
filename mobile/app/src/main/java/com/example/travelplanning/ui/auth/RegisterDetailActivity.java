package com.example.travelplanning.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.databinding.ActivityRegisterDetailBinding;
import com.example.travelplanning.viewmodel.auth.AuthViewModel;

public class RegisterDetailActivity extends AppCompatActivity {
    private ActivityRegisterDetailBinding binding;
    private AuthViewModel viewModel;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        email = getIntent().getStringExtra("email");

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnCreateAccount.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getRegisterSuccess().observe(this, success -> {
            if (success != null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish(); // close this activity and go back to login
            }
        });
    }

    private void setupListeners() {
        binding.btnCreateAccount.setOnClickListener(v -> {
            String username = binding.edtRegisterUser.getText().toString().trim();
            String pass = binding.edtRegisterPass.getText().toString().trim();
            String confirmPass = binding.edtRegisterConfirm.getText().toString().trim();
            viewModel.register(email, username, pass, confirmPass);
        });
    }
}