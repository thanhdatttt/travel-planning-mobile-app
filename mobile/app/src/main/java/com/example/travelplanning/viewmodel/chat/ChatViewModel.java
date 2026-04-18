package com.example.travelplanning.ui.chat;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.travelplanning.data.model.chat.ChatMessage;
import com.example.travelplanning.data.model.chat.ChatSession;
import com.example.travelplanning.data.repository.chat.ChatRepository;
import com.example.travelplanning.data.remote.core.MetaResponse;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends AndroidViewModel {
    private final ChatRepository chatRepository;

    private final MutableLiveData<List<ChatMessage>> _messages = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<ChatMessage>> getMessages() { return _messages; }

    private final MutableLiveData<List<ChatSession>> _sessions = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<ChatSession>> getSessions() { return _sessions; }

    private final MutableLiveData<Boolean> _isTyping = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsTyping() { return _isTyping; }

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public LiveData<String> getToastMessage() { return _toastMessage; }

    private final MutableLiveData<String> _snackbarError = new MutableLiveData<>();
    public LiveData<String> getSnackbarError() { return _snackbarError; }

    private String currentSessionId = null;
    private String lastFailedContent = null;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        chatRepository = new ChatRepository(application.getApplicationContext());
        fetchChatSessions();
    }

    public void fetchChatSessions() {
        chatRepository.getAllSessions(1, 20, new ChatRepository.GetAllSessionsCallback() {
            @Override
            public void onSuccess(List<ChatSession> sessions, MetaResponse meta) {
                _sessions.postValue(sessions);
            }
            @Override
            public void onError(String errorMessage) {
                _toastMessage.postValue("Lỗi lấy danh sách chat: " + errorMessage);
            }
        });
    }

    public void startNewChat() {
        currentSessionId = null;
        _messages.setValue(new ArrayList<>()); 
    }

    public void loadMessagesForSession(String sessionId) {
        currentSessionId = sessionId;
        chatRepository.getSessionMessages(sessionId, new ChatRepository.GetSessionMessagesCallback() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                _messages.postValue(messages);
            }
            @Override
            public void onError(String errorMessage) {
                _toastMessage.postValue("Không thể tải lịch sử chat");
            }
        });
    }

    public void sendMessage(String content) {
        if (content == null || content.trim().isEmpty()) return;

        _isTyping.setValue(true);

        List<ChatMessage> currentList = new ArrayList<>(_messages.getValue() != null ? _messages.getValue() : new ArrayList<>());
        ChatMessage tempUserMsg = new ChatMessage();
        tempUserMsg.setRole("user");
        tempUserMsg.setContent(content);
        currentList.add(tempUserMsg);
        _messages.setValue(currentList);

        chatRepository.sendMessage(content, currentSessionId, new ChatRepository.SendMessageCallback() {
            @Override
            public void onSuccess(String sessionId, ChatMessage botResponse) {
                if (currentSessionId == null) {
                    currentSessionId = sessionId;
                    fetchChatSessions(); 
                }
                
                List<ChatMessage> updatedList = new ArrayList<>(_messages.getValue());
                updatedList.add(botResponse);
                _messages.postValue(updatedList);
                _isTyping.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                List<ChatMessage> updatedList = new ArrayList<>(_messages.getValue());
                updatedList.remove(tempUserMsg);
                _messages.postValue(updatedList);
                
                _isTyping.postValue(false);
                lastFailedContent = content; 
                _snackbarError.postValue(errorMessage);
            }
        });
    }

    public void retryLastMessage() {
        if (lastFailedContent != null) {
            sendMessage(lastFailedContent);
            lastFailedContent = null;
        }
    }

    public void deleteSession(String sessionId) {
        chatRepository.deleteSession(sessionId, new ChatRepository.DeleteSessionCallback() {
            @Override
            public void onSuccess() {
                if (sessionId.equals(currentSessionId)) {
                    startNewChat(); 
                }
                fetchChatSessions(); 
                _toastMessage.postValue("Đã xóa cuộc trò chuyện");
            }

            @Override
            public void onError(String errorMessage) {
                _toastMessage.postValue("Lỗi xóa: " + errorMessage);
            }
        });
    }

    public void clearToast() { _toastMessage.setValue(null); }
    public void clearSnackbar() { _snackbarError.setValue(null); }
}