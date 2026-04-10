package com.example.travelplanning.ui.chat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.core.view.GravityCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.travelplanning.data.model.chat.ChatSession;
import com.example.travelplanning.data.model.chat.ChatMessage;
import com.example.travelplanning.data.repository.chat.ChatRepository;
import com.example.travelplanning.databinding.FragmentChatbotBinding;
import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.travelplanning.R;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.appcompat.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.appcompat.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Rect;
import android.view.ViewTreeObserver;
public class ChatbotFragment extends Fragment {
    
    private ChatSessionAdapter sessionAdapter;
    private List<ChatSession> sessionList = new ArrayList<>();

    private FragmentChatbotBinding binding;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private ChatRepository chatRepository;
    
    private String currentSessionId = null; 

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatbotBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRepository = new ChatRepository(requireContext());
        
        setupKeyboardHide();

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            boolean isKeyboardVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.layoutInputBar.getLayoutParams();

            if (isKeyboardVisible) {
                binding.layoutInputBar.setBackgroundResource(R.color.background); 
                binding.layoutInputBar.setPadding(40, 24, 40, 24);
                params.bottomMargin = 0; 
            } else {
                binding.layoutInputBar.setBackgroundResource(R.drawable.bg_input_gemini_floating);
                binding.layoutInputBar.setPadding(50, 40, 50, 40);
                params.bottomMargin = 0; 
            }
            binding.layoutInputBar.setLayoutParams(params);
            return windowInsets; 
        });

        binding.drawerLayout.setScrimColor(android.graphics.Color.parseColor("#99000000"));

        sessionAdapter = new ChatSessionAdapter(sessionList, 
            session -> {
                loadMessagesForSession(session.getId());
            }, 
            (session, position) -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.dialog_delete_session_title)
                        .setMessage(R.string.dialog_delete_session_message)
                        .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                            handleDeleteSession(session.getId(), position);
                        })
                        .setNegativeButton(R.string.action_cancel, null)
                        .show();
            }
        );

        binding.recyclerViewSessions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewSessions.setAdapter(sessionAdapter);

        setupClickListeners();
        updateWelcomeScreenVisibility();
        fetchChatSessions();
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true); 
        
        binding.recyclerViewChat.setLayoutManager(layoutManager);
        binding.recyclerViewChat.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        binding.btnSend.setOnClickListener(v -> handleSendMessage(binding.editTextMessage.getText().toString().trim()));

        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnMenu.setOnClickListener(v -> {
            if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        binding.btnNewChat.setOnClickListener(v -> {
            currentSessionId = null;
            messageList.clear();
            chatAdapter.notifyDataSetChanged();
            updateWelcomeScreenVisibility();
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
        });

    }

    private void handleSendMessage(String content) {
        if (content.isEmpty()) return;
        binding.layoutWelcome.setVisibility(View.GONE);
        binding.recyclerViewChat.setVisibility(View.VISIBLE);
        binding.editTextMessage.setText("");

        ChatMessage tempUserMsg = new ChatMessage();
        tempUserMsg.setRole("user");
        tempUserMsg.setContent(content);
        
        messageList.add(tempUserMsg);
        int userMsgIndex = messageList.size() - 1;
        chatAdapter.notifyItemInserted(userMsgIndex);
        binding.recyclerViewChat.scrollToPosition(userMsgIndex);

        binding.layoutTypingIndicator.setVisibility(View.VISIBLE);
        binding.btnSend.setEnabled(false);

        chatRepository.sendMessage(content, currentSessionId, new ChatRepository.SendMessageCallback() {
            @Override
            public void onSuccess(String sessionId, ChatMessage botResponse) {
                if (getView() == null) return; 

                if (currentSessionId == null) {
                    currentSessionId = sessionId;
                }

                binding.layoutTypingIndicator.setVisibility(View.GONE);
                binding.btnSend.setEnabled(true);

                messageList.add(botResponse);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                binding.recyclerViewChat.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onError(String errorMessage) {
                if (getView() == null) return;

                binding.layoutTypingIndicator.setVisibility(View.GONE);
                
                binding.btnSend.setEnabled(true);

                messageList.remove(tempUserMsg);
                chatAdapter.notifyItemRemoved(userMsgIndex);
                
                updateWelcomeScreenVisibility();

                Snackbar.make(requireView(), "Lỗi: " + errorMessage + ". Tin nhắn chưa được gửi.", Snackbar.LENGTH_LONG)
                        .setAction("Thử lại", v -> handleSendMessage(content)) 
                        .show();
            }
        });
    }

    private void updateWelcomeScreenVisibility() {
        if (messageList.isEmpty()) {
            binding.layoutWelcome.setVisibility(View.VISIBLE);
            binding.recyclerViewChat.setVisibility(View.GONE);
        } else {
            binding.layoutWelcome.setVisibility(View.GONE);
            binding.recyclerViewChat.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            binding.editTextMessage.clearFocus();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupKeyboardHide() {
        View.OnTouchListener touchListener = (v, event) -> {
            hideKeyboard();
            return false; 
        };
        binding.recyclerViewChat.setOnTouchListener(touchListener);
        binding.layoutWelcome.setOnTouchListener(touchListener);
    }

    private void fetchChatSessions() {
        chatRepository.getAllSessions(1, 20, new ChatRepository.GetAllSessionsCallback() {
            @Override
            public void onSuccess(List<ChatSession> sessions, com.example.travelplanning.data.remote.core.MetaResponse meta) {
                if (getView() == null) return;
                sessionList.clear();
                sessionList.addAll(sessions);
                sessionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("UI_DEBUG_SESSION", "Lỗi UI khi lấy session: " + errorMessage);
            }
        });
    }  
    
    private void loadMessagesForSession(String sessionId) {
        binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
        currentSessionId = sessionId;
        
        chatRepository.getSessionMessages(sessionId, new ChatRepository.GetSessionMessagesCallback() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                if (getView() == null) return;
                messageList.clear();
                messageList.addAll(messages);
                chatAdapter.notifyDataSetChanged();
                updateWelcomeScreenVisibility(); 
                
                if (!messageList.isEmpty()) {
                    binding.recyclerViewChat.scrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(), "Không thể tải lịch sử chat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleDeleteSession(String sessionId, int position) {
        
        chatRepository.deleteSession(sessionId, new ChatRepository.DeleteSessionCallback() {
            @Override
            public void onSuccess() {
                if (getView() == null) return;
                
                sessionList.remove(position);
                sessionAdapter.notifyItemRemoved(position);
                
                if (sessionId.equals(currentSessionId)) {
                    currentSessionId = null;
                    messageList.clear();
                    chatAdapter.notifyDataSetChanged();
                    updateWelcomeScreenVisibility();
                    binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
                }
                
                Toast.makeText(requireContext(), "Đã xóa cuộc trò chuyện", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(requireContext(), "Lỗi xóa: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
    }

}