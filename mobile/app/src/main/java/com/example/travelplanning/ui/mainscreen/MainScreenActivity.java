package com.example.travelplanning.ui.mainscreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import android.view.View;
import com.example.travelplanning.R;
import com.example.travelplanning.databinding.ActivityMainScreenBinding;
import com.example.travelplanning.ui.util.LocaleHelper;

public class MainScreenActivity extends AppCompatActivity {
    private ActivityMainScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        SharedPreferences prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDark ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        binding = ActivityMainScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            android.graphics.Rect r = new android.graphics.Rect();
            binding.getRoot().getWindowVisibleDisplayFrame(r);
            int screenHeight = binding.getRoot().getRootView().getHeight();
            
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) { 
                binding.bottomNavigation.setVisibility(View.GONE);
            } else { 
                binding.bottomNavigation.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        if (intent != null && intent.hasExtra("ACTION_OPEN_DETAIL")) {
            String locationId = intent.getStringExtra("ACTION_OPEN_DETAIL");
            
            Bundle bundle = new Bundle();
            bundle.putString("location_id", locationId);
            
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment != null) {
                navHostFragment.getNavController().navigate(R.id.nav_location_detail, bundle);
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }

    public void setBottomNavVisibility(int visibility) {
        if (binding != null) {
            binding.bottomNavigation.setVisibility(visibility);
        }
    }
}