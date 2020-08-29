package com.patrickarungari.messenger.core.contacts.listeners;

import com.patrickarungari.messenger.core.exception.ChatRuntimeException;

/**
 * Created by stefanodp91 on 28/02/18.
 */

public interface OnContactCreatedCallback {
    void onContactCreatedSuccess(ChatRuntimeException exception);
}
