package com.example.travelplanning.viewmodel.chat;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.travelplanning.data.model.chat.ChatMessage;
import com.example.travelplanning.data.repository.chat.ChatRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ChatViewModel extends AndroidViewModel {
    private final ChatRepository chatRepository;

    private final MutableLiveData<List<ChatMessage>> chatHistory = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    private String currentSessionId = null;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.chatRepository = new ChatRepository(application);
    }

    public void setSessionId(String sessionId) {
        this.currentSessionId = sessionId;
    }

    public void sendMessage(String content) {
        List<ChatMessage> currentHistory = chatHistory.getValue();
        if (currentHistory == null) currentHistory = new ArrayList<>();
        
        currentHistory.add(ChatMessage.builder().role("user").content(content).build());
        chatHistory.setValue(currentHistory);

        isLoading.setValue(true);

        chatRepository.sendMessage(content, currentSessionId, new ChatRepository.SendMessageCallback() {
            @Override
            public void onSuccess(String newSessionId, ChatMessage responseMessage) {
                isLoading.setValue(false);
                currentSessionId = newSessionId;
                
                List<ChatMessage> updatedHistory = chatHistory.getValue();
                if(updatedHistory != null){
                    updatedHistory.add(responseMessage);
                    chatHistory.setValue(updatedHistory);
                }
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
}