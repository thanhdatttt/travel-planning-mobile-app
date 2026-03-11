package com.example.travelplanning.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.travelplanning.R;
import com.example.travelplanning.databinding.ActivityMainBinding;
import com.example.travelplanning.ui.mainscreen.MainScreenActivity;
import com.example.travelplanning.ui.splash.SplashActivity;

import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private boolean isReady = false;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        splashScreen.setKeepOnScreenCondition(() -> !isReady);

        EdgeToEdge.enable(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isReady = true; //let splash screen dismiss
            navigateToMainScreen();
        }, 2000);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void navigateToMainScreen() {
        Intent intent = new Intent(MainActivity.this, MainScreenActivity.class);
        startActivity(intent);
        finish();
    }

}