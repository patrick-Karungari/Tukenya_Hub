package com.patrickarungari.messenger.ui.chat_groups.listeners;

import com.patrickarungari.messenger.core.chat_groups.models.ChatGroup;

/**
 * Created by stefanodp91 on 07/12/17.
 */

public interface OnGroupClickListener {
    void onGroupClicked(ChatGroup chatGroup, int position);
}
