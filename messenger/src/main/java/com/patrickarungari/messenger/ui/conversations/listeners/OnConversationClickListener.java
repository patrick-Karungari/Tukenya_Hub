package com.patrickarungari.messenger.ui.conversations.listeners;

import com.patrickarungari.messenger.core.conversations.models.Conversation;

/**
 * Created by stefanodp91 on 07/12/17.
 */

public interface OnConversationClickListener {
    void onConversationClicked(Conversation conversation, int position);
}
