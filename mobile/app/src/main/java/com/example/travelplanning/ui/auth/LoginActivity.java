package com.example.travelplanning.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.travelplanning.R;
import com.example.travelplanning.databinding.ActivityLoginBinding;
import com.example.travelplanning.ui.mainscreen.MainScreenActivity;
import com.example.travelplanning.viewmodel.auth.AuthViewModel;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager facebookCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupGoogleLogin();
        setupFacebookLogin();
        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, loading -> {
            binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!loading);
        });


        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });


        viewModel.getLoginSuccess().observe(this, res -> {
            if (res != null) {
                startActivity(new Intent(this, MainScreenActivity.class));
                finish();
            }
        });

        viewModel.getSocialLoginSuccess().observe(this, res -> {
            if (res != null) {
                startActivity(new Intent(this, MainScreenActivity.class));
                finish();
            }
        });
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.edtLoginUsername.getText().toString().trim();
            String pass = binding.edtLoginPassword.getText().toString().trim();
            viewModel.login(email, pass);
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterEmailActivity.class));
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ResetEmailVerificationActivity.class));
        });

        binding.btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleLauncher.launch(signInIntent);
        });
        binding.btnFacebook.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        });
    }

    // google login
    private void setupGoogleLogin() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }
    private final ActivityResultLauncher<Intent> googleLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    // get id token and run view model
                    String idToken = account.getIdToken();
                    viewModel.loginWithGoogle(idToken);
                } catch (ApiException e) {
                    Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    );

    // facebook login
    private void setupFacebookLogin() {
        facebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // get access token and run view model
                String token = loginResult.getAccessToken().getToken();
                viewModel.loginWithFacebook(token);
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(@NonNull FacebookException error) {
                Toast.makeText(LoginActivity.this, "Facebook sign in failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
    // pass data to Facebook SDK
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}