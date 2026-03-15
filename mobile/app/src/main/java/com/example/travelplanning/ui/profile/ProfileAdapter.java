package com.example.travelplanning.ui.profile;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.R;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    private List<ProfileItem> items;
    private boolean isEditMode = false;

    public void setEditMode(boolean mode) {
        this.isEditMode = mode;
        notifyDataSetChanged();
    }

    public ProfileAdapter(List<ProfileItem> items){
        this.items = items;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_info, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        ProfileItem item = items.get(position);
        holder.bind(item, isEditMode);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel, tvValue;
        EditText etValue;
        TextWatcher currentTextWatcher; // Lưu lại để tránh trùng lặp listener

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvValue = itemView.findViewById(R.id.tvValue);
            etValue = itemView.findViewById(R.id.etValue);
        }

        public void bind(ProfileItem item, boolean isEditMode) {
            tvLabel.setText(item.getLabel());

            // Xóa TextWatcher cũ trước khi bind dữ liệu mới để tránh lỗi ghi đè dữ liệu sai item
            if (currentTextWatcher != null) {
                etValue.removeTextChangedListener(currentTextWatcher);
            }

            if (isEditMode) {
                tvValue.setVisibility(View.GONE);
                etValue.setVisibility(View.VISIBLE);
                etValue.setText(item.getValue());

                currentTextWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        item.setValue(s.toString());
                    }
                };
                etValue.addTextChangedListener(currentTextWatcher);
            } else {
                tvValue.setVisibility(View.VISIBLE);
                etValue.setVisibility(View.GONE);
                tvValue.setText(item.getValue());
            }
        }
    }
}