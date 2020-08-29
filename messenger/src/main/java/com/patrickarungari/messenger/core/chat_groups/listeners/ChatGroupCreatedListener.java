package com.patrickarungari.messenger.core.chat_groups.listeners;

import com.patrickarungari.messenger.core.chat_groups.models.ChatGroup;
import com.patrickarungari.messenger.core.exception.ChatRuntimeException;

/**
 * Created by stefanodp91 on 29/01/18.
 */

public interface ChatGroupCreatedListener {
    void onChatGroupCreated(ChatGroup chatGroup, ChatRuntimeException chatException);
}
