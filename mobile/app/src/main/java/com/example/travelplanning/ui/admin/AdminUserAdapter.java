package com.example.travelplanning.ui.admin;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.data.model.profile.UserRole;
import com.example.travelplanning.databinding.AdminItemUserBinding;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {
    private List<UserProfile> users;
    private final OnUserOptionClickListener listener;

    public interface OnUserOptionClickListener {
        void onOptionClick(View anchor, UserProfile user);
    }

    public AdminUserAdapter(List<UserProfile> users, OnUserOptionClickListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminItemUserBinding binding = AdminItemUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final AdminItemUserBinding binding;

        public UserViewHolder(AdminItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(UserProfile user, OnUserOptionClickListener listener) {
            binding.tvUsername.setText(user.getUsername());
            binding.tvEmail.setText(user.getEmail());

            // Hiển thị Role lên TextView tvRole trong XML của bạn
            String roleName = user.getRole() != null ? user.getRole().name() : "USER";
            binding.tvRole.setText(roleName.toLowerCase());

            // Logic màu sắc theo Role
            int color;
            if (user.getRole() == UserRole.ADMIN) {
                color = Color.RED;
            } else if (user.getRole() == UserRole.MODERATOR) {
                color = Color.BLUE;
            } else {
                color = Color.parseColor("#4CAF50");
            }

            int strokeColor;
            if(user.getIsDeleted()) strokeColor = Color.GRAY;
            else if(user.getIsBanned()) strokeColor = Color.RED;
            else strokeColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.dark_green);

            binding.ivAvatar.setStrokeColor(ColorStateList.valueOf(strokeColor));

            binding.tvRole.setTextColor(color);

            binding.ivOptions.setOnClickListener(v -> listener.onOptionClick(v, user));
        }
    }
}