package com.patrickarungari.messenger.core.messages.listeners;

import com.patrickarungari.messenger.core.messages.models.Message;
import com.patrickarungari.messenger.core.exception.ChatRuntimeException;

/**
 * Created by andrealeo on 06/12/17.
 */

public interface ConversationMessagesListener {

        public void onConversationMessageReceived(Message message, ChatRuntimeException e);
        public void onConversationMessageChanged(Message message, ChatRuntimeException e);

}

