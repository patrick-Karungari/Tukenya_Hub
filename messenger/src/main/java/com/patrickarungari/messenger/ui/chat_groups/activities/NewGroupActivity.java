package com.patrickarungari.messenger.ui.chat_groups.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.patrickarungari.messenger.R;
import com.patrickarungari.messenger.core.ChatManager;
import com.patrickarungari.messenger.core.chat_groups.listeners.ChatGroupCreatedListener;
import com.patrickarungari.messenger.core.chat_groups.models.ChatGroup;
import com.patrickarungari.messenger.core.chat_groups.syncronizers.GroupsSyncronizer;
import com.patrickarungari.messenger.core.conversations.models.Conversation;
import com.patrickarungari.messenger.core.exception.ChatRuntimeException;
import com.patrickarungari.messenger.core.messages.models.Message;
import com.patrickarungari.messenger.core.users.models.IChatUser;
import com.patrickarungari.messenger.databinding.ActivityNewGroupBinding;
import com.patrickarungari.messenger.ui.chat_groups.WizardNewGroup;
import com.patrickarungari.messenger.ui.chat_groups.adapters.SelectedContactListAdapter;
import com.patrickarungari.messenger.ui.chat_groups.fragments.BottomSheetGroupAdminPanelMember;
import com.patrickarungari.messenger.utils.DebugConstants;
import com.patrickarungari.messenger.utils.StringUtils;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by stefanodp91 on 26/01/18.
 */

public class NewGroupActivity extends AppCompatActivity {

    private static final String TAG = "NEW_GROUP_ACTIVITY";
    private GroupsSyncronizer groupsSyncronizer;
    private ActivityNewGroupBinding mBinding;
    private EmojiEditText groupNameView;
    private MenuItem actionNextMenuItem;
    private List<IChatUser> selectedList;
    private SelectedContactListAdapter selectedContactsListAdapter;
    private EmojiPopup emojiPopup;
    private ImageView emojiButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityNewGroupBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());


        groupsSyncronizer = ChatManager.getInstance().getGroupsSyncronizer();
        emojiButton = mBinding.keyboard;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        selectedList = (ArrayList<IChatUser>) args.getSerializable("ARRAYLIST");
        LinearLayoutManager layoutManager =
                new GridLayoutManager(this, 4);
        mBinding.selectedList.setLayoutManager(layoutManager);
        mBinding.selectedList.setItemAnimator(new DefaultItemAnimator());
        updateSelectedContactListAdapter(selectedList, 0);

        mBinding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                BottomSheetGroupAdminPanelMember dialog = BottomSheetGroupAdminPanelMember
                        .newInstance(R.layout.fragment_bottom_sheet_set_profile_photo, "group");
                dialog.show(ft, BottomSheetGroupAdminPanelMember.TAG);
            }
        });
        groupNameView = mBinding.groupName;
        groupNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (actionNextMenuItem != null) {
                    if (isValidGroupName() && actionNextMenuItem != null) {
                        WizardNewGroup.getInstance().getTempChatGroup().setName(groupNameView.getText().toString());
                        actionNextMenuItem.setVisible(true);
                    } else {
                        actionNextMenuItem.setVisible(false);
                    }
                }
            }
        });
        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                emojiPopup.toggle();
            }
        });
        setUpEmojiPopup();
    }

    @Override
    protected void onStop() {
        //Log.d(TAG, "  MessageListActivity.onStop");

        // dismiss the emoji panel
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        //Log.d(TAG, "onBackPressed");

        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }


    private void updateSelectedContactListAdapter(List<IChatUser> selectedList, int i) {
        selectedContactsListAdapter = new SelectedContactListAdapter(this, selectedList);
        mBinding.selectedList.setAdapter(selectedContactsListAdapter);
        mBinding.textView3.setText(MessageFormat.format("Participants : {0}", selectedContactsListAdapter.getItemCount()));
        mBinding.selectedList.smoothScrollToPosition(i);
    }

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(mBinding.getRoot())
                .setIconColor(ContextCompat.getColor(this, R.color.emoji_gray70))
                .setBackgroundColor(ContextCompat.getColor(this, R.color.grey_300))
                .setOnEmojiBackspaceClickListener(ignore ->
                        Log.d(TAG, "Clicked on Backspace"))
                .setOnEmojiClickListener((ignore, ignore2) -> Log.d(TAG, "Clicked on emoji"))
                .setOnEmojiPopupShownListener(() -> emojiButton.setImageResource(R.drawable.ic_keyboard))
                .setOnSoftKeyboardOpenListener(ignore -> Log.d(TAG, "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> emojiButton.setImageResource(R.drawable.emoji_ios_category_smileysandpeople))
                .setOnSoftKeyboardCloseListener(() -> {
                    Log.d(TAG, "Closed soft keyboard");
                })
                .setKeyboardAnimationStyle(R.style.slide_animation_style)
                .setPageTransformer((page, position) -> {
                })
                .build(mBinding.groupName);


    }

    // check if the group name is valid
    // if yes show the "next" button, hide it otherwise
    private boolean isValidGroupName() {

        String groupName = groupNameView.getText().toString();
        return StringUtils.isValid(groupName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_new_group, menu);

        actionNextMenuItem = menu.findItem(R.id.action_next);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_next) {
            onActionNextClicked();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void onActionNextClicked() {

        final String chatGroupName = WizardNewGroup.getInstance().getTempChatGroup().getName();
        Map<String, Integer> chatGroupMembers = WizardNewGroup.getInstance().getTempChatGroup().getMembers();

        groupsSyncronizer.createChatGroup(chatGroupName, chatGroupMembers, new ChatGroupCreatedListener() {
            @Override
            public void onChatGroupCreated(ChatGroup chatGroup, ChatRuntimeException chatException) {
                Log.d(DebugConstants.DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked.onChatGroupCreated");

                // clear the wizard
                WizardNewGroup.getInstance().dispose();

                if (chatException == null) {
                    Log.d(DebugConstants.DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
                            ".onChatGroupCreated: chatGroup == " + chatGroup.toString());

                    // create a conversation on the fly
                    Conversation conversation = createConversationForAdapter(chatGroup);

                    // add the conversation to the conversation adapter
                    ChatManager.getInstance().getConversationsHandler().addConversation(conversation);

                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.e(DebugConstants.DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
                            ".onChatGroupCreated: " + chatException.getLocalizedMessage());
                    // TODO: 29/01/18
                    setResult(RESULT_CANCELED);
                    return;
                }
            }
        });
    }

    // create a conversation on the fly
    private Conversation createConversationForAdapter(ChatGroup chatGroup) {
        Conversation conversation = new Conversation();
        conversation.setChannelType(Message.GROUP_CHANNEL_TYPE);
        conversation.setConversationId(chatGroup.getGroupId());
        conversation.setTimestamp(chatGroup.getCreatedOnLong());
        conversation.setIs_new(true);
        conversation.setRecipientFullName(chatGroup.getName());
        conversation.setConvers_with(chatGroup.getGroupId());
        conversation.setConvers_with_fullname(chatGroup.getName());
        Log.d(DebugConstants.DEBUG_GROUPS, "NewGroupActivity.onActionNextClicked" +
                ".onChatGroupCreated: it has been created on the fly the conversation == " + conversation.toString());
        return conversation;
    }
}
