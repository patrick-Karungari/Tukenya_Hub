package com.patrickarungari.messenger.ui.messages.activities;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.patrickarungari.messenger.R;
import com.patrickarungari.messenger.core.ChatManager;
import com.patrickarungari.messenger.core.chat_groups.listeners.ChatGroupsListener;
import com.patrickarungari.messenger.core.chat_groups.models.ChatGroup;
import com.patrickarungari.messenger.core.chat_groups.syncronizers.GroupsSyncronizer;
import com.patrickarungari.messenger.core.contacts.synchronizers.ContactsSynchronizer;
import com.patrickarungari.messenger.core.exception.ChatRuntimeException;
import com.patrickarungari.messenger.core.messages.handlers.ConversationMessagesHandler;
import com.patrickarungari.messenger.core.messages.listeners.ConversationMessagesListener;
import com.patrickarungari.messenger.core.messages.listeners.SendMessageListener;
import com.patrickarungari.messenger.core.messages.models.Message;
import com.patrickarungari.messenger.core.presence.PresenceHandler;
import com.patrickarungari.messenger.core.presence.listeners.PresenceListener;
import com.patrickarungari.messenger.core.users.models.IChatUser;
import com.patrickarungari.messenger.storage.OnUploadedCallback;
import com.patrickarungari.messenger.storage.StorageHandler;
import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.messenger.ui.chat_groups.activities.GroupAdminPanelActivity;
import com.patrickarungari.messenger.ui.messages.adapters.MessageListAdapter;
import com.patrickarungari.messenger.ui.messages.fragments.BottomSheetAttach;
import com.patrickarungari.messenger.ui.messages.listeners.OnMessageClickListener;
import com.patrickarungari.messenger.ui.users.activities.PublicProfileActivity;
import com.patrickarungari.messenger.utils.DebugConstants;
import com.patrickarungari.messenger.utils.StringUtils;
import com.patrickarungari.messenger.utils.TimeUtils;
import com.patrickarungari.messenger.utils.image.CropCircleTransformation;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.patrickarungari.messenger.core.ChatManager.Configuration.appId;
import static com.patrickarungari.messenger.ui.ChatUI.BUNDLE_CHANNEL_TYPE;

/**
 * Created by stefano on 31/08/2015.
 */
public class MessageListActivity extends AppCompatActivity
        implements ConversationMessagesListener, PresenceListener, ChatGroupsListener {
    private static final String TAG = MessageListActivity.class.getName();

    public static final int _INTENT_ACTION_GET_PICTURE = 853;

    private PresenceHandler presenceHandler = null;
    private ConversationMessagesHandler conversationMessagesHandler;
    private boolean conversWithOnline = false;
    private long conversWithLastOnline = -1;
    private DatabaseReference typingRef;
    private GroupsSyncronizer groupsSyncronizer = null;

    private RecyclerView recyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private MessageListAdapter messageListAdapter;
    private Toolbar toolbar;

    private ImageView mPictureView;
    private TextView mTitleTextView;
    private TextView mSubTitleTextView;


    private EmojiPopup emojiPopup;
    private EmojiEditText editText;
    private ViewGroup rootView;
    private ImageView emojiButton;
    private ImageView attachButton;
    private ImageView sendButton;
    private LinearLayout mEmojiBar;

    /**
     * {@code recipient} is the real contact whom is talking with.
     * it contains all the info to start a conversation.
     */
    private IChatUser recipient;
    /**
     * {@code chatGroup} is a support item witch contains all additional info
     * about group such as the members list which cannot be included inside the {@code recipient}
     */
    private ChatGroup chatGroup;
    private String channelType; // detect if is a group or a direct conversation
    private DatabaseReference lastOnlineRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        groupsSyncronizer = ChatManager.getInstance().getGroupsSyncronizer();

        setContentView(R.layout.activity_message_list);

        registerViews();

        // retrieve recipient
        recipient = (IChatUser) getIntent().getSerializableExtra(ChatUI.BUNDLE_RECIPIENT);

        // BEGIN contactsSynchronizer
        ContactsSynchronizer contactsSynchronizer = ChatManager.getInstance().getContactsSynchronizer();
        if (contactsSynchronizer != null) {
            IChatUser matchedContact = contactsSynchronizer.findById(recipient.getId());

            if (matchedContact != null) {
                recipient = matchedContact;

            }
        }


        // END contactsSynchronizer

        // retrieve channel type
        channelType = (String) getIntent().getExtras().get(BUNDLE_CHANNEL_TYPE);
        // default case
        if (!StringUtils.isValid(channelType)) {
            channelType = Message.DIRECT_CHANNEL_TYPE;
        }

        // get the recipient from background notification
        if (isFromBackgroundNotification()) {
            recipient = getUserFromBackgroundNotification();
        }

        if (channelType.equals(Message.GROUP_CHANNEL_TYPE)) {
            chatGroup = ChatManager.getInstance().getGroupsSyncronizer().getById(recipient.getId());
        } else {
            // default case : DIRECT_CHANNEL_TYPE
        }

        // ######### begin conversation messages handler
        conversationMessagesHandler = ChatManager.getInstance().getConversationMessagesHandler(recipient);
        conversationMessagesHandler.upsertConversationMessagesListener(this);
        Log.d(TAG, "MessageListActivity.onCreate: conversationMessagesHandler attached");
        conversationMessagesHandler.connect();
        Log.d(TAG, "MessageListActivity.onCreate: conversationMessagesHandler connected");
        // ######### end conversation messages handler

        initRecyclerView();

        //////// toolbar
        if (channelType.equals(Message.DIRECT_CHANNEL_TYPE)) {
            if (recipient != null) {
                initDirectToolbar(recipient);
            }
        } else if (channelType.equals(Message.GROUP_CHANNEL_TYPE)) {
            if (chatGroup != null) {
                initGroupToolbar(chatGroup);
            }
        }

        // minimal settings
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //////// end toolbar

        /////// presence manager
        if (channelType.equals(Message.DIRECT_CHANNEL_TYPE)) {
            if (recipient != null) {
                presenceHandler = ChatManager.getInstance().getPresenceHandler(recipient.getId());
                presenceHandler.upsertPresenceListener(this);
                presenceHandler.connect();
            }
        } else {
            if (recipient != null) {
                groupsSyncronizer = ChatManager.getInstance().getGroupsSyncronizer();
                groupsSyncronizer.upsertGroupsListener(this);
                groupsSyncronizer.connect();
            }
        }


        // panel which contains the edittext, the emoji button and the attach button
        initInputPanel();
        ChatManager.getInstance().getMyPresenceHandler().connect();

    }


    // if the calling intent contains the
    private boolean isFromBackgroundNotification() {

        if (getIntent() == null) {
            return false;
        } else {
            if (!getIntent().hasExtra("sender")) {
                return false;
            } else {
                String recipient = getIntent().getStringExtra("sender");
                return StringUtils.isValid(recipient) ? true : false;
            }
        }
    }

    private IChatUser getUserFromBackgroundNotification() {
        if (StringUtils.isValid(getIntent().getStringExtra("sender"))) {
            String recipientId = getIntent().getStringExtra("sender");
            Log.d(DebugConstants.DEBUG_NOTIFICATION, "MessageListActivity.findUserByBackgroundPushRecicpientId:" +
                    " recipientId == " + recipientId);

            return ChatManager.getInstance().getContactsSynchronizer().findById(recipientId);
        } else {
            throw new ChatRuntimeException("Recipient can not be retrieved! " +
                    "Did you pass it correctly?");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // set the active conversation
        ChatManager.getInstance().getConversationsHandler()
                .setCurrentOpenConversationId(recipient.getId());
        Log.d(TAG, "MessageListActivity.onResume: " +
                "currentOpenConversationId == " + recipient.getId());
        ChatManager.getInstance().getMyPresenceHandler().connect();
        // set the current conversation as read
        ChatManager.getInstance()
                .getConversationsHandler()
                .setConversationRead(recipient.getId());
    }

    @Override
    protected void onPause() {
        // unset the active conversation
        ChatManager.getInstance().getConversationsHandler()
                .setCurrentOpenConversationId(null);
        Log.d(TAG, "MessageListActivity.onResume: currentOpenConversationId detached");

        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "  MessageListActivity.onStop");

        // dismiss the emoji panel
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "  MessageListActivity.onDestroy");

        if (presenceHandler != null) {
            presenceHandler.removePresenceListener(this);
            Log.d(TAG, "MessageListActivity.onDestroy:" +
                    " presenceHandler detached");
        }

        if (groupsSyncronizer != null) {
            groupsSyncronizer.removeGroupsListener(this);
            Log.d(TAG, "MessageListActivity.onDestroy:" +
                    " groupsSyncronizer detached");
        }

        // unset the active conversation
        ChatManager.getInstance().getConversationsHandler()
                .setCurrentOpenConversationId(null);
        Log.d(TAG, "MessageListActivity.onResume: currentOpenConversationId detached");

        // detach the conversation messages listener
        conversationMessagesHandler.removeConversationMessagesListener(this);
    }

    private void registerViews() {
        Log.d(TAG, "registerViews");

        recyclerView = (RecyclerView) findViewById(R.id.main_activity_recycler_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mPictureView = (ImageView) findViewById(R.id.toolbar_picture);
        mTitleTextView = (TextView) findViewById(R.id.toolbar_title);
        mSubTitleTextView = (TextView) findViewById(R.id.toolbar_subtitle);

        editText = (EmojiEditText) findViewById(R.id.main_activity_chat_bottom_message_edittext);
        rootView = (ViewGroup) findViewById(R.id.main_activity_root_view);
        emojiButton = (ImageView) findViewById(R.id.main_activity_emoji);
        attachButton = (ImageView) findViewById(R.id.main_activity_attach);
        sendButton = (ImageView) findViewById(R.id.main_activity_send);
        recyclerView = (RecyclerView) findViewById(R.id.main_activity_recycler_view);
        mEmojiBar = (LinearLayout) findViewById(R.id.main_activity_emoji_bar);
        lastOnlineRef = FirebaseDatabase.getInstance().getReference()
                .child("/apps/" + appId + "/presence/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/lastOnline");
        KeyboardVisibilityEvent.setEventListener(
                this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        if (isOpen) {
                            // some code depending on keyboard visiblity status
                            //ChatManager.getInstance().getMyPresenceHandler().connect(true);
                            lastOnlineRef.setValue("100000000000");
                        } else {
                            //ChatManager.getInstance().getMyPresenceHandler().connect(false);
                            lastOnlineRef.setValue(ServerValue.TIMESTAMP);
                        }
                    }
                });


    }

    private void initDirectToolbar(final IChatUser recipient) {
        // toolbar picture
        setPicture(recipient.getProfilePictureUrl(), R.drawable.ic_person_avatar);

        // toolbar recipient display name
        mTitleTextView.setText(recipient.getFullName());

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageListActivity.this,
                        PublicProfileActivity.class);

                intent.putExtra(ChatUI.BUNDLE_RECIPIENT, recipient);
                startActivity(intent);
            }
        });
    }

    private void initGroupToolbar(final ChatGroup chatGroup) {
        // toolbar picture
        setPicture(chatGroup.getIconURL(), R.drawable.ic_group_avatar);

        // group name
        mTitleTextView.setText(chatGroup.getName());

        // toolbar group members
        String groupMembers;
        if (chatGroup != null && chatGroup.getMembersList() != null &&
                chatGroup.getMembersList().size() > 0) {
            groupMembers = chatGroup.printMembersListWithSeparator(", ");
        } else {
            // if there are no members show the logged user as "you"
            groupMembers = getString(R.string.activity_message_list_group_info_you_label);
        }
        mSubTitleTextView.setText(groupMembers);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageListActivity.this,
                        GroupAdminPanelActivity.class);
                intent.putExtra(ChatUI.BUNDLE_GROUP_ID, chatGroup.getGroupId());
                startActivity(intent);
            }
        });
    }

    private void setPicture(String pictureUrl, @DrawableRes int placeholder) {
        Glide.with(getApplicationContext())
                .load(StringUtils.isValid(pictureUrl) ? pictureUrl : "")
                .placeholder(placeholder)
                .transform(new CropCircleTransformation(getApplicationContext()))
                .into(mPictureView);
    }

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView");

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);  // put adding from bottom
        recyclerView.setLayoutManager(mLinearLayoutManager);
        initRecyclerViewAdapter(recyclerView);
    }

    private void initRecyclerViewAdapter(RecyclerView recyclerView) {
        Log.d(TAG, "initRecyclerViewAdapter");

        Log.d(TAG, "conversationMessagesHandler.getMessages(): " +
                "size() is " + conversationMessagesHandler.getMessages().size());

        messageListAdapter = new MessageListAdapter(this,
                conversationMessagesHandler.getMessages());
        messageListAdapter.setMessageClickListener(this.onMessageClickListener);
        recyclerView.setAdapter(messageListAdapter);
        postponeEnterTransition();
        startPostponedEnterTransition();
        // scroll to last position
        if (messageListAdapter.getItemCount() > 0) {
            int position = messageListAdapter.getItemCount() - 1;
            mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
        }
    }

    /**
     * Listener called when a message is clicked.
     */
    public OnMessageClickListener onMessageClickListener = new OnMessageClickListener() {
        @Override
        public void onMessageLinkClick(TextView messageView, ClickableSpan clickableSpan) {
            Log.d(TAG, "onMessageClickListener.onMessageLinkClick");
            Log.d(TAG, "text: " + messageView.getText().toString());

            if (ChatUI.getInstance().getOnMessageClickListener() != null) {
                ChatUI.getInstance().getOnMessageClickListener()
                        .onMessageLinkClick(messageView, clickableSpan);
            } else {
                Log.d(TAG, "Chat.Configuration.getMessageClickListener() == null");
            }
        }
    };

    private void initInputPanel() {
        Log.d(TAG, "initInputPanel");

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //typingRef.setValue("Typing...");
            }

            @Override
            public void afterTextChanged(Editable s) {
                //typingRef.setValue(null);
            }
        });

        emojiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                emojiPopup.toggle();
            }
        });
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "MessageListActivity.onAttachClicked");

                if (ChatUI.getInstance().getOnAttachClickListener() != null) {
                    ChatUI.getInstance().getOnAttachClickListener().onAttachClicked(null);
                }

                showAttachBottomSheet();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "onSendClicked");

                String text = editText.getText().toString().trim();

                if (!StringUtils.isValid(text)) {
                    return;
                }

                ChatManager.getInstance().sendTextMessage(recipient.getId(), recipient.getFullName(),
                        text, channelType, null, new SendMessageListener() {
                            @Override
                            public void onBeforeMessageSent(Message message, ChatRuntimeException chatException) {
                                if (chatException == null) {
                                    // if the message exists update it, else add it
                                    Log.d(TAG, "sendTextMessage.onBeforeMessageSent.message.id: " + message.getId());
                                    Log.d(TAG, "sendTextMessage.onBeforeMessageSent.message.recipient: " + message.getRecipient());

                                    messageListAdapter.updateMessage(message);
                                    scrollToBottom();
                                } else {

                                    Toast.makeText(MessageListActivity.this,
                                            "Failed to send message",
                                            Toast.LENGTH_SHORT).show();

                                    Log.e(TAG, "sendTextMessage.onBeforeMessageSent: ", chatException);
                                }
                            }

                            @Override
                            public void onMessageSentComplete(Message message, ChatRuntimeException chatException) {
                                if (chatException == null) {

                                    Log.d(TAG, "message sent: " + message.toString());
                                } else {
                                    Toast.makeText(MessageListActivity.this,
                                            "Failed to send message",
                                            Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "error sending message : ", chatException);
                                }
                            }
                        });

                // clear the edittext
                editText.setText("");
            }
        });
        setUpEmojiPopup();

        if (channelType.equals(Message.GROUP_CHANNEL_TYPE)) {
            if (chatGroup != null && chatGroup.getMembersList().contains(ChatManager.getInstance().getLoggedUser())) {
                mEmojiBar.setVisibility(View.VISIBLE);
            } else {
                mEmojiBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onConversationMessageReceived(Message message, ChatRuntimeException e) {
        Log.d(TAG, "onConversationMessageReceived");

        if (e == null) {
            messageListAdapter.updateMessage(message);
            scrollToBottom();
        } else {
            Log.w(TAG, "Error onConversationMessageReceived ", e);
        }
    }

    @Override
    public void onConversationMessageChanged(Message message, ChatRuntimeException e) {
        Log.d(TAG, "onConversationMessageChanged");

        if (e == null) {
            messageListAdapter.updateMessage(message);
            scrollToBottom();

        } else {
            Log.w(TAG, "Error onConversationMessageReceived ", e);
        }
    }

    private void scrollToBottom() {
        // scroll to last position
        if (messageListAdapter.getItemCount() > 0) {
            int position = messageListAdapter.getItemCount() - 1;
            mLinearLayoutManager.scrollToPositionWithOffset(position, 0);
        }
    }

    private void showAttachBottomSheet() {
        Log.d(TAG, "MessageListActivity.onAttachClicked");

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        BottomSheetAttach dialog = BottomSheetAttach.newInstance(recipient, channelType);
        dialog.show(ft, BottomSheetAttach.class.getName());
    }

    @TargetApi(19)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {

        if (requestCode == _INTENT_ACTION_GET_PICTURE) {
            if (data != null /*&& data.getData() != null*/ && resultCode == RESULT_OK) {

                ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
                Uri uri = files != null ? files.get(0).getUri() : null;

                // convert the stream to a file
                File fileToUpload = new File(StorageHandler.getFilePathFromUri(this, uri));
                showConfirmUploadDialog(fileToUpload);
                showPhoto(uri);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showPhoto(Uri uri) {
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(uri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    // bugfix Issue #64
    private void showConfirmUploadDialog(
            final File file) {
        Log.d(TAG, "uploadFile");

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.activity_message_list_confirm_dialog_upload_title_label))
                .setMessage(getString(R.string.activity_message_list_confirm_dialog_upload_message_label))
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // upload the file
                        uploadFile(file);
                    }
                })
                .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss(); // close the alert dialog
                    }
                }).show();
    }

    // bugfix Issue #15
    private void uploadFile(File file) {
        Log.d(TAG, "uploadFile");

        // bugfix Issue #45
        final ProgressDialog progressDialog = new ProgressDialog(MessageListActivity.this);
        progressDialog.setMessage(getString(R.string.activity_message_list_progress_dialog_upload));
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageHandler.uploadFile(this, file, new OnUploadedCallback() {
            @Override
            public void onUploadSuccess(final String uid, final Uri downloadUrl, final String type) {
                Log.d(TAG, "uploadFile.onUploadSuccess - downloadUrl: " + downloadUrl);

                progressDialog.dismiss(); // bugfix Issue #45

                Glide.with(getApplicationContext())
                        .asBitmap()
                        .load(downloadUrl)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                int width = resource.getWidth();
                                int height = resource.getHeight();

                                Log.d(TAG, " MessageListActivity.uploadFile:" +
                                        " width == " + width + " - height == " + height);

                                Map<String, Object> metadata = new HashMap<>();
                                metadata.put("width", width);
                                metadata.put("height", height);
                                metadata.put("src", downloadUrl.toString());
//                                metadata.put("uid", uid);
                                metadata.put("description", "");

                                Log.d(TAG, " MessageListActivity.uploadFile:" +
                                        " metadata == " + metadata);

                                // get the localized type
                                String lastMessageText = "";
                                if (type.toLowerCase().equals(StorageHandler.Type.Image.toString().toLowerCase())) {
                                    lastMessageText = "image:gqdgbnsgekv2vqel";
                                } else if (type.equals(StorageHandler.Type.File)) {
                                    lastMessageText = getString(R.string.activity_message_list_type_file_label);
                                }

                                // TODO: 13/02/18 add image message to the adapter  (like text message)
                                ChatManager.getInstance().sendImageMessage(recipient.getId(),
                                        recipient.getFullName(), lastMessageText /*+ ": " + downloadUrl.toString()*/, channelType,
                                        metadata, null);
                            }

                        });
            }

            @Override
            public void onProgress(double progress) {
                Log.d(TAG, "uploadFile.onProgress - progress: " + progress);

                // bugfix Issue #45
                progressDialog.setProgress((int) progress);

                // TODO: 06/09/17 progress within viewholder
            }

            @Override
            public void onUploadFailed(Exception e) {
                Log.e(TAG, "uploadFile.onUploadFailed: " + e.getMessage());

                progressDialog.dismiss(); // bugfix Issue #45

                Toast.makeText(MessageListActivity.this,
                        getString(R.string.activity_message_list_progress_dialog_upload_failed),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // bugfix Issue #4
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }


    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setIconColor(ContextCompat.getColor(this, R.color.emoji_gray70))
                .setBackgroundColor(ContextCompat.getColor(this, R.color.grey_300))
                .setOnEmojiBackspaceClickListener(ignore ->
                        Log.d(TAG, "Clicked on Backspace"))
                .setOnEmojiClickListener((ignore, ignore2) -> Log.d(TAG, "Clicked on emoji"))
                .setOnEmojiPopupShownListener(() -> emojiButton.setImageResource(R.drawable.ic_keyboard_hide))
                .setOnSoftKeyboardOpenListener(ignore -> Log.d(TAG, "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> emojiButton.setImageResource(R.drawable.emoji_ios_category_smileysandpeople))
                .setOnSoftKeyboardCloseListener(() -> {
                    Log.d(TAG, "Closed soft keyboard");
                })
                .setKeyboardAnimationStyle(R.style.slide_animation_style)
                .setPageTransformer((page, position) -> {
                })
                .build(editText);


    }

    @Override
    public void isUserOnline(boolean isConnected) {
        Log.d(DebugConstants.DEBUG_USER_PRESENCE, "MessageListActivity.isUserOnline: " +
                "isConnected == " + isConnected);

        if (isConnected) {
            conversWithOnline = true;
            mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_online));
        } else {
            conversWithOnline = false;
            if (conversWithLastOnline != PresenceHandler.LAST_ONLINE_UNDEFINED) {
                if (conversWithLastOnline == Long.parseLong("1000000000000")){
                    mSubTitleTextView.setText("Typing...");
                    Log.d(DebugConstants.DEBUG_USER_PRESENCE, "MessageListActivity.isUserOnline: " +
                            "conversWithLastOnline == " + conversWithLastOnline);
                }
                mSubTitleTextView.setText(TimeUtils.getFormattedLastSeen(this, conversWithLastOnline));
                Log.d(DebugConstants.DEBUG_USER_PRESENCE, "MessageListActivity.isUserOnline: " +
                        "conversWithLastOnline == " + conversWithLastOnline);
            } else {
                mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_offline));
            }
        }
    }

    @Override
    public void userLastOnline(long lastOnline) {
        Log.d(DebugConstants.DEBUG_USER_PRESENCE, "MessageListActivity.userLastOnline: " +
                "lastOnline == " + lastOnline);

        conversWithLastOnline = lastOnline;

        if (!conversWithOnline) {
            mSubTitleTextView.setText(TimeUtils.getFormattedLastSeen(this, lastOnline));
        }

        if (!conversWithOnline && lastOnline == PresenceHandler.LAST_ONLINE_UNDEFINED) {
            mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_offline));
        }
    }

    @Override
    public void onPresenceError(Exception e) {
        Log.e(DebugConstants.DEBUG_USER_PRESENCE, "MessageListActivity.onMyPresenceError: " + e.toString());

        mSubTitleTextView.setText(getString(R.string.activity_message_list_convers_with_presence_offline));
    }

    @Override
    public void onGroupAdded(ChatGroup chatGroup, ChatRuntimeException e) {
        if (e == null) {
            this.chatGroup = chatGroup;
            initGroupToolbar(chatGroup);
            initInputPanel();
        } else {
            Log.e(TAG, "MessageListActivity.onGroupAdded: " + e.toString());
        }
    }

    @Override
    public void onGroupChanged(ChatGroup chatGroup, ChatRuntimeException e) {
        if (e == null) {
            this.chatGroup = chatGroup;
            initGroupToolbar(chatGroup);
            initInputPanel();
        } else {
            Log.e(TAG, "MessageListActivity.onGroupChanged: " + e.toString());
        }
    }

    @Override
    public void onGroupRemoved(ChatRuntimeException e) {
        Log.e(TAG, "MessageListActivity.onGroupRemoved: " + e.toString());
    }
}