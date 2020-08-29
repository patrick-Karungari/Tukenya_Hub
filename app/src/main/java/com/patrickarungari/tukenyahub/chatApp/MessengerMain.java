package com.patrickarungari.tukenyahub.chatApp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import com.patrickarungari.messenger.connectivity.AbstractNetworkReceiver;
import com.patrickarungari.messenger.core.ChatManager;
import com.patrickarungari.messenger.core.messages.models.Message;
import com.patrickarungari.messenger.core.users.models.IChatUser;
import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.messenger.ui.chat_groups.activities.AddMemberToChatGroupActivity;
import com.patrickarungari.messenger.ui.contacts.fragments.ContactsListFragment;
import com.patrickarungari.messenger.ui.contacts.listeners.OnContactClickListener;
import com.patrickarungari.messenger.ui.messages.activities.MessageListActivity;
import com.patrickarungari.messenger.ui.users.activities.PublicProfileActivity;
import com.patrickarungari.messenger.utils.image.CropCircleTransformation;
import com.patrickarungari.tukenyahub.Activities.MainActivity;
import com.patrickarungari.tukenyahub.Modules.animation;
import com.patrickarungari.tukenyahub.R;
import com.patrickarungari.tukenyahub.chatApp.Messenger.ActivityNotifications;
import com.patrickarungari.tukenyahub.chatApp.ui.main.SectionsPagerAdapter;
import com.pushlink.android.PushLink;

import static com.patrickarungari.messenger.ui.ChatUI.REQUEST_CODE_CREATE_GROUP;

public class MessengerMain extends AppCompatActivity implements OnContactClickListener {

    private ImageView mGroupIcon;
    private LinearLayout mBoxCreateGroup;
    private ContactsListFragment contactsListFragment;
    private static final String TAG = "Messenger_Main: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.title);
        setSupportActionBar(toolbar);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        contactsListFragment = new ContactsListFragment();
        contactsListFragment.setOnContactClickListener(this);
        ChatUI.getInstance().processRemoteNotification(getIntent());
     // mBoxCreateGroup = (LinearLayout) findViewById(com.patrickarungari.messenger.R.id.box_create_group);
      //  mGroupIcon = (ImageView) findViewById(com.patrickarungari.messenger.R.id.group_icon);
        //initBoxCreateGroup();
    }
    @Override
    public void onContactClicked(IChatUser contact, int position) {
        Log.d(TAG, "ContactListActivity.onRecyclerItemClicked:" +
                " contact == " + contact.toString() + ", position ==  " + position);

        if (ChatUI.getInstance().getOnContactClickListener() != null) {
            ChatUI.getInstance().getOnContactClickListener().onContactClicked(contact, position);
        }

        // start the conversation activity
        startMessageListActivity(contact);
    }
    @Override
    protected void onResume() {
        super.onResume();
        PushLink.setCurrentActivity(this);
    }
    private void startMessageListActivity(IChatUser contact) {
        Log.d(TAG, "ContactListActivity.startMessageListActivity");

        Intent intent = new Intent(this, MessageListActivity.class);
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, contact);
        intent.putExtra(ChatUI.BUNDLE_CHANNEL_TYPE, Message.DIRECT_CHANNEL_TYPE);

        startActivity(intent);

        // finish the contact list activity when it start a new conversation
        //finish();
    }

    private void initBoxCreateGroup() {
        Log.d(TAG, "ContactListActivity.initBoxCreateGroup");

        if (ChatUI.getInstance().areGroupsEnabled()) {
            Glide.with(getApplicationContext())
                    .load("")
                    .placeholder(com.patrickarungari.messenger.R.drawable.ic_group_avatar)
                    .transform(new CropCircleTransformation(getApplicationContext()))
                    .into(mGroupIcon);

            // box click
            mBoxCreateGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (AbstractNetworkReceiver.isConnected(getApplicationContext())) {

                        if (ChatUI.getInstance().getOnCreateGroupClickListener() != null) {
                            ChatUI.getInstance().getOnCreateGroupClickListener()
                                    .onCreateGroupClicked();
                        }

                        startCreateGroupActivity();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                getString(com.patrickarungari.messenger.R.string.activity_contact_list_error_cannot_create_group_offline_label),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mBoxCreateGroup.setVisibility(View.VISIBLE);
        } else {
            mBoxCreateGroup.setVisibility(View.GONE);
        }
    }

    private void startCreateGroupActivity() {
        Log.d(TAG, "ContactListActivity.startCreateGroupActivity");

//        Intent intent = new Intent(this, AddMembersToGroupActivity.class);
        Intent intent = new Intent(this, AddMemberToChatGroupActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CREATE_GROUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE_GROUP) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Handle item selection
        if (id == R.id.logout) {
            ChatManager.getInstance().performLogout(() -> {
                Intent intent = new Intent(MessengerMain.this, MainActivity.class);
                // clear the activity stack
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent); // start the main activity
                animation.slideDown(MessengerMain.this);
                finish(); // finish this activity
            });
            return true;
        }
        if (id == R.id.menu_profile) {

            Intent intent = new Intent(MessengerMain.this,
                    PublicProfileActivity.class);

            intent.putExtra(ChatUI.BUNDLE_RECIPIENT, ChatManager.getInstance().getLoggedUser());
            startActivity(intent);
            return true;
        }

        if (id == R.id.menu_addContacts) {
            //startActivity(new Intent(this, AddContactActivity.class));
            return true;
        }



        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onPostResume() {

        super.onPostResume();
        //lastOnlineRef.setValue(ServerValue.TIMESTAMP);
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        //lastOnlineRef.setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public void onBackPressed() {
        //lastOnlineRef.setValue(ServerValue.TIMESTAMP);
        startActivity(new Intent(MessengerMain.this, MainActivity.class));
        // Bungee.slideRight(this);
    }
}