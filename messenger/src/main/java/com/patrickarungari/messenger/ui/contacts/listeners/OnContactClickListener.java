package com.patrickarungari.messenger.ui.contacts.listeners;

import com.patrickarungari.messenger.core.users.models.IChatUser;

import java.io.Serializable;

/**
 * Created by stefanodp91 on 29/03/17.
 */
public interface OnContactClickListener extends Serializable {
    void onContactClicked(IChatUser contact, int position);
}
