package com.example.travelplanning.ui.util;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import com.example.travelplanning.R;
import com.google.android.material.snackbar.Snackbar;

public class SnackBarHelper {
    public enum SnackBarType {
        SUCCESS, ERROR, WARNING, INFO
    }

    public static void showTopSnackBar(View view, String message, SnackBarType type) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();

        // choose color
        int colorRes;
        switch (type) {
            case SUCCESS: colorRes = R.color.primary_variant; break;
            case ERROR: colorRes = R.color.error; break;
            case WARNING: colorRes = R.color.warning; break;
            default: colorRes = R.color.info; break;
        }

        int color = ContextCompat.getColor(view.getContext(), colorRes);

        // config style for snackbar
        snackbar.setBackgroundTint(color);
        snackbar.setTextColor(Color.WHITE);

        // move position to top
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP;
        params.setMargins(0, 100, 0, 0);
        snackbarView.setLayoutParams(params);

        snackbar.show();
    }
}