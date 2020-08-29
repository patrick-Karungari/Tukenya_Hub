package com.patrickarungari.messenger.ui.messages.listeners;

import com.patrickarungari.messenger.core.users.models.IChatUser;

import java.io.Serializable;

/**
 * Created by stefanodp91 on 29/03/17.
 */
public interface OnAttachDocumentsClickListener<T> extends Serializable {
    void onAttachDocumentsClicked(IChatUser recipient, String channelType, T data);
}
