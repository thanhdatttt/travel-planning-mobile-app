package com.example.travelplanning.ui.admin;

import com.bumptech.glide.Glide;
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
    private final OnUserOptionClickListener optionListener;
    private final OnUserClickListener itemListener;

    public interface OnUserOptionClickListener {
        void onOptionClick(View anchor, UserProfile user);
    }
    public interface OnUserClickListener {
        void onUserClick(UserProfile user);
    }

    public AdminUserAdapter(List<UserProfile> users,
                            OnUserOptionClickListener optionListener,
                            OnUserClickListener itemListener) {
        this.users = users;
        this.optionListener = optionListener;
        this.itemListener = itemListener;
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
        holder.bind(users.get(position), optionListener, itemListener);
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

        public void bind(UserProfile user, OnUserOptionClickListener optionListener, OnUserClickListener itemListener) {
            binding.tvUsername.setText(user.getUsername());
            binding.tvEmail.setText(user.getEmail());

            // RENDER AVATAR WITH GLIDE
            Glide.with(binding.ivAvatar.getContext())
                    .load(user.getAvatarUrl() != null ? user.getAvatarUrl() : R.drawable.no_avatar)
                    .circleCrop()
                    .placeholder(R.drawable.no_avatar)
                    .error(R.drawable.no_avatar)
                    .into(binding.ivAvatar);

            String roleName = user.getRole() != null ? user.getRole().name() : "USER";
            binding.tvRole.setText(roleName.toLowerCase());

            // Role Color Logic
            int color;
            if (user.getRole() == UserRole.ADMIN) {
                color = Color.RED;
            } else if (user.getRole() == UserRole.MODERATOR) {
                color = Color.BLUE;
            } else {
                color = Color.parseColor("#4CAF50");
            }
            binding.tvRole.setTextColor(color);

            // Stroke Color Logic
            int strokeColor;
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                strokeColor = Color.GRAY;
            } else if (Boolean.TRUE.equals(user.getIsBanned())) {
                strokeColor = Color.RED;
            } else {
                strokeColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.dark_green);
            }
            binding.ivAvatar.setStrokeColor(ColorStateList.valueOf(strokeColor));

            binding.getRoot().setOnClickListener(v -> itemListener.onUserClick(user));
            binding.ivOptions.setOnClickListener(v -> optionListener.onOptionClick(v, user));
        }
    }

    public void setData(List<UserProfile> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }
}