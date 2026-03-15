package com.example.travelplanning.ui.account;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelplanning.R;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    private final List<AccountOption> items;
    private final OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(AccountOption option);
    }

    protected AccountAdapter(List<AccountOption> items ,OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account_menu, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        AccountOption option = items.get(position);
        holder.bind(option);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(option));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final ImageView ivIcon;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }

        public void bind(AccountOption option) {
            tvTitle.setText(itemView.getContext().getString(option.getTitleRes()));
            ivIcon.setImageResource(option.getIconRes());
        }
    }
}