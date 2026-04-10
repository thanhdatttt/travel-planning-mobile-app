package com.example.travelplanning.ui.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.travelplanning.R;
import com.example.travelplanning.data.model.chat.ChatSession;
import com.example.travelplanning.databinding.FragmentChatbotBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ChatbotFragment extends Fragment {

    private FragmentChatbotBinding binding;
    private ChatViewModel viewModel;
    
    private ChatAdapter chatAdapter;
    private ChatSessionAdapter sessionAdapter;
    private final List<ChatSession> sessionList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatbotBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setupAdapters();
        setupKeyboardInsets();
        setupClickListeners();
        setupKeyboardHide();

        setupObservers();
    }

    private void setupObservers() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            chatAdapter.updateData(messages);
            if (messages.isEmpty()) {
                binding.layoutWelcome.setVisibility(View.VISIBLE);
                binding.recyclerViewChat.setVisibility(View.GONE);
            } else {
                binding.layoutWelcome.setVisibility(View.GONE);
                binding.recyclerViewChat.setVisibility(View.VISIBLE);
                binding.recyclerViewChat.scrollToPosition(messages.size() - 1);
            }
        });

        viewModel.getSessions().observe(getViewLifecycleOwner(), sessions -> {
            sessionList.clear();
            sessionList.addAll(sessions);
            sessionAdapter.notifyDataSetChanged();
        });

        viewModel.getIsTyping().observe(getViewLifecycleOwner(), isTyping -> {
            binding.layoutTypingIndicator.setVisibility(isTyping ? View.VISIBLE : View.GONE);
            binding.btnSend.setEnabled(!isTyping);
        });

        viewModel.getToastMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                viewModel.clearToast();
            }
        });

        viewModel.getSnackbarError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                Snackbar.make(requireView(), "Lỗi: " + errorMsg, Snackbar.LENGTH_LONG)
                        .setAction("Thử lại", v -> viewModel.retryLastMessage())
                        .show();
                viewModel.clearSnackbar();
            }
        });
    }

    private void setupAdapters() {
        chatAdapter = new ChatAdapter(new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        binding.recyclerViewChat.setLayoutManager(layoutManager);
        binding.recyclerViewChat.setAdapter(chatAdapter);

        sessionAdapter = new ChatSessionAdapter(sessionList,
            session -> { 
                viewModel.loadMessagesForSession(session.getId());
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            },
            (session, position) -> { 
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.dialog_delete_session_title)
                        .setMessage(R.string.dialog_delete_session_message)
                        .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                            viewModel.deleteSession(session.getId());
                        })
                        .setNegativeButton(R.string.action_cancel, null)
                        .show();
            }
        );
        binding.recyclerViewSessions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewSessions.setAdapter(sessionAdapter);
        binding.drawerLayout.setScrimColor(android.graphics.Color.parseColor("#99000000"));
    }

    private void setupClickListeners() {
        binding.btnSend.setOnClickListener(v -> {
            String content = binding.editTextMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                viewModel.sendMessage(content);
                binding.editTextMessage.setText(""); 
            }
        });

        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        
        binding.btnMenu.setOnClickListener(v -> {
            if (!binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        binding.btnNewChat.setOnClickListener(v -> {
            viewModel.startNewChat();
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        });
    }

    private void setupKeyboardInsets() {
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
    }

    private void hideKeyboard() {
        View view = requireActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            binding.editTextMessage.clearFocus(); // Làm mất con trỏ nhấp nháy
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

}