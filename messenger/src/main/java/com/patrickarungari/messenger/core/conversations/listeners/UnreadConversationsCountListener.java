package com.patrickarungari.messenger.core.conversations.listeners;

import com.patrickarungari.messenger.core.exception.ChatRuntimeException;

public interface UnreadConversationsCountListener {
    void onUnreadConversationCount(int count, ChatRuntimeException e);
}
