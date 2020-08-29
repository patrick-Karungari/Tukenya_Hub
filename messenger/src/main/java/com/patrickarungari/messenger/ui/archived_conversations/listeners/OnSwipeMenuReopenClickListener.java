package com.patrickarungari.messenger.ui.archived_conversations.listeners;

import com.patrickarungari.messenger.core.conversations.models.Conversation;

public interface OnSwipeMenuReopenClickListener {
    void onSwipeMenuReopened(Conversation conversation, int position);
}
