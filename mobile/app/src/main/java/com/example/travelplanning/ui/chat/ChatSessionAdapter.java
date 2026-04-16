package com.example.travelplanning.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.travelplanning.R;
import com.example.travelplanning.data.model.chat.ChatSession;
import java.util.List;

public class ChatSessionAdapter extends RecyclerView.Adapter<ChatSessionAdapter.ViewHolder> {
    private List<ChatSession> sessionList;
    private final OnSessionClickListener listener;
    private final OnSessionDeleteListener deleteListener; 

    public interface OnSessionClickListener {
        void onSessionClick(ChatSession session);
    }
    public interface OnSessionDeleteListener {
        void onSessionDelete(ChatSession session, int position);
    }

    public ChatSessionAdapter(List<ChatSession> sessionList, OnSessionClickListener listener, OnSessionDeleteListener deleteListener) {
        this.sessionList = sessionList;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatSession session = sessionList.get(position);
        holder.tvTitle.setText(session.getTitle() != null ? session.getTitle() : "Cuộc trò chuyện mới");
        
        holder.itemView.setOnClickListener(v -> listener.onSessionClick(session));
        
        holder.btnDelete.setOnClickListener(v -> deleteListener.onSessionDelete(session, position));
    }

    @Override
    public int getItemCount() {
        return sessionList != null ? sessionList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        android.widget.ImageButton btnDelete; 

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSessionTitle);
            btnDelete = itemView.findViewById(R.id.btnDeleteSession);
        }
    }
}