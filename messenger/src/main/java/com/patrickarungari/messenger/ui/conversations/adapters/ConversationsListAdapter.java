package com.patrickarungari.messenger.ui.conversations.adapters;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.daimajia.swipe.SwipeLayout;
import com.patrickarungari.messenger.R;
import com.patrickarungari.messenger.core.ChatManager;
import com.patrickarungari.messenger.core.chat_groups.models.ChatGroup;
import com.patrickarungari.messenger.core.conversations.models.Conversation;
import com.patrickarungari.messenger.core.users.models.IChatUser;
import com.patrickarungari.messenger.ui.adapters.AbstractRecyclerAdapter;
import com.patrickarungari.messenger.ui.conversations.listeners.OnConversationClickListener;
import com.patrickarungari.messenger.ui.conversations.listeners.OnConversationLongClickListener;
import com.patrickarungari.messenger.ui.conversations.listeners.OnSwipeMenuCloseClickListener;
import com.patrickarungari.messenger.ui.conversations.listeners.OnSwipeMenuUnreadClickListener;
import com.patrickarungari.messenger.utils.StringUtils;
import com.patrickarungari.messenger.utils.TimeUtils;
import com.patrickarungari.messenger.utils.image.CropCircleTransformation;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

/**
 * Created by stefanodp91 on 18/12/17.
 */
public class ConversationsListAdapter extends AbstractRecyclerAdapter<Conversation, ConversationsListAdapter.ViewHolder> {
    private static final String TAG = ConversationsListAdapter.class.getName();

    private OnConversationClickListener onConversationClickListener;
    private OnConversationLongClickListener onConversationLongClickListener;
    private OnSwipeMenuCloseClickListener onSwipeMenuCloseClickListener;
    private OnSwipeMenuUnreadClickListener onSwipeMenuUnreadClickListener;

    public OnConversationClickListener getOnConversationClickListener() {
        return onConversationClickListener;
    }

    public void setOnConversationClickListener(OnConversationClickListener onConversationClickListener) {
        this.onConversationClickListener = onConversationClickListener;
    }

    public OnConversationLongClickListener getOnConversationLongClickListener() {
        return onConversationLongClickListener;
    }

    public void setOnConversationLongClickListener(
            OnConversationLongClickListener onConversationLongClickListener) {
        this.onConversationLongClickListener = onConversationLongClickListener;
    }

    public void setOnSwipeMenuCloseClickListener(OnSwipeMenuCloseClickListener onSwipeMenuCloseClickListener) {
        this.onSwipeMenuCloseClickListener = onSwipeMenuCloseClickListener;
    }

    public OnSwipeMenuCloseClickListener getOnSwipeMenuCloseClickListener() {
        return onSwipeMenuCloseClickListener;
    }

    public void setOnSwipeMenuUnreadClickListener(OnSwipeMenuUnreadClickListener onSwipeMenuUnreadClickListener) {
        this.onSwipeMenuUnreadClickListener = onSwipeMenuUnreadClickListener;
    }

    public OnSwipeMenuUnreadClickListener getOnSwipeMenuUnreadClickListener() {
        return onSwipeMenuUnreadClickListener;
    }

    public ConversationsListAdapter(Context context, List<Conversation> conversations) {
        super(context, conversations);
    }

    @Override
    public void setList(List<Conversation> mList) {
        super.setList(mList);
    }

    @Override
    public ConversationsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_conversation, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull ConversationsListAdapter.ViewHolder holder, final int position) {
        final Conversation conversation = getItem(position);

        setRecipientPicture(holder, conversation);

        setRecipientDisplayName(holder, conversation.getConvers_with_fullname(), conversation.getConvers_with());

        setGroupSenderName(holder, conversation);

        setLastMessageText(holder, conversation);

        setTimestamp(holder, conversation.getIs_new(), conversation.getTimestampLong());

        setConversationCLickAction(holder, conversation, position);

        setConversationLongCLickAction(holder, conversation, position);

        setOnCloseClickListener(holder, conversation, position);

        setOnUnreadClickListener(holder, conversation, position);
        setType(holder,conversation,position);
        setTextButton(holder, conversation);
    }

    private void setType(ViewHolder holder, Conversation conversation, int position) {
        if (conversation.getLast_message_text()!=null && conversation.getLast_message_text().length() == ("image:gqdgbnsgekv2vqel").length() &&
        conversation.getLast_message_text().equals("image:gqdgbnsgekv2vqel")&&
        conversation.getLast_message_text().contains("image:gqdgbnsgekv2vqel")){
            holder.type.setVisibility(View.VISIBLE);
            holder.lastTextMessage.setText("");
            holder.type.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.ic_image));
        }
    }


    private void setRecipientPicture(ViewHolder holder, Conversation conversation) {
        String picture = "";
        if (conversation.isDirectChannel()) {

            // retrieve the contact
            IChatUser contact = ChatManager.getInstance()
                    .getContactsSynchronizer()
                    .findById(conversation.getConvers_with());

            // retrieve the contact picture
            if (contact != null) {
                picture = contact.getProfilePictureUrl();
            }

            // show the contact picture
            Glide.with(holder.itemView.getContext())
                    .load(picture)
                    .placeholder(R.drawable.ic_person_avatar)
                    .transform(new CropCircleTransformation(holder.itemView.getContext()))
                    .into(holder.recipientPicture);
        } else if (conversation.isGroupChannel()) {

            // retrieve the group
            ChatGroup chatGroup = ChatManager.getInstance()
                    .getGroupsSyncronizer()
                    .getById(conversation.getConversationId());

            // retrieve the group picture
            if (chatGroup != null) {
                picture = chatGroup.getIconURL();
            }

            Glide.with(holder.itemView.getContext())
                    .load(picture)
                    .placeholder(R.drawable.ic_group_avatar)
                    .transform(new CropCircleTransformation(holder.itemView.getContext()))
                    .into(holder.recipientPicture);
        } else {
            Toast.makeText(holder.itemView.getContext(),
                    "channel type is undefined",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    // set the recipient display name whom are talking with
    private void setRecipientDisplayName(ViewHolder holder, String recipientFullName, String recipientId) {
        String displayName;
        if (StringUtils.isValid(recipientFullName)) displayName = recipientFullName;
        else displayName = recipientId;
        holder.recipientDisplayName.setText(displayName);
    }

    // show te last message text
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setLastMessageText(ViewHolder holder, Conversation conversation) {
        // default text message
        String lastMessageText;
        if (StringUtils.isValid(conversation.getLast_message_text()))
            lastMessageText = conversation.getLast_message_text();
        else lastMessageText = "";

        if (conversation.getIs_new()) {
            // show bold text
            holder.lastTextMessage.setText(Html.fromHtml("<b>" + lastMessageText + "</b>"));
        } else {
            // not not bold text
            holder.lastTextMessage.setText(lastMessageText);
        }
        if (conversation.getSender() != null && conversation.getSender()
                .equals(ChatManager.getInstance().getLoggedUser().getId()) && conversation.isDirectChannel()) {
            if (conversation.getIs_new() != null && !conversation.getIs_new()){
                holder.send.setVisibility(View.VISIBLE);
                holder.send.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.ic_message_receipt_16dp));
               // holder.send.getDrawable().setTint(ContextCompat.getColor(holder.itemView.getContext(), R.color.blue_300));
            }

        }
    }

    private void setGroupSenderName(ViewHolder holder, Conversation conversation) {
        // retrieve the sender
        String sender = null;
        if (conversation.isGroupChannel()) {
          /*  if (conversation.getSender() != null && conversation.getSender()
                    .equals(ChatManager.getInstance().getLoggedUser().getId())) {
                //sender = holder.itemView.getContext().getString(R.string.activity_conversation_list_adapter_you_label);
                sender = "null";
            } else {
                if (StringUtils.isValid(conversation.getSender())) {
                    if (!conversation.getSender().equals("system")) {
                        sender = conversation.getSender_fullname();
                    }
                }
            }*/

            if (conversation.getSender() != null &&StringUtils.isValid(conversation.getSender()) && conversation.getSender().equals("system")) {
                holder.senderDisplayName.setVisibility(View.GONE); // hide it
                holder.send.setVisibility(View.GONE);
                holder.type.setVisibility(View.GONE);
            } else if (conversation.getSender() != null &&conversation.getSender().equals(ChatManager.getInstance().getLoggedUser().getId()) && StringUtils.isValid(conversation.getSender())){
                holder.send.setVisibility(View.VISIBLE);
                holder.send.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(),R.drawable.ic_message_receipt_16dp));
                holder.senderDisplayName.setVisibility(View.GONE);// hide it
            }else if (conversation.getSender() != null && StringUtils.isValid(conversation.getSender())){
                holder.senderDisplayName.setText(sender + ": "); // set it
                holder.senderDisplayName.setVisibility(View.VISIBLE); // show it
            }
        } else {
            if (conversation.getSender() != null && conversation.getSender()
                    .equals(ChatManager.getInstance().getLoggedUser().getId())) {
                //sender = holder.itemView.getContext().getString(R.string.activity_conversation_list_adapter_you_label);
                holder.send.setVisibility(View.VISIBLE);
            }
            holder.senderDisplayName.setVisibility(View.GONE);
        }
    }

    // show the last sent message timestamp
    private void setTimestamp(ViewHolder holder, boolean hasNewMessages, long timestamp) {
        // format the timestamp to a pretty visible format
        String formattedTimestamp = TimeUtils.getFormattedTimestamp(holder.itemView.getContext(), timestamp);

        if (hasNewMessages) {
            // show bold text
            holder.lastMessageTimestamp.setText(Html.fromHtml("<b>" + formattedTimestamp + "</b>"));
        } else {
            // not not bold text
            holder.lastMessageTimestamp.setText(formattedTimestamp);
        }
    }

    // set on row click listener
    private void setConversationCLickAction(ViewHolder holder,
                                            final Conversation conversation, final int position) {
        // use the swipe item click listener to solve the open/close issue
        // more details at https://github.com/daimajia/AndroidSwipeLayout/issues/403
        holder.swipeItem.getSurfaceView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getOnConversationClickListener() != null) {
                    getOnConversationClickListener().onConversationClicked(conversation, position);
                } else {
                    Log.w(TAG, "ArchivedConversationsListAdapter.setConversationCLickAction:" +
                            " getOnConversationClickListener() is null. " +
                            "set it with setOnConversationClickListener method. ");
                }
            }
        });
    }

    // set on row long click listener
    private void setConversationLongCLickAction(ViewHolder holder,
                                                final Conversation conversation, final int position) {
        // use the swipe item click listener to solve the open/close issue
        // more details at https://github.com/daimajia/AndroidSwipeLayout/issues/403
        holder.swipeItem.getSurfaceView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (getOnConversationLongClickListener() != null) {
                    getOnConversationLongClickListener().onConversationLongClicked(conversation, position);

                    // source :
                    // https://stackoverflow.com/questions/18911290/perform-both-the-normal-click-and-long-click-at-button
                    return true; // event triggered
                } else {
                    Log.w(TAG, "ArchivedConversationsListAdapter.setConversationLongCLickAction:" +
                            " getOnConversationLongClickListener is null. " +
                            "set it with setOnConversationLongClickListener method. ");
                }

                return false; // event not triggered
            }
        });
    }

    private void setOnCloseClickListener(final ViewHolder holder, final Conversation conversation, final int position) {
        holder.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnSwipeMenuCloseClickListener().onSwipeMenuClosed(conversation, position);
            }
        });
    }

    private void setOnUnreadClickListener(final ViewHolder holder, final Conversation conversation, final int position) {
        holder.unread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // update the text button
                setTextButton(holder, conversation);

                getOnSwipeMenuUnreadClickListener().onSwipeMenuUnread(conversation, position);
            }
        });
    }

    /**
     * Dismiss the swipe menu for the view at position
     *
     * @param position the position of the item to dismiss
     */
    public void dismissSwipeMenu(RecyclerView recyclerView, int position) {
        // retrieve the viewholder at position
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);

        // check if the viewholder is an instance of ConversationListAdapter.ViewHolder
        if (viewHolder instanceof ViewHolder) {

            // cast the holder to ConversationListAdapter.ViewHolder
            ViewHolder holder = (ViewHolder) viewHolder;

            // dismiss the menu
            holder.swipeItem.close();
        }
    }

    private void setTextButton(ViewHolder holder, Conversation conversation) {
        if (conversation.getIs_new()) {
            holder.unread.setText("Read");
        } else {
            holder.unread.setText("Unread");
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView send;
        private ImageView recipientPicture;
        private TextView recipientDisplayName;
        private TextView senderDisplayName;
        private EmojiTextView lastTextMessage;
        private TextView lastMessageTimestamp;
        private ImageView type;
        private TextView close;
        private TextView unread;
        private SwipeLayout swipeItem;

        public ViewHolder(View itemView) {
            super(itemView);

            type = itemView.findViewById(R.id.type);
            recipientPicture = itemView.findViewById(R.id.recipient_picture);
            recipientDisplayName = itemView.findViewById(R.id.recipient_display_name);
            senderDisplayName = itemView.findViewById(R.id.sender_display_name);
            lastTextMessage = itemView.findViewById(R.id.last_text_message);
            lastMessageTimestamp = itemView.findViewById(R.id.last_message_timestamp);
            send = itemView.findViewById(R.id.send);
            close = itemView.findViewById(R.id.close);
            unread = itemView.findViewById(R.id.unread);

            swipeItem = itemView.findViewById(R.id.swipe_item);
            swipeItem.setShowMode(SwipeLayout.ShowMode.PullOut);
//            swipeItem.addDrag(SwipeLayout.DragEdge.Left, swipeItem.findViewById(R.id.swipe_left));
            swipeItem.addDrag(SwipeLayout.DragEdge.Right, swipeItem.findViewById(R.id.swipe_right));
        }
    }
}