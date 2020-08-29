package com.patrickarungari.messenger.ui.archived_conversations.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patrickarungari.messenger.core.ChatManager;
import com.patrickarungari.messenger.core.presence.MyPresenceHandler;
import com.patrickarungari.messenger.core.presence.listeners.MyPresenceListener;
import com.patrickarungari.messenger.core.users.models.ChatUser;
import com.patrickarungari.messenger.core.users.models.IChatUser;
import com.patrickarungari.messenger.ui.archived_conversations.adapters.ArchivedConversationsListAdapter;
import com.patrickarungari.messenger.ui.archived_conversations.listeners.OnSwipeMenuReopenClickListener;
import com.patrickarungari.messenger.utils.DebugConstants;

import com.patrickarungari.messenger.R;

import com.patrickarungari.messenger.core.conversations.ArchivedConversationsHandler;
import com.patrickarungari.messenger.core.conversations.listeners.ConversationsListener;
import com.patrickarungari.messenger.core.conversations.models.Conversation;
import com.patrickarungari.messenger.core.exception.ChatRuntimeException;
import com.patrickarungari.messenger.ui.ChatUI;
import com.patrickarungari.messenger.ui.conversations.listeners.OnConversationClickListener;
import com.patrickarungari.messenger.ui.conversations.listeners.OnConversationLongClickListener;
import com.patrickarungari.messenger.ui.decorations.ItemDecoration;
import com.patrickarungari.messenger.ui.messages.activities.MessageListActivity;

/**
 * Created by stefano on 02/08/2018.
 */
public class ArchivedConversationListFragment extends Fragment implements
        ConversationsListener,
        OnConversationClickListener,
        OnConversationLongClickListener,
        OnSwipeMenuReopenClickListener,
        MyPresenceListener {

    private static final String TAG = ArchivedConversationListFragment.class.getName();

    private ArchivedConversationsHandler conversationsHandler;
    private MyPresenceHandler myPresenceHandler;

    // conversation list recyclerview
    private RecyclerView recyclerViewConversations;
    private LinearLayoutManager rvConversationsLayoutManager;
    private ArchivedConversationsListAdapter conversationsListAdapter;

    // no conversations layout
    private RelativeLayout noConversationsLayout;

    public static Fragment newInstance() {
        Fragment mFragment = new ArchivedConversationListFragment();
        return mFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conversationsHandler = ChatManager.getInstance().getArchivedConversationsHandler();
        myPresenceHandler = ChatManager.getInstance().getMyPresenceHandler();

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "ArchivedConversationListFragment.onCreateView");
        View view = inflater.inflate(R.layout.fragment_archived_conversation_list, container, false);

        // init RecyclerView
        recyclerViewConversations = view.findViewById(R.id.conversations_list);
//        recyclerViewConversations.addItemDecoration(new ItemDecoration(getActivity(),
//                getResources().getDrawable(R.drawable.decorator_fragment_conversation_list)));
        recyclerViewConversations.addItemDecoration(new ItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL,
                getResources().getDrawable(R.drawable.decorator_fragment_archived_conversation_list)));
        rvConversationsLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversations.setLayoutManager(rvConversationsLayoutManager);

        // init RecyclerView adapter
        conversationsListAdapter = new ArchivedConversationsListAdapter(
                getActivity(), conversationsHandler.getConversations());
        conversationsListAdapter.setOnConversationClickListener(this);
        conversationsListAdapter.setOnConversationLongClickListener(this);
        conversationsListAdapter.setOnSwipeMenuReopenClickListener(this);
        recyclerViewConversations.setAdapter(conversationsListAdapter);

        // no conversations layout
        noConversationsLayout = view.findViewById(R.id.layout_no_conversations);
        toggleNoConversationLayoutVisibility(conversationsListAdapter.getItemCount());

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "ArchivedConversationListFragment.onViewCreated");

        conversationsHandler.upsertConversationsListener(this);
        Log.d(TAG, "ArchivedConversationListFragment.onCreateView: conversationMessagesHandler attached");
        conversationsHandler.connect();

        myPresenceHandler.upsertPresenceListener(this);
        Log.d(DebugConstants.DEBUG_MY_PRESENCE, "ArchivedConversationListFragment.onCreateView: myPresenceHandler attached");
        myPresenceHandler.connect();

//        // subscribe for current user presence changes
//        PresenceManger.observeUserPresenceChanges(ChatManager.getInstance().getTenant(),
//                ChatManager.getInstance().getLoggedUser().getId(), onMyPresenceListener);
    }

    @Override
    public void onDestroy() {

        conversationsHandler.removeConversationsListener(this);
        Log.d(TAG, "ArchivedConversationListFragment.onDestroy: conversationMessagesHandler detached");

        myPresenceHandler.removePresenceListener(this);
        Log.d(DebugConstants.DEBUG_MY_PRESENCE, "ArchivedConversationListFragment.onDestroy: myPresenceHandler detached");

        super.onDestroy();
    }


    // toggle the no conversation layout visibilty.
    // if there are items show the list of item, otherwise show a placeholder layout
    private void toggleNoConversationLayoutVisibility(int itemCount) {
        if (itemCount > 0) {
            // show the item list
            recyclerViewConversations.setVisibility(View.VISIBLE);
            noConversationsLayout.setVisibility(View.GONE);
        } else {
            // show the placeholder layout
            recyclerViewConversations.setVisibility(View.GONE);
            noConversationsLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onConversationAdded(Conversation conversation, ChatRuntimeException e) {
        // added a new conversation

        Log.d(TAG, "ArchivedConversationListFragment.onConversationAdded");

        conversationsListAdapter.notifyDataSetChanged();

        toggleNoConversationLayoutVisibility(conversationsListAdapter.getItemCount());
    }

    @Override
    public void onConversationChanged(Conversation conversation, ChatRuntimeException e) {
        // existing conversation updated

        Log.d(TAG, "ArchivedConversationListFragment.onConversationChanged");

        conversationsListAdapter.notifyDataSetChanged();

        toggleNoConversationLayoutVisibility(conversationsListAdapter.getItemCount());
    }

    @Override
    public void onConversationRemoved(ChatRuntimeException e) {
        Log.d(TAG, "ArchivedConversationListFragment.onConversationRemoved");
        if (e == null) {
            conversationsListAdapter.notifyDataSetChanged();
            toggleNoConversationLayoutVisibility(conversationsListAdapter.getItemCount());
        } else {
            Log.d(TAG, "ArchivedConversationListFragment.onConversationRemoved: " + e.toString());
        }
    }

    @Override
    public void onConversationClicked(Conversation conversation, int position) {
        // click on conversation

        // set the conversation as read
        conversationsHandler.setConversationRead(conversation.getConversationId());

        // start the message list activity of the corresponding conversation
        startMessageActivity(conversation);
    }

    @Override
    public void onConversationLongClicked(Conversation conversation, int position) {
        // long click on conversation

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        BSArchivedConversationsListFragmentLongPress dialog =
                BSArchivedConversationsListFragmentLongPress.newInstance(conversation);
        dialog.setConversationsHandler(conversationsHandler);
        dialog.show(ft, BSArchivedConversationsListFragmentLongPress.class.getName());
    }

    private void startMessageActivity(Conversation conversation) {
        Log.d(TAG, "ArchivedConversationListFragment.startMessageActivity: conversation == " + conversation.toString());

        Intent intent = new Intent(getActivity(), MessageListActivity.class);
        IChatUser recipient = new ChatUser(conversation.getConvers_with(), conversation.getConvers_with_fullname());
        intent.putExtra(ChatUI.BUNDLE_RECIPIENT, recipient);
        intent.putExtra(ChatUI.BUNDLE_CHANNEL_TYPE, conversation.getChannelType());
        getActivity().startActivity(intent);
    }

    @Override
    public void isLoggedUserOnline(boolean isConnected, String deviceId) {
        // TODO: 09/01/18
        Log.d(DebugConstants.DEBUG_MY_PRESENCE, "ArchivedConversationListFragment.isUserOnline: " +
                "isConnected == " + isConnected + ", deviceId == " + deviceId);
    }

    @Override
    public void onMyPresenceError(Exception e) {
        // TODO: 09/01/18
        Log.e(DebugConstants.DEBUG_MY_PRESENCE, "ArchivedConversationListFragment.onMyPresenceError: " + e.toString());
    }


    @Override
    public void onSwipeMenuReopened(Conversation conversation, int position) {
        //        Log.i(TAG, "onSwipeMenuUnread: conversation: " + conversation.toString() + " position: " + position);

//        conversationsHandler.toggleConversationRead(conversation.getConversationId());

        // dismiss the swipe menu
        conversationsListAdapter.dismissSwipeMenu(recyclerViewConversations, position);
//        conversationsListAdapter.notifyItemChanged(position);

    }
}