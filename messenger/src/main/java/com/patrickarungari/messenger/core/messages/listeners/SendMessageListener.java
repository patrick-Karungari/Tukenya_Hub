package com.patrickarungari.messenger.core.messages.listeners;

import com.patrickarungari.messenger.core.exception.ChatRuntimeException;
import com.patrickarungari.messenger.core.messages.models.Message;

/**
 * Created by andrealeo on 24/11/17.
 */

public interface SendMessageListener {

    void onBeforeMessageSent(Message message, ChatRuntimeException chatException);

    void onMessageSentComplete(Message message, ChatRuntimeException chatException);
}