package com.patrickarungari.messenger.ui.conversations.listeners;

import com.patrickarungari.messenger.core.conversations.models.Conversation;

public interface OnSwipeMenuCloseClickListener {
    void onSwipeMenuClosed(Conversation conversation, int position);
}
