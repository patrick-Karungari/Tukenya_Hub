package com.patrickarungari.messenger.utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.style.URLSpan;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.patrickarungari.messenger.R;
import com.patrickarungari.messenger.core.ChatManager;
import com.patrickarungari.messenger.core.users.models.IChatUser;
import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.messenger.ui.contacts.activites.ContactListActivity;
import com.patrickarungari.messenger.ui.conversations.listeners.OnNewConversationClickListener;
import com.patrickarungari.messenger.ui.messages.listeners.OnMessageClickListener;


import static com.patrickarungari.messenger.core.ChatManager._SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER;

public class ChatApplication extends Application {
    final String TAG = getClass().getSimpleName();
    Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initChatSDK();
    }
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
      public void initChatSDK() {
        //enable persistence must be made before any other usage of FirebaseDatabase instance.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // it creates the chat configurations
        ChatManager.Configuration mChatConfiguration =
                new ChatManager.Configuration.Builder(getString(R.string.chat_firebase_appId))
                        .firebaseUrl(getString(R.string.chat_firebase_url))
                        .storageBucket(getString(R.string.chat_firebase_storage_bucket))
                        .build();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //assuming you have a login, check if the logged user (converted to IChatUser) is valid
        //if (currentUser != null) {
        if (currentUser != null) {
            IChatUser iChatUser = (IChatUser) IOUtils.getObjectFromFile(context,
                    _SERIALIZED_CHAT_CONFIGURATION_LOGGED_USER);
            ChatManager.start(this, mChatConfiguration, iChatUser);
            Log.i(TAG, "chat has been initialized with success");
            ChatUI.getInstance().setContext(context);
            ChatUI.getInstance().enableGroups(true);
            ChatUI.getInstance().setOnMessageClickListener((OnMessageClickListener) (message, clickableSpan) -> {
                String text = ((URLSpan) clickableSpan).getURL();
                Uri uri = Uri.parse(text);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(browserIntent);
            });
            // set on new conversation click listener
            //final IChatUser support = new ChatUser("support", "Chat21 Support");
            final IChatUser support = null;
            ChatUI.getInstance().setOnNewConversationClickListener((OnNewConversationClickListener) () -> {
                if (support != null) {
                    ChatUI.getInstance().openConversationMessagesActivity(support);
                } else {
                    Intent intent = new Intent(context, ContactListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // start activity from context
                    startActivity(intent);
                }
            });
            Log.i(TAG, "ChatUI has been initialized with success");
        } else {
            Log.w(TAG, "chat can't be initialized because chatUser is null");
        }
    }
}
