package com.patrickarungari.messenger.ui.conversations.listeners;

import com.patrickarungari.messenger.core.conversations.models.Conversation;

public interface OnSwipeMenuUnreadClickListener {
    void onSwipeMenuUnread(Conversation conversation, int position);
}
