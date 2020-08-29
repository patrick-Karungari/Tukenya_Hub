package com.patrickarungari.messenger.core.conversations.listeners;

import com.patrickarungari.messenger.core.conversations.models.Conversation;
import com.patrickarungari.messenger.core.exception.ChatRuntimeException;

/**
 * Created by andrealeo on 06/12/17.
 */

public interface ConversationsListener {

    public void onConversationAdded(Conversation conversation, ChatRuntimeException e);

    public void onConversationChanged(Conversation conversation, ChatRuntimeException e);

    public void onConversationRemoved(ChatRuntimeException e);

}

