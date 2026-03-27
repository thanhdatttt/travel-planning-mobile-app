package com.example.travelplanning.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.travelplanning.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminFilterDialog extends BottomSheetDialogFragment {

    public interface OnFilterApplyListener {
        void onApply(boolean isBanned, boolean isInActive, String sortBy, String sortOrder, List<String> roles);

    }

    private OnFilterApplyListener listener;
    // Khai báo các biến lưu giá trị mặc định/hiện tại
    private String selectedRole = "all";
    private String selectedSortBy = "username";
    private String selectedSortOrder = "asc";

    public AdminFilterDialog(OnFilterApplyListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // Sử dụng ViewBinding hoặc findViewById cho layout của bạn
        View view = inflater.inflate(R.layout.layout_filter_user, container, false);

        // 1. Setup Dropdowns (ComboBox)
        setupDropdowns(view);

        // 2. Xử lý nút Reset
        view.findViewById(R.id.btnReset).setOnClickListener(v -> resetFilters(view));

        // 3. Xử lý nút Filter (Tìm kiếm)
        view.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            boolean isBanned = ((Chip) view.findViewById(R.id.chipBanned)).isChecked();
            boolean isInActive = ((Chip) view.findViewById(R.id.chipInactive)).isChecked();

            // Nếu chưa bọc, bạn check từng cái manually:
            List<String> roles = new ArrayList<>();
            if (((Chip) view.findViewById(R.id.chipAdmin)).isChecked()) roles.add("admin");
            if (((Chip) view.findViewById(R.id.chipModerator)).isChecked()) roles.add("moderator");
            if (((Chip) view.findViewById(R.id.chipNormalUser)).isChecked()) roles.add("user");
            String selectedRole = roles.isEmpty() ? "all" : String.join(",", roles);

            listener.onApply(isBanned, isInActive, selectedSortBy, selectedSortOrder, roles);
            dismiss();
        });

        view.findViewById(R.id.btnClose).setOnClickListener(v -> dismiss());

        return view;
    }

    private void setupDropdowns(View v) {
        AutoCompleteTextView actvBy = v.findViewById(R.id.actvSortBy);
        AutoCompleteTextView actvOrder = v.findViewById(R.id.actvSortOrder);

        String[] byOptions = {"username", "email", "created_at"};
        String[] orderOptions = {"asc", "desc"};

        actvBy.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, byOptions));
        actvOrder.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, orderOptions));

        actvBy.setOnItemClickListener((parent, view, position, id) -> selectedSortBy = byOptions[position]);
        actvOrder.setOnItemClickListener((parent, view, position, id) -> selectedSortOrder = orderOptions[position]);
    }

    private void resetFilters(View v) {
        ((Chip) v.findViewById(R.id.chipBanned)).setChecked(false);
        ((Chip) v.findViewById(R.id.chipInactive)).setChecked(false);
        ((Chip) v.findViewById(R.id.chipAdmin)).setChecked(false);
        ((Chip) v.findViewById(R.id.chipModerator)).setChecked(false);
        ((Chip) v.findViewById(R.id.chipNormalUser)).setChecked(false);
        ((AutoCompleteTextView) v.findViewById(R.id.actvSortBy)).setText("", false);
        ((AutoCompleteTextView) v.findViewById(R.id.actvSortOrder)).setText("", false);
        selectedRole = "all";
        selectedSortBy = "username";
        selectedSortOrder = "asc";
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}