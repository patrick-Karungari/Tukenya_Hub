package com.patrickarungari.messenger.ui.chat_groups.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.patrickarungari.messenger.R;;
import com.patrickarungari.messenger.core.chat_groups.models.ChatGroup;
import com.patrickarungari.messenger.core.messages.models.Message;
import com.patrickarungari.messenger.core.users.models.ChatUser;
import com.patrickarungari.messenger.core.users.models.IChatUser;
import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.messenger.ui.chat_groups.fragments.ChatGroupsListFragment;
import com.patrickarungari.messenger.ui.chat_groups.listeners.OnGroupClickListener;
import com.patrickarungari.messenger.ui.messages.activities.MessageListActivity;

import static com.patrickarungari.messenger.ui.ChatUI.BUNDLE_CHANNEL_TYPE;

/**
 * Created by stefano on 25/08/2015.
 */
public class ChatGroupsListActivity extends AppCompatActivity implements OnGroupClickListener {
    private static final String TAG = ChatGroupsListActivity.class.getSimpleName();

    private ChatGroupsListFragment contactsListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_groups_list);

        contactsListFragment = new ChatGroupsListFragment();
        contactsListFragment.setOnChatGroupClickListener(this);

        // #### BEGIN TOOLBAR ####
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // #### END  TOOLBAR ####

        // #### BEGIN CONTAINER ####
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, contactsListFragment)
                .commit();
        // #### BEGIN CONTAINER ####
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "ContactListActivity.onOptionsItemSelected");

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGroupClicked(ChatGroup chatGroup, int position) {
        IChatUser groupRecipient = new ChatUser(chatGroup.getGroupId(), chatGroup.getName());

        // start the message list activity
        Intent intent = new Intent(ChatGroupsListActivity.this, MessageListActivity.class);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, groupRecipient);
        intent.putExtra(BUNDLE_CHANNEL_TYPE, Message.GROUP_CHANNEL_TYPE);
        startActivity(intent);
        finish();
    }
}