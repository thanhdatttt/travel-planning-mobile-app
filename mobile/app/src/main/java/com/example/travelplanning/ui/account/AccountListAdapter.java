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

public class AccountListAdapter extends ListAdapter<AccountOption, AccountListAdapter.AccountViewHolder> {

    private static final DiffUtil.ItemCallback<AccountOption> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<AccountOption>() {
            @Override
            public boolean areItemsTheSame(@NonNull AccountOption oldItem, @NonNull AccountOption newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull AccountOption oldItem, @NonNull AccountOption newItem) {
                // So sánh toàn bộ nội dung của object
                return oldItem.getIconRes() == newItem.getIconRes() &&
                        oldItem.getTitle().equals(newItem.getTitle());
            }
        };

    public interface OnItemClickListener {
        void onItemClick(AccountOption option);
    }
    private final OnItemClickListener listener;

    protected AccountListAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
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
        AccountOption option = getItem(position);
        holder.bind(option);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(option));
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
            tvTitle.setText(option.getTitle());
            ivIcon.setImageResource(option.getIconRes());
        }
    }


}