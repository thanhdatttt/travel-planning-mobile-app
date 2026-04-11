package com.example.travelplanning.ui.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.profile.UserProfile;
import com.example.travelplanning.databinding.AdminItemUserSimpleBinding; // Giả sử tên file XML của bạn là item_admin_stat_user.xml
import java.util.ArrayList;
import java.util.List;

public class AdminRoleUserAdapter extends RecyclerView.Adapter<AdminRoleUserAdapter.UserViewHolder> {
    private List<UserProfile> userList = new ArrayList<>();

    public void setData(List<UserProfile> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AdminItemUserSimpleBinding binding = AdminItemUserSimpleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserProfile user = userList.get(position);
        holder.binding.tvUsername.setText(user.getFullName());
        holder.binding.tvEmail.setText(user.getEmail());

        Glide.with(holder.itemView.getContext())
                .load(user.getAvatarUrl())
                .placeholder(R.drawable.no_avatar)
                .into(holder.binding.ivAvatar);
    }

    @Override
    public int getItemCount() { return userList.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        AdminItemUserSimpleBinding binding;
        UserViewHolder(AdminItemUserSimpleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}