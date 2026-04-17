package com.example.travelplanning.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.travelplanning.R;
import com.example.travelplanning.databinding.FragmentSettingsBinding;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        setupUIState(prefs);
        setupToolbar();
        setupListeners(prefs);
    }

    private void setupUIState(SharedPreferences prefs) {
        boolean isDark = prefs.getBoolean("dark_mode", false);
        binding.switchTheme.setChecked(isDark);
        binding.switchTheme.setText(isDark ? getString(R.string.theme_dark) : getString(R.string.theme_light));

        String lang = prefs.getString("lang", "en");
        binding.btnLanguage.setText(lang.equals("vi") ? getString(R.string.lang_vi) : getString(R.string.lang_en));
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void setupListeners(SharedPreferences prefs) {
        binding.switchTheme.setOnCheckedChangeListener((v, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();

            binding.switchTheme.setText(isChecked ? getString(R.string.theme_dark) : getString(R.string.theme_light));

            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        binding.btnLanguage.setOnClickListener(v -> {
            String currentLang = prefs.getString("lang", "en");
            String newLang = currentLang.equals("vi") ? "en" : "vi";

            prefs.edit().putString("lang", newLang).apply();

            // restarts the Activity and applies the attachBaseContext logic
            requireActivity().recreate();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}