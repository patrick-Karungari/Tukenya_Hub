package com.patrickarungari.messenger.core.conversations.listeners;

import com.patrickarungari.messenger.core.exception.ChatRuntimeException;

/**
 * Created by andrealeo on 06/12/17.
 */

public interface UnreadConversationsListener {

    void onUnreadConversationCounted(int count, ChatRuntimeException e);

}

